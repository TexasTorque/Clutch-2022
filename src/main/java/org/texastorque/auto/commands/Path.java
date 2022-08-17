/**
 * Copyright 2022 Texas Torque.
 * 
 * This file is part of Clutch-2022, which is not licensed for distribution.
 * For more details, see ./license.txt or write <jus@gtsbr.org>.
 */
package org.texastorque.auto.commands;

import com.pathplanner.lib.PathPlanner;
import com.pathplanner.lib.PathPlannerTrajectory;
import com.pathplanner.lib.PathPlannerTrajectory.PathPlannerState;
import edu.wpi.first.math.controller.HolonomicDriveController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.Timer;
import org.texastorque.Subsystems;
import org.texastorque.subsystems.Drivebase;
import org.texastorque.torquelib.auto.TorqueCommand;
import org.texastorque.torquelib.control.TorquePID;
import org.texastorque.torquelib.sensors.TorqueNavXGyro;

public final class Path extends TorqueCommand implements Subsystems {
    private final TorquePID xController = TorquePID.create(1).build();
    private final TorquePID yController = TorquePID.create(1).build();
    private final ProfiledPIDController thetaController =
            new ProfiledPIDController(4, 0, 0, new TrapezoidProfile.Constraints(6 * Math.PI, 6 * Math.PI));
    private final HolonomicDriveController controller =
            new HolonomicDriveController(xController, yController, thetaController);

    private final PathPlannerTrajectory trajectory;
    private final Timer timer = new Timer();
    private final boolean resetOdometry;

    public Path(final String name) {
        thetaController.enableContinuousInput(Math.toRadians(-180), Math.toRadians(180));
        trajectory = PathPlanner.loadPath(name, Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED,
                                          Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED);
        resetOdometry = false;
    }

    public Path(final String name, final boolean reset) {
        thetaController.enableContinuousInput(Math.toRadians(-180), Math.toRadians(180));
        trajectory = PathPlanner.loadPath(name, Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED,
                                          Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED);
        this.resetOdometry = reset;
    }

    public Path(final String name, final boolean reset, final double maxSpeed, final double maxAcceleration) {
        thetaController.enableContinuousInput(Math.toRadians(-180), Math.toRadians(180));
        trajectory = PathPlanner.loadPath(name, maxSpeed, maxAcceleration);
        this.resetOdometry = reset;
    }

    @Override
    protected final void init() {
        timer.reset();
        timer.start();
        if (!resetOdometry) return;

        TorqueNavXGyro.getInstance().setAngleOffset(360 - trajectory.getInitialPose().getRotation().getDegrees());
        drivebase.getOdometry().resetPosition(trajectory.getInitialState().poseMeters,
                                              trajectory.getInitialState().holonomicRotation);
    }

    @Override
    protected final void continuous() {
        final PathPlannerState current = (PathPlannerState)trajectory.sample(timer.get());
        ChassisSpeeds speeds =
                controller.calculate(drivebase.getOdometry().getPoseMeters(), current, current.holonomicRotation);
        speeds = new ChassisSpeeds(-speeds.vxMetersPerSecond, speeds.vyMetersPerSecond, speeds.omegaRadiansPerSecond);
        drivebase.setSpeeds(speeds);
    }

    @Override
    protected final boolean endCondition() {
        return timer.hasElapsed(trajectory.getTotalTimeSeconds());
    }

    @Override
    protected final void end() {
        timer.stop();
        drivebase.reset();
    }
}