package org.texastorque.subsystems;

import org.texastorque.Ports;
import org.texastorque.Subsystems;
import org.texastorque.torquelib.base.TorqueMode;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.base.TorqueSubsystemState;
import org.texastorque.torquelib.motors.TorqueSparkMax;
import org.texastorque.torquelib.util.KPID;
import org.texastorque.torquelib.util.TorqueMathUtil;

import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public final class Climber extends TorqueSubsystem implements Subsystems {
    private static volatile Climber instance;

    private static final double MAX_LEFT = 183, MAX_RIGHT = -184.74;

    public static abstract class ArmConfig {
        protected final double speed;

        protected ArmConfig(final double speed) {
            this.speed = speed;
        }

        public abstract double calculate(final double current, final boolean limitSwitch);
    }

    public static final class NonConstrainedArmConfig extends ArmConfig {
        public NonConstrainedArmConfig(final double speed) {
            super(speed);
        } 

        @Override
        public final double calculate(final double current, final boolean limitSwitch) {
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
        public final double calculate(final double current, final boolean limitSwitch) {
            return TorqueMathUtil.linearConstraint(speed, current, min, max);
        }
    }

    public static final class LimitedArmConfig extends ArmConfig {

        protected final double min, max;
        protected LimitedArmConfig(final double speed, final double min, final double max) {
            super(speed);
            this.min = min;
            this.max = max;
        }

        @Override
        public double calculate(final double current, final boolean limitSwitch) {
            double ret = 0.0;
            if (limitSwitch) {
                if ((this.min > this.max) && current < 0) {
                    return 0.0;
                } else if ((this.min < this.max) && current > 0) {
                    return 0.0;
                }
            } else {
                return TorqueMathUtil.linearConstraint(speed, current, min, max);
            }
            return ret;

        }
        
    }

    public static enum ClimberState implements TorqueSubsystemState {
        OFF(new NonConstrainedArmConfig(0), new NonConstrainedArmConfig(0)),
        BOTH_UP(new ConstrainedArmConfig(1, 0, MAX_LEFT), 
                new ConstrainedArmConfig(-1, MAX_RIGHT, 0)),
        BOTH_DOWN(new ConstrainedArmConfig(-1, 0, MAX_LEFT), 
                new ConstrainedArmConfig(1, MAX_RIGHT, 0)),
        ZERO_LEFT(new NonConstrainedArmConfig(-.3), new NonConstrainedArmConfig(0)),
        ZERO_RIGHT(new NonConstrainedArmConfig(0), new NonConstrainedArmConfig(.3)),
        BOTH_UP_LIMIT(new LimitedArmConfig(1, 0, MAX_LEFT), 
                new LimitedArmConfig(-1, MAX_RIGHT, 0)),
        BOTH_DOWN_LIMIT(new LimitedArmConfig(-1, 0, MAX_LEFT), 
                new LimitedArmConfig(1, MAX_RIGHT, 0));
                
        private final ArmConfig left, right;

        ClimberState(final ArmConfig left, final ArmConfig right) {
            this.left = left;
            this.right = right;
        }

        public final ArmConfig getLeft() { return left; }
        public final ArmConfig getRight() { return right; }
    }

    public double _winchState = 0;
    public boolean _servo = false;
        
    private final TorqueSparkMax left, right, winch;
    private final Servo leftServo, rightServo;

    private boolean started = false;
    public final boolean hasStarted() { return started; }
    public final void reset() { started = false; }

    private ClimberState state = ClimberState.OFF;
    public final void setState(final ClimberState state) { this.state = state; }
    public final ClimberState getState() { return this.state; }

    private final TorqueSparkMax setupArmMotors(final int port) {
        final TorqueSparkMax motor = new TorqueSparkMax(port);
        motor.configurePositionalCANFrame();
        motor.setEncoderZero(0);
        return motor;
    }

    private Climber() {
        this.left = setupArmMotors(Ports.CLIMBER.ARMS.LEFT);
        this.right = setupArmMotors(Ports.CLIMBER.ARMS.RIGHT);
        
        // Positive is pull together
        this.winch = new TorqueSparkMax(Ports.CLIMBER.WINCH);
        // Guess and check until they stop yelling at me
        this.winch.configurePID(new KPID(.1, 0, 0, 0, -1., 1.));
        this.winch.configurePositionalCANFrame();
        this.winch.setEncoderZero(0);
        this.winch.burnFlash();
        
        this.leftServo = new Servo(Ports.CLIMBER.SERVO.LEFT);
        this.rightServo = new Servo(Ports.CLIMBER.SERVO.RIGHT);
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
        SmartDashboard.putString("Arms", String.format("%03.2f   %03.2f", left.getPosition(), right.getPosition()));

        left.setPercent(state.getLeft().calculate(left.getPosition(), _servo));
        right.setPercent(state.getRight().calculate(right.getPosition(), _servo));

        winch.setPercent(Math.max(Math.min(_winchState, 1), -1));

        SmartDashboard.putString("Winch", String.format("%03.2f", winch.getPosition()));

        leftServo.set(_servo ? .5 : .9);
        rightServo.set(_servo ? .5 : .1);
    }

    public static final synchronized Climber getInstance() {
        return instance == null ? instance = new Climber() : instance;
    }

}
