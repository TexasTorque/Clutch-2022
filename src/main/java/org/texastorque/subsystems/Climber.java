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

    private static final double MAX_LEFT = 184, MAX_RIGHT = -184;

    public static abstract class ArmConfig {
        protected final double speed;

        protected ArmConfig(final double speed) {
            this.speed = speed;
        }

        public abstract double calculate(final double current);
    }

    public static final class NonConstrainedArmConfig extends ArmConfig {
        public NonConstrainedArmConfig(final double speed) {
            super(speed);
        } 

        @Override
        public final double calculate(final double current) {
            return speed;
        }
    }

    public static final class ConstrainedArmConfig extends ArmConfig {
        protected final double min, max;

        public ConstrainedArmConfig(final double speed, final double min, final double max) {
            super(speed);
            this.min = min;
            this.max = max;
        }

        @Override
        public final double calculate(final double current) {
            return TorqueMathUtil.linearConstraint(speed, current, min, max);
        }
    }

    public static enum ClimberState implements TorqueSubsystemState {
        OFF(new NonConstrainedArmConfig(0), new NonConstrainedArmConfig(0)),
        BOTH_UP(new ConstrainedArmConfig(1, 0, MAX_LEFT), 
                new ConstrainedArmConfig(-1, MAX_RIGHT, 0)),
        BOTH_DOWN(new ConstrainedArmConfig(-1, 0, MAX_LEFT), 
                new ConstrainedArmConfig(1, MAX_RIGHT, 0)),
        ZERO_LEFT(new NonConstrainedArmConfig(-.3), new NonConstrainedArmConfig(0)),
        ZERO_RIGHT(new NonConstrainedArmConfig(0), new NonConstrainedArmConfig(.3));

        private final ArmConfig left, right;

        ClimberState(final ArmConfig left, final ArmConfig right) {
            this.left = left;
            this.right = right;
        }

        public final ArmConfig getLeft() { return left; }
        public final ArmConfig getRight() { return right; }
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
        motor.setEncoderZero(0);
        return motor;
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
        if (!started && state != ClimberState.OFF) started = true;
        
        TorqueSubsystemState.logState(state);
        SmartDashboard.putString("Winch", String.format("%03.2f   %03.2f", left.getPosition(), right.getPosition()));

        left.setPercent(state.getLeft().calculate(left.getPosition()));
        right.setPercent(state.getLeft().calculate(right.getPosition()));
    }

    public static final synchronized Climber getInstance() {
        return instance == null ? instance = new Climber() : instance;
    }

}
