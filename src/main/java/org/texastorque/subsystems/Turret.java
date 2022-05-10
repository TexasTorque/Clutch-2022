package org.texastorque.subsystems;

import org.texastorque.Ports;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.motors.TorqueSparkMax;
import org.texastorque.torquelib.sensors.TorqueLight;
import org.texastorque.torquelib.util.TorqueMathUtil;

import edu.wpi.first.math.controller.PIDController;

public class Turret extends TorqueSubsystem {
    private static volatile Turret instance;

    private static final double MAX_VOLTS = 11;
    private static final double RATIO = 128.4722;
    private static final double KS = 0.14066;

    private static final double ROT_CENTER = 0;
    private static final double ROT_BACK = 180;
    private static final double TOLERANCE = 1.5;
    private static final double MAX_LEFT = 93;
    private static final double MAX_RIGHT = -93;
    private static final double DIRECTIONAL = 5;


    public enum TurretState implements TorqueSubsystemState {
        CENTER, TRACK, POSITIONAL, OFF;
    }

    private final TorqueLight camera;
    private final TorqueSparkMax rotator;

    private final PIDController pidController = new PIDController(0.1039, 0, 0);

    private double requested = 0;
    private TurretState state = TurretState.OFF;
    private double position = 0;

    private Turret() {
        rotator = new TorqueSparkMax(Ports.TURRET);
        camera = Shooter.getInstance().getCamera();
    }

    public void setState(final TurretState state) {
        this.state = state;
    }

    public void setPosition(final double position) {
        this.position = position;
    }

    @Override
    public void initTeleop() {
        state = TurretState.OFF;
    }

    @Override
    public void updateTeleop() {
        if (state == TurretState.OFF)
            requested = 0;
        else if (state == TurretState.CENTER)
            requested = calculateRequested(ROT_CENTER);
        else if (state == TurretState.POSITIONAL)
            requested = calculateRequested(TorqueMathUtil.constrain(position, MAX_RIGHT, MAX_LEFT));
        else if (state == TurretState.TRACK) {
            if (camera.hasTargets())
                requested = isLocked() ? 0 : calculateRequested(camera.getTargetYaw(), 0);
            else
                requested = calculateRequested(ROT_CENTER);
        } else
            requested = 0;

        if (getDegrees() > MAX_LEFT)
            requested = formatRequested(DIRECTIONAL);
        else if (getDegrees() < MAX_RIGHT)
            requested = formatRequested(-DIRECTIONAL);

        rotator.setVoltage(Math.signum(requested) * Math.min(Math.abs(requested), MAX_VOLTS));
    }

    @Override
    public void initAuto() {
        state = TurretState.OFF;
    }

    @Override
    public void updateAuto() {
        updateTeleop();
    }

    public double getDegrees() {
        return (rotator.getPosition() / RATIO * 360.) % 360;
    }

    private double calculateRequested(final double requested) {
        return calculateRequested(getDegrees(), requested);
    }

    private double calculateRequested(final double real, final double requested) {
        return formatRequested(pidController.calculate(real, requested));
    }

    private double formatRequested(final double requested) {
        return KS * Math.signum(requested) + requested;
    }

    public boolean isLocked() {
        return camera.hasTargets() && Math.abs(camera.getTargetYaw()) < TOLERANCE;
    }

    public static synchronized Turret getInstance() {
        return instance == null ? instance = new Turret() : instance;
    }
}
