package org.texastorque.subsystems;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.Ports;
import org.texastorque.Subsystems;
import org.texastorque.torquelib.base.TorqueMode;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.base.TorqueSubsystemState;
import org.texastorque.torquelib.motors.TorqueSparkMax;
import org.texastorque.torquelib.sensors.TorqueLight;
import org.texastorque.torquelib.util.TorqueMath;

public class Turret extends TorqueSubsystem implements Subsystems {
    private static volatile Turret instance;

    private static final double MAX_VOLTS = 12, RATIO = 128.4722, KS = 0.2, ROT_CENTER = 0, ROT_BACK = 180,
                                TOLERANCE = 5, MAX_LEFT = 93, MAX_RIGHT = -93, DIRECTIONAL = 5;
    private static final boolean SHOOT_WITH_ODOMETRY = true;
    public static final Translation2d HUB_CENTER_POSITION = new Translation2d(8.2, 4.1);

    public enum TurretState implements TorqueSubsystemState {
        CENTER,
        TRACK,
        POSITIONAL,
        OFF;
    }

    private final TorqueLight camera;
    private final TorqueSparkMax rotator;

    // Needs to be played with
    private final PIDController pidController = new PIDController(0.15, 0, 0);

    private double requested = 0;
    private TurretState state = TurretState.OFF;
    private double position = 0;

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

    @Override
    public final void update(final TorqueMode mode) {
        // These should be inside tracking logic
        // if (getDegrees() > MAX_LEFT) state = TurretState.CENTER;
        // requested = formatRequested(-DIRECTIONAL);
        // else if (getDegrees() < MAX_RIGHT)
        // state = TurretState.CENTER;
        // requested = formatRequested(DIRECTIONAL);

        SmartDashboard.putBoolean("Has Targets", camera.hasTargets());

        if (climber.hasStarted()) {
            requested = calculateRequested(ROT_BACK);
        } else if (state == TurretState.OFF) {
            requested = 0;
        } else if (state == TurretState.CENTER) {
            requested = calculateRequested(ROT_CENTER);
        } else if (state == TurretState.POSITIONAL) {
            requested = calculateRequested(mode.isAuto() ? position
                                                         : TorqueMath.constrain(position, MAX_RIGHT, MAX_LEFT));
        } else if (state == TurretState.TRACK) {
            if (camera.hasTargets())
                // is this good, idk?
                requested = isLocked() ? 0 : calculateRequested(camera.getTargetYaw(), 0);
            else
                requested = calculateRequested(SHOOT_WITH_ODOMETRY ? calculateAngleWithOdometry() : ROT_CENTER);
        } else
            requested = 0;

        rotator.setVoltage(Math.signum(requested) * Math.min(Math.abs(requested), MAX_VOLTS));

        TorqueSubsystemState.logState(state);

        SmartDashboard.putNumber("Turret Req", requested);
        SmartDashboard.putNumber("Turret Deg", getDegrees());
        SmartDashboard.putBoolean("Turret Locked", isLocked());
    }

    public final double getDegrees() { return (rotator.getPosition() / RATIO * 360.) % 360; }

    private final double calculateRequested(final double requested) {
        return calculateRequested(getDegrees(), requested);
    }

    private final double calculateRequested(final double real, final double requested) {
        return formatRequested(pidController.calculate(real, requested));
    }

    private final double formatRequested(final double requested) { return KS * Math.signum(requested) + requested; }

    public final boolean isLocked() {
        SmartDashboard.putNumber("Turret Abs Yaw", Math.abs(camera.getTargetYaw()));
        return state == TurretState.POSITIONAL || state == TurretState.CENTER ||
                (camera.hasTargets() && Math.abs(camera.getTargetYaw()) < TOLERANCE);
        // For Positional:
        // return TurretState.POSITIONAL && Math.abs(getDegrees() - position) < TOLERANCE;
    }

    private final double calculateAngleWithOdometry() {
        //          v  This might want to be raw gyro angle??
        return (drivebase.getPose().getRotation().minus(
                        new Rotation2d(Math.atan2(HUB_CENTER_POSITION.getX() - drivebase.getPose().getX(),
                                                  HUB_CENTER_POSITION.getY() - drivebase.getPose().getY()))))
                .times(-1)
                .getDegrees();
    }

    public static final synchronized Turret getInstance() {
        return instance == null ? instance = new Turret() : instance;
    }
}
