package org.texastorque.auto.commands;

import com.pathplanner.lib.PathPlanner;
import com.pathplanner.lib.PathPlannerTrajectory;
import com.pathplanner.lib.PathPlannerTrajectory.PathPlannerState;

import org.texastorque.subsystems.Drivebase;
import org.texastorque.subsystems.Drivebase.DrivebaseState;
import org.texastorque.torquelib.auto.TorqueCommand;
import org.texastorque.torquelib.sensors.TorqueNavXGyro;

import edu.wpi.first.math.controller.HolonomicDriveController;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.Timer;

public final class FollowPathPlanner extends TorqueCommand {
    private final PIDController xController = new PIDController(1, 0, 0);
    private final PIDController yController = new PIDController(1, 0, 0);
    private final ProfiledPIDController thetaController = new ProfiledPIDController(4, 0, 0, 
            new TrapezoidProfile.Constraints(6 * Math.PI, 6 * Math.PI));
    private final HolonomicDriveController controller = new HolonomicDriveController(xController, yController, thetaController);

    private final PathPlannerTrajectory trajectory;
    private final Timer timer = new Timer();
    private final boolean resetOdometry;

    public FollowPathPlanner(final String name) {
        thetaController.enableContinuousInput(Math.toRadians(-180), Math.toRadians(180));
        trajectory = PathPlanner.loadPath(name, Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED, Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED);
        resetOdometry = false;
    }

    public FollowPathPlanner(final String name, final boolean reset) {
        thetaController.enableContinuousInput(Math.toRadians(-180), Math.toRadians(180));
        trajectory = PathPlanner.loadPath(name, Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED, Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED);
        this.resetOdometry = reset;
    }

    public FollowPathPlanner(final String name, final boolean reset,
            final double maxSpeed, final double maxAcceleration) {
        thetaController.enableContinuousInput(Math.toRadians(-180), Math.toRadians(180));
        trajectory = PathPlanner.loadPath(name, maxSpeed, maxAcceleration);
        this.resetOdometry = reset;
    } 

    @Override
    protected void init() {
        timer.reset();
        timer.start();
        if (!resetOdometry) return;

        TorqueNavXGyro.getInstance().setAngleOffset(360 - trajectory.getInitialPose().getRotation().getDegrees());
        Drivebase.getInstance().getPoseEstimator().resetPosition(trajectory.getInitialState().poseMeters, 
                trajectory.getInitialState().holonomicRotation);
        Drivebase.getInstance().setState(DrivebaseState.ROBOT_RELATIVE);
    }

    @Override
    protected void continuous() {
        final PathPlannerState current = (PathPlannerState) trajectory.sample(timer.get());
        ChassisSpeeds speeds = controller.calculate(
                Drivebase.getInstance().getPoseEstimator().getEstimatedPosition(),
                current, current.holonomicRotation);
        speeds = new ChassisSpeeds(-speeds.vxMetersPerSecond, speeds.vyMetersPerSecond, 
                speeds.omegaRadiansPerSecond);
        Drivebase.getInstance().setSpeeds(speeds);
    }

    @Override
    protected boolean endCondition() {
        return timer.hasElapsed(trajectory.getTotalTimeSeconds());
    }

    @Override
    protected void end() {
        timer.stop();
        Drivebase.getInstance().reset();
    }
}