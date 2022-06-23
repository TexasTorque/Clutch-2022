package org.texastorque.subsystems;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.Ports;
import org.texastorque.Subsystems;
import org.texastorque.torquelib.base.TorqueMode;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.base.TorqueSubsystemState;
import org.texastorque.torquelib.motors.TorqueSparkMax;
import org.texastorque.torquelib.util.KPID;

/**
 * The intake subsystem.
 *
 * @author Jack Pittenger
 * @author Justus Languell
 */
public final class Intake extends TorqueSubsystem implements Subsystems {
    private static volatile Intake instance;

    public enum IntakeDirection implements TorqueSubsystemState {
        INTAKE(-1),
        STOPPED(0),
        OUTAKE(.3);

        private final double direction;

        IntakeDirection(final double direction) { this.direction = direction; }

        public final double getDirection() { return direction; }
    }

    public static enum IntakePosition implements TorqueSubsystemState {
        UP(0),
        DOWN(-5.64);

        private final double position;

        IntakePosition(final double position) { this.position = position; }

        public final double getPosition() { return position; }
    }

    private static final double ROTARY_MIN_SPEED = -.35, ROTARY_MAX_SPEED = 1, ROLLER_MIN_SPEED = .8,
                                ROLLER_MAX_SPEED = 1;

    private IntakeDirection direction = IntakeDirection.STOPPED;
    private IntakePosition position = IntakePosition.UP;

    private final TorqueSparkMax rotary, rollers;

    private Intake() {
        rotary = new TorqueSparkMax(Ports.INTAKE.ROTARY);
        rotary.configurePID(new KPID(0.1, 0.00005, .00002, 0, ROTARY_MIN_SPEED, ROTARY_MAX_SPEED));
        rotary.configurePositionalCANFrame();
        rotary.burnFlash();

        rollers = new TorqueSparkMax(Ports.INTAKE.ROLLER);
        rollers.burnFlash();
    }

    public final void setState(final IntakeDirection direction, final IntakePosition position) {
        this.direction = direction;
        this.position = position;
    }

    public final void setDirection(final IntakeDirection direction) { this.direction = direction; }

    public final void setPosition(final IntakePosition position) { this.position = position; }

    public final IntakeDirection getDirection() { return direction; }

    public final IntakePosition getPosition() { return position; }

    public final boolean isIntaking() { return direction == IntakeDirection.INTAKE && position == IntakePosition.DOWN; }

    @Override
    public final void initialize(final TorqueMode mode) {}

    @Override
    public final void update(final TorqueMode mode) {
        rotary.setPosition(position.getPosition());

        rollers.setPercent(direction.getDirection());

        TorqueSubsystemState.logState(direction);
        TorqueSubsystemState.logState(position);

        SmartDashboard.putNumber("Rotary Position", rotary.getPosition());
    }

    public static final synchronized Intake getInstance() {
        return instance == null ? instance = new Intake() : instance;
    }
}
