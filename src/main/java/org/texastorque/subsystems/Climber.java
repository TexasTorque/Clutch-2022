package org.texastorque.subsystems;

import org.texastorque.Ports;
import org.texastorque.Subsystems;
import org.texastorque.torquelib.base.TorqueMode;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.base.TorqueSubsystemState;
import org.texastorque.torquelib.motors.TorqueSparkMax;
import org.texastorque.torquelib.util.KPID;
import org.texastorque.torquelib.util.TorqueMathUtil;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public final class Climber extends TorqueSubsystem implements Subsystems {
    private static volatile Climber instance;

    private static final double MAX_LEFT = 183, MAX_RIGHT = 184.74;

    public static class ArmConfig {
        protected final double speed;
        public ArmConfig(final double speed) { this.speed = speed; } 
        public double calculate(final double current, final boolean limit) {
            return speed;
        }
    }

    public static class ConstrainedArmConfig extends ArmConfig {
        protected final double min, max;
        public ConstrainedArmConfig(final double speed, final double min, final double max) {
            super(speed);
            this.min = min;
            this.max = max;
        }

        @Override
        public double calculate(final double current, final boolean limit) {
            return TorqueMathUtil.linearConstraint(speed, current, min, max);
        }
    }

    public static class LimitedArmConfig extends ConstrainedArmConfig {
        protected final boolean upper;
        protected LimitedArmConfig(final double speed, final double min, final double max, final boolean upper) {
            super(speed, min, max);
            this.upper = upper;
        }

        @Override
        public double calculate(final double current, final boolean limit) {
            return TorqueMathUtil.linearConstraint(limit ? (upper ? max : min) : speed, current, min, max);
        }
        
    }

    public static enum ClimberState implements TorqueSubsystemState {
        OFF(new ArmConfig(0), new ArmConfig(0)),
        BOTH_UP(new ConstrainedArmConfig(1, 0, MAX_LEFT), 
                new ConstrainedArmConfig(1, 0, MAX_RIGHT)),
        BOTH_DOWN(new LimitedArmConfig(-1, 0, MAX_LEFT, false), 
                new LimitedArmConfig(-1, 0, MAX_RIGHT, false)),
        ZERO_LEFT(new ArmConfig(-.3), new ArmConfig(0)),
        ZERO_RIGHT(new ArmConfig(0), new ArmConfig(-.3));
       
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
    private final DigitalInput leftSwitch, rightSwitch;

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
        left = setupArmMotors(Ports.CLIMBER.ARMS.LEFT);
        right = setupArmMotors(Ports.CLIMBER.ARMS.RIGHT);
        
        // Positive is pull together
        winch = new TorqueSparkMax(Ports.CLIMBER.WINCH);
        // Guess and check until they stop yelling at me
        winch.configurePID(new KPID(.1, 0, 0, 0, -1., 1.));
        winch.configurePositionalCANFrame();
        winch.setEncoderZero(0);
        winch.burnFlash();
        
        leftServo = new Servo(Ports.CLIMBER.SERVO.LEFT);
        rightServo = new Servo(Ports.CLIMBER.SERVO.RIGHT);

        leftSwitch = new DigitalInput(Ports.CLIMBER.CLAW.LEFT);
        rightSwitch = new DigitalInput(Ports.CLIMBER.CLAW.RIGHT);
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

        left.setPercent(state.getLeft().calculate(left.getPosition(), leftSwitch.get()));
        right.setPercent(-state.getRight().calculate(-right.getPosition(), rightSwitch.get()));

        winch.setPercent(Math.max(Math.min(_winchState, 1), -1));

        SmartDashboard.putString("Winch", String.format("%03.2f", winch.getPosition()));

        leftServo.set(_servo ? .5 : .9);
        rightServo.set(_servo ? .5 : .1);
    }

    public static final synchronized Climber getInstance() {
        return instance == null ? instance = new Climber() : instance;
    }

}
