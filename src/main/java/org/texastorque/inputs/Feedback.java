package org.texastorque.inputs;

import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.torquelib.base.TorqueFeedback;

public class Feedback {
    private static volatile Feedback instance;

    public enum GyroDirection {
        CLOCKWISE, COUNTERCLOCKWISE;
    }

    private GyroFeedback gyroFeedback;
    private LimelightFeedback limelightFeedback;

    private Feedback() {
        gyroFeedback = new GyroFeedback();
        limelightFeedback = new LimelightFeedback();
    }

    public void update() {
        gyroFeedback.update();
        limelightFeedback.update();
    }

    public void smartDashboard() {
        gyroFeedback.smartDashboard();
        limelightFeedback.smartDashboard();
    }

    public class GyroFeedback extends TorqueFeedback {
        private final AHRS nxGyro;

        private double pitch;
        private double yaw;
        private double roll;

        private GyroDirection direction = GyroDirection.CLOCKWISE;

        private GyroFeedback() {
            nxGyro = new AHRS(SPI.Port.kMXP);
            nxGyro.getFusedHeading();
        }

        @Override
        public void update() {
            pitch = nxGyro.getPitch();
            roll = nxGyro.getRoll();

            double yaw_t = getDegrees();
            if (yaw_t - yaw > 0.3) {
                direction = GyroDirection.CLOCKWISE;
            } else {
                direction = GyroDirection.COUNTERCLOCKWISE;
            }
            yaw = yaw_t;
        }

        public void resetGyro() {
            nxGyro.reset();
        }

        public void zeroYaw() {
            nxGyro.zeroYaw();
        }

        public Rotation2d getRotation2d() {
            return Rotation2d.fromDegrees(getDegrees());
        }

        public Rotation2d getCCWRotation2d() {
            return Rotation2d.fromDegrees(getCCWDegrees());
        }

        public GyroDirection getGyroDirection() {
            return direction;
        }

        private float getDegrees() {
            return nxGyro.getFusedHeading();
        }

        private float getCCWDegrees() {
            return 360.0f - nxGyro.getFusedHeading();
        }

        @Override
        public void smartDashboard() {
            SmartDashboard.putNumber("[FB]Gyro Pitch", pitch);
            SmartDashboard.putNumber("[FB]Gyro Yaw", yaw);
            SmartDashboard.putNumber("[FB]Gyro Roll", roll);
            SmartDashboard.putNumber("[FB]Gyro Deg", getDegrees());
            SmartDashboard.putNumber("[FB]Gyro CCW Deg", getCCWDegrees());
        }
    }

    public class LimelightFeedback extends TorqueFeedback {
        private NetworkTable limelightTable = NetworkTableInstance.getDefault().getTable("limelight");
        private NetworkTableEntry tx = limelightTable.getEntry("tx");
        private NetworkTableEntry ty = limelightTable.getEntry("ty");
        private NetworkTableEntry ta = limelightTable.getEntry("ta");

        private double hOffset;
        private double vOffset;
        private double taOffset;

        @Override
        public void update() {
            hOffset = tx.getDouble(0);
            vOffset = ty.getDouble(0);
            taOffset = ta.getDouble(0);
        }

        /**
         * @return the hOffset
         */
        public double gethOffset() {
            return hOffset;
        }

        /**
         * @return the vOffset
         */
        public double getvOffset() {
            return vOffset;
        }

        /**
         * @return the taOffset
         */
        public double getTaOffset() {
            return taOffset;
        }

        public void smartDashboard() {
            SmartDashboard.putNumber("hOffset", hOffset);
            SmartDashboard.putNumber("vOffset", vOffset);
            SmartDashboard.putNumber("taOffset", taOffset);
        }

    }

    public GyroFeedback getGyroFeedback() {
        return gyroFeedback;
    }

    public LimelightFeedback getLimelightFeedback() {
        return limelightFeedback;
    }

    public static synchronized Feedback getInstance() {
        return (instance == null) ? instance = new Feedback() : instance;
    }
}