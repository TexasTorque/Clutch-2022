/**
 * Copyright 2022 Texas Torque.
 * 
 * This file is part of Clutch-2022, which is not licensed for distribution.
 * For more details, see ./license.txt or write <jus@gtsbr.org>.
 */
package org.texastorque.subsystems;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.Ports;
import org.texastorque.Subsystems;
import org.texastorque.torquelib.base.TorqueMode;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.base.TorqueSubsystemState;
import org.texastorque.torquelib.control.TorqueRamp;
import org.texastorque.torquelib.control.TorqueTimeout;
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

    private static final double ROTARY_UP_POSITION = 0, ROTARY_DOWN_POSITION = -5.64, ROTARY_MIN_SPEED = -.35,
                                ROTARY_MAX_SPEED = .35, ROLLER_MAX_SPEED = 1;

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
    private TorqueTimeout revIntake = new TorqueTimeout(.5);
    // Bruh idfk
    private TorqueRamp rampIntakeVolts = new TorqueRamp(2, 1.3, 9);
    // private TorqueRamp rampIntakeVolts = new TorqueRamp(3, 1.3, 12);
    // private TorqueRamp rampIntakeCurrent = new TorqueRamp(3, .1, .5);

    private final TorqueSparkMax rotary, rollers;

    private Intake() {
        rotary = new TorqueSparkMax(Ports.INTAKE.ROTARY);
        rotary.configurePID(new KPID(0.1, 0, 0, 0, ROTARY_MIN_SPEED, ROTARY_MAX_SPEED));

        rollers = new TorqueSparkMax(Ports.INTAKE.ROLLER);
        rollers.configurePID(new KPID(1, 0, 0, 0, -1, 1));

        // rollers.configureSmartMotion(5600, 0, 5600, 100, 0);
    }

    public final void setState(final IntakeState state) { this.state = state; }

    public final boolean isIntaking() { return state == IntakeState.INTAKE; }

    @Override
    public final void initialize(final TorqueMode mode) {}

    @Override
    public final void update(final TorqueMode mode) {
        rollers.setVoltage(Math.signum(state.getDirection()) * rampIntakeVolts.calculate(state != IntakeState.PRIMED));
        // rollers.setCurrent(Math.signum(state.getDirection()) * rampIntakeCurrent.calculate(state != IntakeState.PRIMED));
        // rollers.setVelocityRPM(5600);

        rotary.setPosition(revIntake.calculate(isIntaking()) ? IntakeState.PRIMED.getPosition() : state.getPosition());

        TorqueSubsystemState.logState(state);

        SmartDashboard.putNumber("Rotary Position", rotary.getPosition());
        SmartDashboard.putNumber("Rotary Current", rotary.getCurrent());
        SmartDashboard.putNumber("Rollers Current", rollers.getCurrent());
    }

    public static final synchronized Intake getInstance() {
        return instance == null ? instance = new Intake() : instance;
    }
}
