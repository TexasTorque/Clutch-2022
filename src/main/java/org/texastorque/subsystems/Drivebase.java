package org.texastorque.subsystems;

import edu.wpi.first.math.MatBuilder;
import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.Nat;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.constants.Constants;
import org.texastorque.constants.Ports;
import org.texastorque.inputs.AutoInput;
import org.texastorque.inputs.Feedback;
import org.texastorque.inputs.Input;
import org.texastorque.modules.SwerveOdometry;
import org.texastorque.modules.SwerveWheel;
import org.texastorque.torquelib.base.TorqueSubsystem;

public class Drivebase extends TorqueSubsystem {
        private static volatile Drivebase instance;

        private final Feedback feedback = Feedback.getInstance();
        private final Input input = Input.getInstance();

        /**
         * Locations of wheel modules
         */
        private final Translation2d locationBackLeft = new Translation2d(
                        Constants.DISTANCE_TO_CENTER_X, -Constants.DISTANCE_TO_CENTER_Y);
        private final Translation2d locationBackRight = new Translation2d(
                        Constants.DISTANCE_TO_CENTER_X, Constants.DISTANCE_TO_CENTER_Y);
        private final Translation2d locationFrontLeft = new Translation2d(
                        -Constants.DISTANCE_TO_CENTER_X, -Constants.DISTANCE_TO_CENTER_Y);
        private final Translation2d locationFrontRight = new Translation2d(
                        -Constants.DISTANCE_TO_CENTER_X, Constants.DISTANCE_TO_CENTER_Y);

        /**
         * Kinematics
         */
        public final SwerveDriveKinematics kinematics;

        /**
         * Pose estimator
         */
        public final SwerveDrivePoseEstimator poseEstimator;
        private final Field2d field2d;

        /**
         * Modules
         */
        private SwerveWheel backLeft;
        private SwerveWheel backRight;
        private SwerveWheel frontLeft;
        private SwerveWheel frontRight;

        /**
         * Variables
         */
        private double xSpeed = 0;
        private double ySpeed = 0;
        private double rotation = 0;
        private boolean fieldRelative = false;
        private SwerveModuleState[] swerveModuleStates;
        private Matrix<N3, N1> stateStds = new MatBuilder<>(Nat.N3(), Nat.N1()).fill(0.4, 0.4, .9);
        private Matrix<N1, N1> localStds = new MatBuilder<>(Nat.N1(), Nat.N1()).fill(.3);
        private Matrix<N3, N1> visionStds = new MatBuilder<>(Nat.N3(), Nat.N1()).fill(.5, .5, 1);

        private Drivebase() {
                backLeft = new SwerveWheel(0, Ports.DRIVE_TRANS_LEFT_BACK,
                                Ports.DRIVE_ROT_LEFT_BACK);
                backRight = new SwerveWheel(1, Ports.DRIVE_TRANS_RIGHT_BACK,
                                Ports.DRIVE_ROT_RIGHT_BACK);
                frontLeft = new SwerveWheel(2, Ports.DRIVE_TRANS_LEFT_FRONT,
                                Ports.DRIVE_ROT_LEFT_FRONT);
                frontRight = new SwerveWheel(3, Ports.DRIVE_TRANS_RIGHT_FRONT,
                                Ports.DRIVE_ROT_RIGHT_FRONT);

                kinematics = new SwerveDriveKinematics(locationBackLeft, locationBackRight,
                                locationFrontLeft, locationFrontRight);

                poseEstimator = new SwerveDrivePoseEstimator(Feedback.getInstance().getGyroFeedback().getRotation2d(),
                                new Pose2d(), kinematics, stateStds,
                                localStds, visionStds);
                field2d = new Field2d();
        }

        private void reset() {
                xSpeed = 0;
                ySpeed = 0;
                rotation = 0;
                fieldRelative = false;
        }

        @Override
        public void initTeleop() {
                reset();
        }

        @Override
        public void initAuto() {
                reset();
        }

        @Override
        public void updateTeleop() {
                xSpeed = input.getDrivebaseTranslationInput().getXSpeed();
                ySpeed = input.getDrivebaseTranslationInput().getYSpeed();
                rotation = input.getDrivebaseRotationInput().getRot();
                fieldRelative = true;

                swerveModuleStates = kinematics.toSwerveModuleStates(
                                fieldRelative ? ChassisSpeeds.fromFieldRelativeSpeeds(
                                                xSpeed, ySpeed, rotation,
                                                feedback.getGyroFeedback().getRotation2d())
                                                : new ChassisSpeeds(xSpeed, ySpeed, rotation));

                SwerveDriveKinematics.desaturateWheelSpeeds(
                                swerveModuleStates, Constants.DRIVE_MAX_SPEED_METERS);
        }

        @Override
        public void updateAuto() {
                swerveModuleStates = AutoInput.getInstance().getDriveStates();
                outputAuto();
        }

        @Override
        public void output() {
                if (Input.getInstance().getShooterInput().xFactor()) {
                        swerveModuleStates[0].angle = Rotation2d.fromDegrees(135);
                        swerveModuleStates[1].angle = Rotation2d.fromDegrees(45);
                        swerveModuleStates[2].angle = Rotation2d.fromDegrees(45);
                        swerveModuleStates[3].angle = Rotation2d.fromDegrees(135);
                }

                frontLeft.setDesiredState(swerveModuleStates[0]);
                frontRight.setDesiredState(swerveModuleStates[1]);
                backLeft.setDesiredState(swerveModuleStates[2]);
                backRight.setDesiredState(swerveModuleStates[3]);
        }

        public void outputAuto() {
                frontLeft.setDesiredState(swerveModuleStates[0]);
                frontRight.setDesiredState(swerveModuleStates[1]);
                backLeft.setDesiredState(swerveModuleStates[2]);
                backRight.setDesiredState(swerveModuleStates[3]);
        }

        @Override
        public void updateFeedbackTeleop() {
                poseEstimator.update(feedback.getGyroFeedback().getRotation2d().times(-1),
                                frontLeft.getState(), frontRight.getState(),
                                backLeft.getState(), backRight.getState());
                field2d.setRobotPose(poseEstimator.getEstimatedPosition());
                SmartDashboard.putData("Field", field2d);
                SmartDashboard.putNumber("[Real]X", getEstimatedPosition().getX());
                SmartDashboard.putNumber("[Real]Y", getEstimatedPosition().getY());
                SmartDashboard.putNumber("[Real]Rot", getEstimatedPosition().getRotation().getDegrees());
        }

        public Pose2d getEstimatedPosition() {
                return poseEstimator.getEstimatedPosition();
        }

        public void updateWithVision(Pose2d visionRobotPoseMeters) {
                try {
                        if (choleskyErrorUnlikely(visionRobotPoseMeters)) {
                                poseEstimator.addVisionMeasurement(visionRobotPoseMeters,
                                                Timer.getFPGATimestamp()
                                                                - Feedback.getInstance().getTorquelightFeedback()
                                                                                .getLatency()
                                                                - .0011);
                        }
                } catch (Exception e) {
                        System.out.println(
                                        "Failed to add vision measurement to pose estimator. Likely due to Cholesky decomposition failing due to it not being the sqrt method. Full details on the error: \n"
                                                        + e.getMessage());
                }
        }

        /**
         * In a nutshell, we want to prevent cholesky decomposition failures by limiting
         * the addition of vision to joint probabilistic space (+- 2 std)
         * 
         * @return if it is highly likely that cholesky decomposition will pass
         */
        private boolean choleskyErrorUnlikely(Pose2d visionRobotPoseMeters) {
                Pose2d estimatedPosition = poseEstimator.getEstimatedPosition();
                /**
                 * +
                 * + +
                 * + +
                 * 
                 */
                double diff_x = Math.abs(estimatedPosition.getX() - visionRobotPoseMeters.getX());
                double diff_y = Math.abs(estimatedPosition.getY() - visionRobotPoseMeters.getY());
                return (diff_x < visionStds.get(1, 1) * 2) && (diff_y < visionStds.get(2, 1) * 2);
        }

        @Override
        public void updateFeedbackAuto() {
                updateFeedbackTeleop();
        }

        @Override
        public void updateSmartDashboard() {
        }

        public static synchronized Drivebase getInstance() {
                return (instance == null) ? instance = new Drivebase() : instance;
        }
}
