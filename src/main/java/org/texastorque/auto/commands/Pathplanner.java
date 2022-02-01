package org.texastorque.auto.commands;

import com.pathplanner.lib.PathPlanner;
import com.pathplanner.lib.PathPlannerTrajectory;
import com.pathplanner.lib.PathPlannerTrajectory.PathPlannerState;

import org.texastorque.auto.commands.*;
import org.texastorque.constants.Constants;
import org.texastorque.inputs.AutoInput;
import org.texastorque.inputs.Feedback;
import org.texastorque.subsystems.*;
import org.texastorque.torquelib.auto.TorqueCommand;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.math.controller.HolonomicDriveController;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.math.trajectory.TrapezoidProfile;


public class Pathplanner extends TorqueCommand{

    private PathPlannerTrajectory trajectory;
        private final PIDController xController = new PIDController(1.5, 0, 0);
        private final PIDController yController = new PIDController(1.5, 0, 0);
        private final TrapezoidProfile.Constraints tConstraints = new TrapezoidProfile.Constraints(
                        Constants.MAX_ANGULAR_SPEED, Constants.MAX_ANGULAR_ACCELERATION);
        private final ProfiledPIDController thetaController = new ProfiledPIDController(4, 0, 0, tConstraints);
        private final HolonomicDriveController hController = new HolonomicDriveController(xController, yController,
                        thetaController);
        private final Timer timer = new Timer();

        public Pathplanner(String name) {
                thetaController.enableContinuousInput(Math.toRadians(-180), Math.toRadians(180));
                trajectory = PathPlanner.loadPath(name, Constants.TOP_SPEED_METERS, Constants.TOP_ACCELERATION_METERS);
        }

        @Override
        protected void init() {
                System.out.println("Initializing Pathweaver...");
                timer.reset();
                timer.start();

                Drivebase.getInstance().odometry.resetPosition(trajectory.getInitialPose(),
                                trajectory.getInitialPose().getRotation());

                System.out.println(
                                "My initial ;): " + Drivebase.getInstance().odometry.getPoseMeters().getRotation()
                                                .getDegrees());
                System.out.println(
                                "My initial real ;): " + Feedback.getInstance().getGyroFeedback().getRotation2d()
                                                .getDegrees());
                System.out.println("Wanted initial:" + trajectory.getInitialPose().getRotation().getDegrees());
        }

        @Override
        protected void continuous() {
                double time = timer.get();
                PathPlannerState currentTrajectory = (PathPlannerState) trajectory.sample(time);

                SmartDashboard.putNumber("[Want]X", currentTrajectory.poseMeters.getX());
                SmartDashboard.putNumber("[Want]Y", currentTrajectory.poseMeters.getY());
                SmartDashboard.putNumber("[Want]Rot", currentTrajectory.poseMeters.getRotation().getDegrees());

                ChassisSpeeds chassisSpeeds = hController.calculate(Drivebase.getInstance().odometry.getPoseMeters(),
                                currentTrajectory, currentTrajectory.holonomicRotation);
                chassisSpeeds = new ChassisSpeeds(-chassisSpeeds.vxMetersPerSecond, chassisSpeeds.vyMetersPerSecond,
                                chassisSpeeds.omegaRadiansPerSecond);
                SwerveModuleState[] states = Drivebase.getInstance().kinematics.toSwerveModuleStates(chassisSpeeds);

                SwerveDriveKinematics.desaturateWheelSpeeds(states, Constants.TOP_SPEED_METERS);

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
                AutoInput.getInstance()
                                .setDriveStates(Drivebase.getInstance().kinematics
                                                .toSwerveModuleStates(new ChassisSpeeds(0, 0, 0)));
                System.out.println("Pathweaver done :). Have a great day!");
        }
    
}
