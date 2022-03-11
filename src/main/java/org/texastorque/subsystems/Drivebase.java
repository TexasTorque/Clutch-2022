package org.texastorque.subsystems;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
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
         * Odometry
         */
        public final SwerveOdometry odometry;

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

        private Drivebase() {
                backLeft = new SwerveWheel(0, Ports.DRIVE_TRANS_LEFT_BACK,
                                Ports.DRIVE_ROT_LEFT_BACK,
                                new PIDController(Constants.DRIVE_ROT_LEFT_BACK_Kp, Constants.DRIVE_ROT_LEFT_BACK_Ki,
                                                Constants.DRIVE_ROT_LEFT_BACK_Kd),
                                Constants.DRIVE_ROT_LEFT_BACK_Ks);
                backRight = new SwerveWheel(1, Ports.DRIVE_TRANS_RIGHT_BACK,
                                Ports.DRIVE_ROT_RIGHT_BACK,
                                new PIDController(Constants.DRIVE_ROT_RIGHT_BACK_Kp, Constants.DRIVE_ROT_RIGHT_BACK_Ki,
                                                Constants.DRIVE_ROT_RIGHT_BACK_Kd),
                                Constants.DRIVE_ROT_RIGHT_BACK_Ks);
                frontLeft = new SwerveWheel(2, Ports.DRIVE_TRANS_LEFT_FRONT,
                                Ports.DRIVE_ROT_LEFT_FRONT,
                                new PIDController(Constants.DRIVE_ROT_LEFT_FRONT_Kp, Constants.DRIVE_ROT_LEFT_FRONT_Ki,
                                                Constants.DRIVE_ROT_LEFT_FRONT_Kd),
                                Constants.DRIVE_ROT_LEFT_FRONT_Ks);
                frontRight = new SwerveWheel(3, Ports.DRIVE_TRANS_RIGHT_FRONT,
                                Ports.DRIVE_ROT_RIGHT_FRONT,
                                new PIDController(Constants.DRIVE_ROT_RIGHT_FRONT_Kp,
                                                Constants.DRIVE_ROT_RIGHT_FRONT_Ki,
                                                Constants.DRIVE_ROT_RIGHT_FRONT_Kd),
                                Constants.DRIVE_ROT_RIGHT_FRONT_Ks);

                kinematics = new SwerveDriveKinematics(locationBackLeft, locationBackRight,
                                locationFrontLeft, locationFrontRight);

                odometry = new SwerveOdometry(
                                kinematics, feedback.getGyroFeedback().getRotation2d());
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
                odometry.update(feedback.getGyroFeedback().getRotation2d().times(-1),
                                frontLeft.getState(), frontRight.getState(),
                                backLeft.getState(), backRight.getState());
                SmartDashboard.putNumber("[Real]X", odometry.getPoseMeters().getX());
                SmartDashboard.putNumber("[Real]Y", odometry.getPoseMeters().getY());
                SmartDashboard.putNumber(
                                "[Real]Rot", odometry.getPoseMeters().getRotation().getDegrees());
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
