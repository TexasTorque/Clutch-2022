package org.texastorque.inputs;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.photonvision.PhotonCamera;
import org.photonvision.PhotonUtils;
import org.photonvision.targeting.PhotonPipelineResult;
import org.photonvision.targeting.PhotonTrackedTarget;
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
    private TorqueLightFeedback torqueLightFeedback;
    private ShooterFeedback shooterFeedback;
    private ClimberFeedback climberFeedback;

    private Feedback() {
        gyroFeedback = new GyroFeedback();
        torqueLightFeedback = new TorqueLightFeedback();
        shooterFeedback = new ShooterFeedback();
        climberFeedback = new ClimberFeedback();
    }

    public void update() {
        gyroFeedback.update();
        torqueLightFeedback.update();
        shooterFeedback.update();
        climberFeedback.update();
    }

    public void smartDashboard() {
        gyroFeedback.smartDashboard();
        torqueLightFeedback.smartDashboard();
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

        public float getPitch() {
            return nxGyro.getPitch();
        }

        public double getVelocityX() {
            return nxGyro.getVelocityX();
        }

        public double getVelocityY() {
            return nxGyro.getVelocityY();
        }

        public double getAcceleartionX() {
            return nxGyro.getWorldLinearAccelX();
        }

        public double getAcceleartionY() {
            return nxGyro.getWorldLinearAccelY();
        }

        public double getAcceleartionZ() {
            return nxGyro.getWorldLinearAccelZ();
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

    public class TorqueLightFeedback extends TorqueFeedback {

        private final PhotonCamera torqueCam = new PhotonCamera(NetworkTableInstance.getDefault(),
                "torquecam");

        private PhotonPipelineResult result = new PhotonPipelineResult();
        private PhotonTrackedTarget bestTarget = new PhotonTrackedTarget();

        @Override
        public void update() {
            result = torqueCam.getLatestResult();
            if (result.hasTargets())
                bestTarget = result.getBestTarget();
        }

        public boolean hasTargets() {
            return result.hasTargets();
        }

        public double getTargetArea() {
            return bestTarget.getArea();
        }

        public double getTargetYaw() {
            return bestTarget.getYaw();
        }

        public double getTargetPitch() {
            return bestTarget.getPitch();
        }

        public Transform2d getCameraToTarget() {
            return bestTarget.getCameraToTarget();
        }

        public double getDistance() {
            return PhotonUtils.calculateDistanceToTargetMeters(Constants.CAMERA_HEIGHT,
                    Constants.HEIGHT_OF_VISION_STRIP_METERS, Constants.CAMERA_ANGLE.getRadians(),
                    Units.degreesToRadians(getTargetPitch()));
        }

        public void smartDashboard() {
            SmartDashboard.putBoolean("[TorqueLight]hasTarget", hasTargets());
            SmartDashboard.putNumber("[TorqueLight]targetArea", getTargetArea());
            SmartDashboard.putNumber("[TorqueLight]targetYaw", getTargetYaw());
            SmartDashboard.putNumber("[TorqueLight]targetPitch", getTargetPitch());
            SmartDashboard.putNumber("[TorqueLight]targetDistance", getDistance());

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

        private boolean leftClaw;
        private boolean rightClaw;

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

        /**
         * @param leftClaw the leftClaw to set
         */
        public void setLeftClaw(boolean leftClaw) {
            this.leftClaw = leftClaw;
        }

        /**
         * @param rightClaw the rightClaw to set
         */
        public void setRightClaw(boolean rightClaw) {
            this.rightClaw = rightClaw;
        }

        public boolean getLeftClaw() {
            return leftClaw;
        }

        public boolean getRightClaw() {
            return rightClaw;
        }

        @Override
        public void smartDashboard() {
            SmartDashboard.putNumber("[Climber]Position Left", leftPosition);
            SmartDashboard.putNumber("[Climber]Position Right", rightPosition);
            SmartDashboard.putBoolean("[Climber]Claw Left", leftClaw);
            SmartDashboard.putBoolean("[Climber]Claw Right", rightClaw);
        }
    }

    public boolean isTurretAlligned() {
        return Math.abs(torqueLightFeedback.getTargetYaw()) < Constants.TOLERANCE_DEGREES
                && torqueLightFeedback.hasTargets();
    }

    public GyroFeedback getGyroFeedback() {
        return gyroFeedback;
    }

    public TorqueLightFeedback getTorquelightFeedback() {
        return torqueLightFeedback;
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