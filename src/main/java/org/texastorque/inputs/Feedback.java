package org.texastorque.inputs;

import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.torquelib.base.TorqueFeedback;

public class Feedback {
    private static volatile Feedback instance;

    private GyroFeedback gyroFeedback;

    private Feedback() { gyroFeedback = new GyroFeedback(); }

    public void update() { gyroFeedback.update(); }

    public void smartDashboard() { gyroFeedback.smartDashboard(); }

    public class GyroFeedback extends TorqueFeedback {
        private final AHRS nxGyro;

        private double pitch;
        private double yaw;
        private double roll;

        private GyroFeedback() {
            nxGyro = new AHRS(SPI.Port.kMXP);
            nxGyro.getFusedHeading();
        }

        @Override
        public void update() {
            pitch = nxGyro.getPitch();
            yaw = nxGyro.getYaw();
            roll = nxGyro.getRoll();
        }

        public void resetGyro() { nxGyro.reset(); }

        public void zeroYaw() { nxGyro.zeroYaw(); }

        public Rotation2d getRotation2d() { return Rotation2d.fromDegrees(getDegrees()); }

        public Rotation2d getCCWRotation2d() { return Rotation2d.fromDegrees(getCCWDegrees()); }

        private float getDegrees() { return nxGyro.getFusedHeading(); }

        private float getCCWDegrees() { return 360.0f - nxGyro.getFusedHeading(); }

        @Override
        public void smartDashboard() {
            SmartDashboard.putNumber("[FB]Gyro Pitch", pitch);
            SmartDashboard.putNumber("[FB]Gyro Yaw", yaw);
            SmartDashboard.putNumber("[FB]Gyro Roll", roll);
            SmartDashboard.putNumber("[FB]Gyro Deg", getDegrees());
            SmartDashboard.putNumber("[FB]Gyro CCW Deg", getCCWDegrees());
        }
    }

    public GyroFeedback getGyroFeedback() { return gyroFeedback; }

    public static synchronized Feedback getInstance() {
        return (instance == null) ? instance = new Feedback() : instance;
    }
}