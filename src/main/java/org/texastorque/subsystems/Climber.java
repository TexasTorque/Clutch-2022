package org.texastorque.subsystems;

import org.texastorque.Ports;
import org.texastorque.Subsystems;
import org.texastorque.torquelib.base.TorqueMode;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.base.TorqueSubsystemState;
import org.texastorque.torquelib.motors.TorqueSparkMax;
import org.texastorque.torquelib.util.TorqueMathUtil;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public final class Climber extends TorqueSubsystem implements Subsystems {
    private static volatile Climber instance;

    private final double MAX_LEFT = 182, MAX_RIGHT = -182;

    public static enum ClimberState implements TorqueSubsystemState {
        OFF(0, 0),
        BOTH_UP(-1, -1),
        BOTH_DOWN(1, 1),
        ZERO_RIGHT(0, -.3),
        ZERO_LEFT(-.3, 0);

        private final double left, right;

        ClimberState(final double left, final double right) { 
            this.left = left;
            this.right = right;
        }

        public final double getLeft() { return left; }
        public final double getRight() { return right; }
    }

    private final TorqueSparkMax left, right;

    private boolean started = false;
    public final boolean hasStarted() { return started; }
    public final void reset() { started = false; }

    private ClimberState state = ClimberState.OFF;
    public final void setState(final ClimberState state) { this.state = state; }
    public final ClimberState getState() { return this.state; }

    private final TorqueSparkMax setupWinchMotor(final int port) {
        final TorqueSparkMax motor = new TorqueSparkMax(port);
        motor.configurePositionalCANFrame();
        motor.zeroEncoderAtCurrentPosition();
        return motor;
    }

    public final void zeroWinchMotors() {
        left.zeroEncoderAtCurrentPosition();
        right.zeroEncoderAtCurrentPosition();
    }

    private Climber() {
        this.left = setupWinchMotor(Ports.CLIMBER.WINCH.LEFT);
        this.right = setupWinchMotor(Ports.CLIMBER.WINCH.RIGHT);
    }

    @Override
    public final void initialize(final TorqueMode mode) {
        if (mode != TorqueMode.TELEOP) return;
        reset();
    }

    @Override
    public final void update(final TorqueMode mode) {
        if (mode != TorqueMode.TELEOP) return;

        if (!started && state != ClimberState.OFF)
            started = true;
        
        if (!TorqueMathUtil.constrained(left.getPosition(), 0., MAX_LEFT) 
                || !TorqueMathUtil.constrained(right.getPosition(), 0., MAX_RIGHT))
            return;
        this.left.setPercent(this.state.getLeft());
        this.right.setPercent(this.state.getRight());

        TorqueSubsystemState.logState(state);
        SmartDashboard.putString("Winch", String.format("%03.2f   %03.2f", left.getPosition(), right.getPosition()));
    }

    public static final synchronized Climber getInstance() {
        return instance == null ? instance = new Climber() : instance;
    }
}
