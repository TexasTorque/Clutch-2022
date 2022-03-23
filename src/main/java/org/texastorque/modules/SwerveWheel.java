package org.texastorque.modules;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.SupplyCurrentLimitConfiguration;
import com.revrobotics.CANSparkMax.ControlType;
import com.revrobotics.SparkMaxPIDController.ArbFFUnits;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.constants.Constants;
import org.texastorque.torquelib.component.TorqueSparkMax;
import org.texastorque.torquelib.component.TorqueTalon;
import org.texastorque.torquelib.util.TorqueMathUtil;
import org.texastorque.util.KPID;

public class SwerveWheel {

        // Motors
        private final TorqueSparkMax drive;
        private final TorqueTalon rotate;

        // Values
        private final int id;
        private static final double m = 4096;
        private double lastSpeed = 0;
        private double lastTime = Timer.getFPGATimestamp();

        // PID
        private static final KPID drivePIDLeft = new KPID(Constants.DRIVE_LEFT_Kp, Constants.DRIVE_LEFT_Ki,
                        Constants.DRIVE_LEFT_Kd,
                        0, -1, 1);
        private static final SimpleMotorFeedforward driveFeedforwardLeft = new SimpleMotorFeedforward(
                        Constants.DRIVE_LEFT_Ks, Constants.DRIVE_LEFT_Kv, Constants.DRIVE_LEFT_Ka);

        private static final KPID drivePIDRight = new KPID(Constants.DRIVE_RIGHT_Kp, Constants.DRIVE_RIGHT_Ki,
                        Constants.DRIVE_RIGHT_Kd,
                        0, -1, 1);
        private static final SimpleMotorFeedforward driveFeedforwardRight = new SimpleMotorFeedforward(
                        Constants.DRIVE_RIGHT_Ks, Constants.DRIVE_RIGHT_Kv, Constants.DRIVE_RIGHT_Ka);

        private final PIDController rotatePID;
        private final double Ks;

        // Convertions
        private static final double degreeToEncoder = m / 180.0;
        private static final double encoderToDegree = 180.0 / m;

        public SwerveWheel(int id, int portTrans, int portRot, PIDController rotatePID, double Ks) {
                this.id = id;
                this.Ks = Ks;

                drive = new TorqueSparkMax(portTrans);
                rotate = new TorqueTalon(portRot);

                if (isLeft()) {
                        drive.configurePID(drivePIDLeft);
                } else {
                        drive.configurePID(drivePIDRight);
                }

                drive.configureIZone(Constants.DRIVE_KIz);
                drive.configureSmartMotion(metersPerSecondToEncoderPerMinute(Constants.DRIVE_MAX_SPEED_METERS),
                                metersPerSecondToEncoderPerMinute(Constants.DRIVE_MINIMUM_VELOCITY),
                                metersPerSecondToEncoderPerMinute(Constants.DRIVE_MAX_ACCELERATION_METERS),
                                metersPerSecondToEncoderPerMinute(Constants.DRIVE_ALLOWED_ERROR), 0);
                drive.setSupplyLimit(40); // Amperage supply limit

                this.rotatePID = rotatePID;
                rotatePID.enableContinuousInput(-180, 180);
                rotatePID.setTolerance(Constants.DRIVE_ROT_TOLERANCE);

                rotate.configureSupplyLimit(
                                new SupplyCurrentLimitConfiguration(true, 25, 30, 1));
                // if (id == 0) {
                // SmartDashboard.putNumber("kp", rotatePID.getP());
                // SmartDashboard.putNumber("ki", rotatePID.getI());
                // SmartDashboard.putNumber("kd", rotatePID.getD());
                // SmartDashboard.putNumber("kadd", add);
                // }
        }

        /**
         *
         * @param value The value in encoder units
         * @return The value in degrees [-180,180]
         */
        private double fromEncoder(double value) {
                double val = value % m * encoderToDegree;
                if (Math.signum(value) == -1 && Math.floor(value / m) % 2 == -0) {
                        val = val + 180;
                } else if (Math.signum(value) == 1 && Math.floor(value / m) % 2 == 1) {
                        val = val - 180;
                }
                return val;
        }

        public Rotation2d getRotation() {
                return Rotation2d.fromDegrees(fromEncoder(rotate.getPosition()));
        }

        public SwerveModuleState getState() {
                return new SwerveModuleState(
                                encoderPerMinuteToMetersPerSecond(drive.getVelocity()),
                                getRotation());
        }

        /**
         *
         * @param metersPerSecond Speed in meters per second
         * @return Speed in encoder rotations per minute
         */
        public double metersPerSecondToEncoderPerMinute(double metersPerSecond) {
                return metersPerSecond * (60. / 1.) * (1 / (2 * Math.PI * Constants.DRIVE_WHEEL_RADIUS_METERS))
                                * (1. / Constants.DRIVE_GEARING);
        }

        /**
         *
         * @param encodersPerMinute Speed in encoder rotations per minute
         * @return Speed in meters per second
         */
        public double encoderPerMinuteToMetersPerSecond(double encodersPerMinute) {
                return encodersPerMinute * (1. / 60.) * (2 * Math.PI * Constants.DRIVE_WHEEL_RADIUS_METERS / 1.)
                                * (Constants.DRIVE_GEARING / 1.);
        }

        /**
         * 
         * @return If the swerve module is on the left side
         */
        private boolean isLeft() {
                return id == 0 || id == 2;
        }

        public void setDesiredState(SwerveModuleState desiredState) {
                SwerveModuleState state = SwerveModuleState.optimize(desiredState, getRotation());
                if (id == 0) {
                        SmartDashboard.putNumber("speedDist", drive.getPosition());
                }
                if (DriverStation.isTeleop()) {
                        drive.set(state.speedMetersPerSecond * -1 /
                                        Constants.DRIVE_MAX_SPEED_METERS);
                } else {
                        double t = Timer.getFPGATimestamp();
                        double dt = t - lastTime;

                        double en = -metersPerSecondToEncoderPerMinute(state.speedMetersPerSecond);
                        if (id == 0) {
                                SmartDashboard.putNumber(id + "en", en);
                                SmartDashboard.putNumber(id + "speed",
                                                state.speedMetersPerSecond);
                                SmartDashboard.putNumber(
                                                id + "real", encoderPerMinuteToMetersPerSecond(drive.getVelocity()));
                        }
                        if (isLeft()) {
                                drive.setWithFF(en, ControlType.kSmartVelocity, 0,
                                                -driveFeedforwardLeft.calculate(
                                                                lastSpeed, state.speedMetersPerSecond, dt),
                                                ArbFFUnits.kVoltage);
                        } else {
                                drive.setWithFF(en, ControlType.kSmartVelocity, 0,
                                                -driveFeedforwardRight.calculate(lastSpeed, state.speedMetersPerSecond,
                                                                dt),
                                                ArbFFUnits.kVoltage);

                        }
                        lastTime = t;
                        lastSpeed = state.speedMetersPerSecond;
                }

                // double kp = SmartDashboard.getNumber("kp", 0);
                // double ki = SmartDashboard.getNumber("ki", 0);
                // double kd = SmartDashboard.getNumber("kd", 0);
                // add = SmartDashboard.getNumber("kadd", add);

                // if (kp != rotatePID.getP()) {
                // rotatePID.setP(kp);
                // }
                // if (ki != rotatePID.getI()) {
                // rotatePID.setI(ki);
                // }
                // if (kd != rotatePID.getD()) {
                // rotatePID.setD(kd);
                // }

                double req = rotatePID.calculate(fromEncoder(rotate.getPosition()),
                                state.angle.getDegrees());
                req = TorqueMathUtil.constrain(req + Ks * Math.signum(req), -1, 1);

                if (id == 0) {
                        SmartDashboard.putNumber("ReqRotvolt", req);
                        SmartDashboard.putNumber("ReqRotreq", state.angle.getDegrees());
                        SmartDashboard.putNumber("ReqRotreal", fromEncoder(rotate.getPosition()));
                }
                if (rotatePID.atSetpoint()) {
                        req = 0;
                }
                rotate.set(req);
        }
}