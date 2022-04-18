package org.texastorque.auto.commands;

import com.pathplanner.lib.PathPlanner;
import com.pathplanner.lib.PathPlannerTrajectory;
import com.pathplanner.lib.PathPlannerTrajectory.PathPlannerState;
import edu.wpi.first.math.controller.HolonomicDriveController;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.constants.Constants;
import org.texastorque.inputs.AutoInput;
import org.texastorque.inputs.Feedback;
import org.texastorque.subsystems.*;
import org.texastorque.torquelib.auto.TorqueCommand;

public class Pathplanner extends TorqueCommand {
        private PathPlannerTrajectory trajectory;

        private final PIDController xController = new PIDController(
                        Constants.PATH_PLANNER_X_P, Constants.PATH_PLANNER_X_I, Constants.PATH_PLANNER_X_D);
        private final PIDController yController = new PIDController(
                        Constants.PATH_PLANNER_Y_P, Constants.PATH_PLANNER_Y_I, Constants.PATH_PLANNER_Y_D);
        private final TrapezoidProfile.Constraints tConstraints = new TrapezoidProfile.Constraints(
                        Constants.MAX_ANGULAR_SPEED, Constants.MAX_ANGULAR_ACCELERATION);
        private final ProfiledPIDController thetaController = new ProfiledPIDController(Constants.PATH_PLANNER_R_P,
                        Constants.PATH_PANNER_R_I, Constants.PATH_PLANNER_R_D, tConstraints);
        private final HolonomicDriveController hController = new HolonomicDriveController(xController, yController,
                        thetaController);

        private final Timer timer = new Timer();

        private boolean resetOdometry = true;
        private String name;
        private double MAX_SPEED = Constants.DRIVE_MAX_SPEED_METERS;
        private double MAX_ACCELERATION = Constants.DRIVE_MAX_ACCELERATION_METERS;

        public Pathplanner(String name) {
                thetaController.enableContinuousInput(Math.toRadians(-180), Math.toRadians(180));
                trajectory = PathPlanner.loadPath(name, MAX_SPEED,
                                MAX_ACCELERATION);
                this.name = name;
        }

        public Pathplanner(String name, boolean resetOdometry) {
                this(name);
                this.resetOdometry = resetOdometry;
        }

        public Pathplanner(String name, boolean resetOdometry, double maxSpeed, double maxAcceleration) {
                this(name);
                this.resetOdometry = resetOdometry;
                this.MAX_SPEED = maxSpeed;
                this.MAX_ACCELERATION = maxAcceleration;
                trajectory = PathPlanner.loadPath(name, MAX_SPEED,
                                MAX_ACCELERATION);
        }

        @Override
        protected void init() {
                System.out.println("Initializing Pathplanner for " + name + "...");
                timer.reset();
                timer.start();

                if (resetOdometry) {
                        float offset = 360f - (float) trajectory.getInitialPose().getRotation().getDegrees();

                        Feedback.getInstance().getGyroFeedback().setAngleOffset(offset);
                        Drivebase.getInstance().odometry.resetPosition(trajectory.getInitialState().poseMeters,
                                        trajectory.getInitialState().holonomicRotation);

                        System.out.println("My initial: " + Drivebase.getInstance().odometry.getPoseMeters()
                                        .getRotation()
                                        .getDegrees());

                        System.out.println("My initial real: " + Feedback.getInstance()
                                        .getGyroFeedback()
                                        .getRotation2d()
                                        .getDegrees());

                        System.out.println("Wanted initial:" + trajectory.getInitialState().holonomicRotation);
                }
        }

        @Override
        protected void continuous() {
                double time = timer.get();
                PathPlannerState currentTrajectory = (PathPlannerState) trajectory.sample(time);

                SmartDashboard.putNumber("[Want]X", currentTrajectory.poseMeters.getX());
                SmartDashboard.putNumber("[Want]Y", currentTrajectory.poseMeters.getY());
                SmartDashboard.putNumber("[Want]Rot",
                                currentTrajectory.poseMeters.getRotation().getDegrees());

                ChassisSpeeds chassisSpeeds = hController.calculate(
                                Drivebase.getInstance().odometry.getPoseMeters(), currentTrajectory,
                                currentTrajectory.holonomicRotation);
                chassisSpeeds = new ChassisSpeeds(-chassisSpeeds.vxMetersPerSecond,
                                chassisSpeeds.vyMetersPerSecond,
                                chassisSpeeds.omegaRadiansPerSecond);
                SwerveModuleState[] states = Drivebase.getInstance().kinematics.toSwerveModuleStates(
                                chassisSpeeds);

                SwerveDriveKinematics.desaturateWheelSpeeds(states, Constants.DRIVE_MAX_SPEED_METERS);

                // Set the states
                AutoInput.getInstance().setDriveStates(states);
        }

        @Override
        protected boolean endCondition() {
                return timer.hasElapsed(trajectory.getTotalTimeSeconds());
        }

        @Override
        protected void end() {
                timer.stop();
                AutoInput.getInstance().setDriveStates(Drivebase.getInstance().kinematics
                                .toSwerveModuleStates(new ChassisSpeeds(0, 0, 0)));

                System.out.println("Pathplanner done: Have a great day!");
        }
}