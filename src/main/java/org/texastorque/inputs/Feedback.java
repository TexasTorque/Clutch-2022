package org.texastorque.inputs;

import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.constants.Constants;
import org.texastorque.torquelib.base.TorqueFeedback;
import org.texastorque.torquelib.util.RollingMedian;

public class Feedback {
    private static volatile Feedback instance;

    public enum GyroDirection {
        CLOCKWISE,
        COUNTERCLOCKWISE;
    }

    private GyroFeedback gyroFeedback;
    private LimelightFeedback limelightFeedback;
    private ShooterFeedback shooterFeedback;
    private ClimberFeedback climberFeedback;

    private Feedback() {
        gyroFeedback = new GyroFeedback();
        limelightFeedback = new LimelightFeedback();
        shooterFeedback = new ShooterFeedback();
        climberFeedback = new ClimberFeedback();
    }

    public void update() {
        gyroFeedback.update();
        limelightFeedback.update();
        shooterFeedback.update();
        climberFeedback.update();
    }

    public void smartDashboard() {
        gyroFeedback.smartDashboard();
        limelightFeedback.smartDashboard();
        shooterFeedback.smartDashboard();
        climberFeedback.smartDashboard();
    }

    public class GyroFeedback extends TorqueFeedback {
        private final AHRS nxGyro;

        private double pitch;
        private double yaw;
        private double roll;

        private float angleOffset = 0;

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
            // return nxGyro.getRoll();
            return (nxGyro.getFusedHeading() + angleOffset) % 360;
        }

        private float getCCWDegrees() {
            return 360.0f - nxGyro.getFusedHeading();
        }

        /**
         * @param angleOffset the angleOffset to set
         */
        public void setAngleOffset(float angleOffset) {
            this.angleOffset = (angleOffset - nxGyro.getFusedHeading() + 360f) % 360f;
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

        private RollingMedian vMedian;
        private RollingMedian hMedian;
        private RollingMedian taMedian;

        private double distance;

        public LimelightFeedback() {
            vMedian = new RollingMedian(4);
            hMedian = new RollingMedian(4);
            taMedian = new RollingMedian(4);
        }

        @Override
        public void update() {
            hOffset = hMedian.calculate(tx.getDouble(0));
            vOffset = vMedian.calculate(ty.getDouble(0));
            taOffset = taMedian.calculate(ta.getDouble(0));
            distance = calcDistance(vOffset);
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

        /**
         * @return Median calculated distance to target
         */
        public double getDistance() {
            return distance;
        }

        /**
         * Calculate distance
         *
         * @param ty Vert degree offset
         * @return distance (m)
         */
        private double calcDistance(double ty) {
            return (Constants.HEIGHT_OF_VISION_STRIP_METERS -
                    Constants.HEIGHT_TO_LIMELIGHT_METERS) /
                    Math.tan(Math.toRadians(Constants.LIMELIGHT_ANGEL_DEG + ty));
        }

        public void smartDashboard() {
            SmartDashboard.putNumber("hOffset", hOffset);
            SmartDashboard.putNumber("vOffset", vOffset);
            SmartDashboard.putNumber("taOffset", taOffset);
            SmartDashboard.putNumber("distance", distance);
        }
    }

    public class ShooterFeedback extends TorqueFeedback {

        private double RPM;
        private double hoodPosition;

        @Override
        public void update() {
        }

        /**
         * @return the hoodPosition
         */
        public double getHoodPosition() {
            return hoodPosition;
        }

        /**
         * @return the RPM
         */
        public double getRPM() {
            return RPM;
        }

        /**
         * @param hoodPosition the hoodPosition to set
         */
        public void setHoodPosition(double hoodPosition) {
            this.hoodPosition = hoodPosition;
        }

        /**
         * @param RPM the RPM to set
         */
        public void setRPM(double RPM) {
            this.RPM = RPM;
        }

        public void smartDashboard() {
            SmartDashboard.putNumber("ShooterRPM", RPM);
            SmartDashboard.putNumber("HoodPosition", hoodPosition);
        }
    }

    public class ClimberFeedback extends TorqueFeedback {

        private double leftPosition;
        private double rightPosition;

        @Override
        public void update() {
        }

        /**
         * @return the leftPosition
         */
        public double getLeftPosition() {
            return leftPosition;
        }

        /**
         * @return the rightPosition
         */
        public double getRightPosition() {
            return rightPosition;
        }

        /**
         * @param leftPosition the leftPosition to set
         */
        public void setLeftPosition(double leftPosition) {
            this.leftPosition = leftPosition;
        }

        /**
         * @param rightPosition the rightPosition to set
         */
        public void setRightPosition(double rightPosition) {
            this.rightPosition = rightPosition;
        }

        @Override
        public void smartDashboard() {
            SmartDashboard.putNumber("[Climber]Position Left", leftPosition);
            SmartDashboard.putNumber("[Climber]Position Right", rightPosition);
        }
    }

    public boolean isTurretAlligned() {
        return limelightFeedback.gethOffset() < Constants.TOLERANCE_DEGREES;
    }

    public GyroFeedback getGyroFeedback() {
        return gyroFeedback;
    }

    public LimelightFeedback getLimelightFeedback() {
        return limelightFeedback;
    }

    public ShooterFeedback getShooterFeedback() {
        return shooterFeedback;
    }

    public ClimberFeedback getClimberFeedback() {
        return climberFeedback;
    }

    public static synchronized Feedback getInstance() {
        return (instance == null) ? instance = new Feedback() : instance;
    }
}