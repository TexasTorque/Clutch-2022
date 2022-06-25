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
import org.texastorque.torquelib.util.TorqueLogging;

/**
 * The intake subsystem.
 *
 * @author Jack Pittenger
 * @author Justus Languell
 */
public final class Intake extends TorqueSubsystem implements Subsystems {
    private static volatile Intake instance;
    
    private static final double ROTARY_UP_POSITION = 0, ROTARY_DOWN_POSITION = -5.64,
                                ROTARY_MIN_SPEED = -.35, ROTARY_MAX_SPEED = .35, 
                                ROLLER_MAX_SPEED = 1;

    public enum IntakeState implements TorqueSubsystemState {
        INTAKE(-ROLLER_MAX_SPEED, ROTARY_DOWN_POSITION),
        PRIMED(0, ROTARY_UP_POSITION),
        OUTAKE(ROLLER_MAX_SPEED, ROTARY_DOWN_POSITION);

        private final double direction, position;

        IntakeState(final double direction, final double position) { 
            this.direction = direction; 
            this.position = position;
        }

        public final double getDirection() { return direction; }
        public final double getPosition() { return position; }

        @Override
        public final String toString() {
            return String.format("Rol: %01.2f, Rot: %01.2f", direction, position);
        }
    }

    private IntakeState state = IntakeState.PRIMED;

    private final TorqueSparkMax rotary, rollers;

    private Intake() {
        rotary = new TorqueSparkMax(Ports.INTAKE.ROTARY);
        rotary.configurePID(new KPID(0.1, 0, 0, 0, ROTARY_MIN_SPEED, ROTARY_MAX_SPEED));
        // rotary.configurePositionalCANFrame();
        // rotary.burnFlash();

        rollers = new TorqueSparkMax(Ports.INTAKE.ROLLER);
        // rollers.burnFlash();
    }

    public final void setState(final IntakeState state) { this.state = state; }

    public final boolean isIntaking() { return state == IntakeState.INTAKE; }

    @Override
    public final void initialize(final TorqueMode mode) {}

    @Override
    public final void update(final TorqueMode mode) {
        rotary.setPosition(state.getPosition());
        rollers.setPercent(state.getDirection());

        TorqueSubsystemState.logState(state);

        // SmartDashboard.putNumber("Rotary Position", rotary.getPosition());
        TorqueLogging.putNumber("Rotary Position", rotary.getPosition());
        // SmartDashboard.putNumber("Rotary Current", rotary.getCurrent());
        TorqueLogging.putNumber("Rotary Current", rotary.getCurrent());
        // SmartDashboard.putNumber("Rollers Current", rollers.getCurrent());
        TorqueLogging.putNumber("Rollers Current", rollers.getCurrent());
    }

    public static final synchronized Intake getInstance() {
        return instance == null ? instance = new Intake() : instance;
    }
}
