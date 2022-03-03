package org.texastorque.modules;

import com.ctre.phoenix.motorcontrol.SupplyCurrentLimitConfiguration;
import com.revrobotics.CANSparkMax.ControlType;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.DriverStation;
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

        // PID
        private static final KPID drivePID = new KPID(Constants.DRIVE_Kp, Constants.DRIVE_Ki, Constants.DRIVE_Kd,
                        Constants.DRIVE_Kf, -1, 1);
        private static final PIDController rotatePID = new PIDController(.008, 0, 0);

        // Convertions
        private static final double degreeToEncoder = m / 180.0;
        private static final double encoderToDegree = 180.0 / m;

        public SwerveWheel(int id, int portTrans, int portRot) {
                this.id = id;

                drive = new TorqueSparkMax(portTrans);
                rotate = new TorqueTalon(portRot);

                drive.configurePID(drivePID);
                drive.configureIZone(Constants.DRIVE_KIz);
                drive.setSupplyLimit(40); // Amperage supply limit

                rotatePID.enableContinuousInput(-180, 180);
                rotate.configureSupplyLimit(
                                new SupplyCurrentLimitConfiguration(true, 25, 30, 1));
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
                                drive.getVelocityMeters(Constants.DRIVE_WHEEL_RADIUS_METERS),
                                getRotation());
        }

        // /**
        // *
        // * @param metersPerSecond Speed in meters per second
        // * @return Speed in encoder per second
        // */
        // public double metersPerSecondToEncoderPerSecond(double metersPerSecond) {
        // return 4 * (30.0 * metersPerSecond) /
        // (Math.PI * Constants.DRIVE_WHEEL_RADIUS_METERS);
        // }

        public void setDesiredState(SwerveModuleState desiredState) {
                // desiredState.angle.times(-1);
                SwerveModuleState state = SwerveModuleState.optimize(desiredState, getRotation());

                if (DriverStation.isTeleop()) {
                        drive.set(state.speedMetersPerSecond * -1 /
                                        Constants.DRIVE_MAX_SPEED_METERS);
                } else {
                        double en = -drive.velocityMetersToEncoder(
                                        Constants.DRIVE_WHEEL_RADIUS_METERS,
                                        state.speedMetersPerSecond);
                        if (id == 0) {
                                SmartDashboard.putNumber(id + "en", en);
                                SmartDashboard.putNumber(id + "speed",
                                                state.speedMetersPerSecond);
                                SmartDashboard.putNumber(
                                                id + "real", drive.getVelocityMeters(
                                                                Constants.DRIVE_WHEEL_RADIUS_METERS));
                        }
                        drive.set(en, ControlType.kVelocity);
                }

                double requestedRotate = TorqueMathUtil.constrain(
                                rotatePID.calculate(fromEncoder(rotate.getPosition()),
                                                state.angle.getDegrees()),
                                -1, 1);
                rotate.set(requestedRotate);
        }
}