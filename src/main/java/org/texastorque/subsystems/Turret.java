/**
 * Copyright 2022 Texas Torque.
 *
 * This file is part of Clutch-2022, which is not licensed for distribution.
 * For more details, see ./license.txt or write <jus@gtsbr.org>.
 */
package org.texastorque.subsystems;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.Ports;
import org.texastorque.Subsystems;
import org.texastorque.torquelib.base.TorqueMode;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.base.TorqueSubsystemState;
import org.texastorque.torquelib.control.TorquePID;
import org.texastorque.torquelib.control.TorqueRollingMedian;
import org.texastorque.torquelib.motors.TorqueSparkMax;
import org.texastorque.torquelib.sensors.TorqueLight;
import org.texastorque.torquelib.util.TorqueMath;

public final class Turret extends TorqueSubsystem implements Subsystems {
    private static volatile Turret instance;

    private static final double MAX_VOLTS = 12, RATIO = 128.4722, ROT_CENTER = 0, ROT_BACK = 180, TOLERANCE = 4,
                                MAX_LEFT = 93, MAX_RIGHT = -93, DIRECTIONAL = 5,
                                // KS = .2;
            KS = 0.14066;
    private static final boolean SHOOT_WITH_ODOMETRY = false;
    public static final Translation2d HUB_CENTER_POSITION = new Translation2d(8.2, 4.1);

    public enum TurretState implements TorqueSubsystemState {
        CENTER,
        TRACK,
        POSITIONAL,
        OFF;
    }

    private final TorqueLight camera;
    private final TorqueSparkMax rotator;

    private final TorquePID pid = TorquePID.create(.1039).build();

    private double requested = 0, position = 0, angleToHub = 0;
    private TurretState state = TurretState.OFF;

    public final void setAngleToHub(final double angleToHub) {
        this.angleToHub = angleToHub;
    }

    private Turret() {
        rotator = new TorqueSparkMax(Ports.TURRET);
        camera = shooter.getCamera();
        rotator.setEncoderZero(RATIO * -90 / 360);
    }

    public final void setState(final TurretState state) { this.state = state; }

    public final void setPosition(final double position) { this.position = position; }

    @Override
    public final void initialize(final TorqueMode mode) {
        state = TurretState.OFF;
    }

    private boolean isAuto = false;

    @Override
    public final void update(final TorqueMode mode) {
        isAuto = mode.isAuto();
        calculateAngleWithOdometry();
        // These should be inside tracking logic
        // if (getDegrees() > MAX_LEFT) state = TurretState.CENTER;
        // requested = formatRequested(-DIRECTIONAL);
        // else if (getDegrees() < MAX_RIGHT)
        // state = TurretState.CENTER;
        // requested = formatRequested(DIRECTIONAL);

        // final double angleDifference = drivebase.getPose().getRotation()
        SmartDashboard.putBoolean("Has Targets", camera.hasTargets());

        if (climber.hasStarted()) {
            requested = calculateRequested(ROT_BACK);
        } else if (state == TurretState.OFF) {
            requested = 0;
        } else if (state == TurretState.CENTER) {
            requested = calculateRequested(ROT_CENTER);
        } else if (state == TurretState.POSITIONAL) {
            requested =
                    calculateRequested(mode.isAuto() ? position : TorqueMath.constrain(position, MAX_RIGHT, MAX_LEFT));
        } else if (state == TurretState.TRACK) {
            if (camera.hasTargets())
                // is this good, idk?
                requested = isLocked() ? 0 : calculateRequested(camera.getTargetYaw(), 0);
            // requested = isLocked() ? 0 : calculateRequested(yawFilter.calculate(camera.getTargetYaw()), 0);
            else if (mode.isAuto())
                requested = 0;
            // requested = 0;
            // requested = calculateRequested(position); // center at the last told position
            else
                requested = calculateRequested(SHOOT_WITH_ODOMETRY ? calculateAngleWithOdometry() : ROT_CENTER);
        } else
            requested = 0;

        rotator.setVoltage(Math.signum(requested) * Math.min(Math.abs(requested), MAX_VOLTS));

        TorqueSubsystemState.logState(state);

        SmartDashboard.putNumber("Turret Req", requested);
        SmartDashboard.putNumber("Turret Deg", getDegrees());
        SmartDashboard.putNumber("Turret Delta", requested - getDegrees());
        SmartDashboard.putBoolean("Turret Locked", isLocked());
    }

    public final double getDegrees() { return (rotator.getPosition() / RATIO * 360.) % 360; }

    private final double calculateRequested(final double requested) {
        return calculateRequested(getDegrees(), requested);
    }

    private final double calculateRequested(final double real, final double requested) {
        return formatRequested(pid.calculate(real, requested));
    }

    private final double formatRequested(final double requested) { return KS * Math.signum(requested) + requested; }

    public final boolean isLocked() {
        SmartDashboard.putNumber("Turret Abs Yaw", Math.abs(camera.getTargetYaw()));
        if (state == TurretState.TRACK && isAuto) return true;
        return state == TurretState.POSITIONAL || state == TurretState.CENTER ||
                (camera.hasTargets() && Math.abs(camera.getTargetYaw()) < TOLERANCE);
        // For Positional:
        // return TurretState.POSITIONAL && Math.abs(getDegrees() - position) < TOLERANCE;
    }

    /**
     * Calculates the angle of the turret to the hub based on the
     * current robot odometry position.
     *
     * @return Degrees to hub.
     */
    public final double calculateAngleWithOdometry() {
        final Pose2d pose = drivebase.getPose();
        SmartDashboard.putString("_P", pose.toString());
        final double x = Shooter.HUB_CENTER_POSITION.getX() - pose.getX();
        final double y = Shooter.HUB_CENTER_POSITION.getY() - pose.getY();
        final Rotation2d angle = new Rotation2d(Math.atan2(y, x));
        final Rotation2d combined = pose.getRotation().minus(angle);
        SmartDashboard.putNumber("_A", combined.getDegrees());
        SmartDashboard.putNumber("_B", combined.times(-1).getDegrees());
        return combined.getDegrees();
    }

    public static final synchronized Turret getInstance() {
        return instance == null ? instance = new Turret() : instance;
    }
}
