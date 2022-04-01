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
        private final static int countPerRev = 4096 * 2;
        private final static int m = 4096;
        private final int id;
        private double lastSpeed = 0;
        private double lastTime = Timer.getFPGATimestamp();

        // PID
        private static final KPID drivePID = new KPID(Constants.DRIVE_Kp, Constants.DRIVE_Ki,
                        Constants.DRIVE_Kd,
                        0, -1, 1);
        private static final SimpleMotorFeedforward driveFeedforward = new SimpleMotorFeedforward(
                        Constants.DRIVE_Ks, Constants.DRIVE_Kv, Constants.DRIVE_Ka);

        public SwerveWheel(int id, int portTrans, int portRot) {
                this.id = id;

                drive = new TorqueSparkMax(portTrans);
                rotate = new TorqueTalon(portRot);

                drive.configurePID(drivePID);
                drive.configureIZone(Constants.DRIVE_KIz);
                drive.configureSmartMotion(metersPerSecondToEncoderPerMinute(Constants.DRIVE_MAX_SPEED_METERS),
                                metersPerSecondToEncoderPerMinute(Constants.DRIVE_MINIMUM_VELOCITY),
                                metersPerSecondToEncoderPerMinute(Constants.DRIVE_MAX_ACCELERATION_METERS),
                                metersPerSecondToEncoderPerMinute(Constants.DRIVE_ALLOWED_ERROR), 0);
                drive.setSupplyLimit(40); // Amperage supply limit

                rotate.configureSupplyLimit(
                                new SupplyCurrentLimitConfiguration(true, 25, 30, 1));
                rotate.configurePID(new KPID(Constants.DRIVE_ROT_Kp, Constants.DRIVE_ROT_Ki, Constants.DRIVE_ROT_Kd, 0,
                                -1, 1));
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
        private static double fromEncoder(double value) {
                if (value % m == 0)
                        value += .0001;
                double val = value % m * 180 / (m);
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

        public void setDesiredState(SwerveModuleState desiredState) {
                SwerveModuleState state = SwerveModuleState.optimize(desiredState, getRotation());

                if (DriverStation.isTeleop()) {
                        drive.set(state.speedMetersPerSecond * -1 /
                                        Constants.DRIVE_MAX_SPEED_METERS);
                } else {
                        double t = Timer.getFPGATimestamp();
                        double dt = t - lastTime;

                        double en = -metersPerSecondToEncoderPerMinute(state.speedMetersPerSecond);
                        if (id == 0) {
                                // This logs req, real, and residual in the speed of drive motor
                                SmartDashboard.putNumber("SpeedReq" + id, state.speedMetersPerSecond);
                                SmartDashboard.putNumber("SpeedReal" + id,
                                                -encoderPerMinuteToMetersPerSecond(drive.getVelocity()));
                                SmartDashboard.putNumber("SpeedResidual" + id,
                                                -encoderPerMinuteToMetersPerSecond(drive.getVelocity())
                                                                - state.speedMetersPerSecond);
                        }
                        drive.setWithFF(en, ControlType.kSmartVelocity, 0,
                                        -driveFeedforward.calculate(
                                                        lastSpeed, state.speedMetersPerSecond, dt),
                                        ArbFFUnits.kVoltage);

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

                double requestedRotationEncoderUnits = state.angle.getDegrees() * countPerRev / 360.;
                double newPosition = Math.IEEEremainder(requestedRotationEncoderUnits - rotate.getPosition(),
                                countPerRev / 2.)
                                + rotate.getPosition();
                if (id == 0) {
                        // This logs req, real, and residual in the rotation
                        SmartDashboard.putNumber("RotReq" + id, state.angle.getDegrees());
                        SmartDashboard.putNumber("RotReal" + id, fromEncoder(rotate.getPosition()));
                        SmartDashboard.putNumber("RotResidual" + id,
                                        fromEncoder(rotate.getPosition()) - state.angle.getDegrees());
                }
                rotate.set(newPosition, ControlMode.Position);
        }
}