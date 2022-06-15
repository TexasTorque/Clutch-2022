package org.texastorque.subsystems;

import org.texastorque.Ports;
import org.texastorque.Subsystems;
import org.texastorque.torquelib.base.TorqueMode;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.base.TorqueSubsystemState;
import org.texastorque.torquelib.control.TorqueClick;
import org.texastorque.torquelib.control.TorqueToggle;
import org.texastorque.torquelib.motors.TorqueSparkMax;
import org.texastorque.torquelib.util.KPID;
import org.texastorque.torquelib.util.TorqueMathUtil;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public final class Climber extends TorqueSubsystem implements Subsystems {
    private static volatile Climber instance;

    private static final double MAX_LEFT = 183, MAX_RIGHT = 184.74, 
            LEFT_SERVO_ENGAGED = .5, LEFT_SERVO_DISENGAGED = .9,
            RIGHT_SERVO_ENGAGED = .5, RIGHT_SERVO_DISENGAGED = .1;

    public static enum ClimberState implements TorqueSubsystemState {
        OFF, INIT_PUSH, INIT_PULL, TILT_OUT, TILT_IN, ADVANCE;

        public final ClimberState next() {
            return values()[(this.ordinal() + 1) % values().length];
        }
    }

    public double _winchState = 0;
    public boolean _servo = false;
        
    private final TorqueSparkMax left, right, winch;
    private final Servo leftServo, rightServo;
    private final DigitalInput leftSwitch, rightSwitch;

    private boolean started = false;
    public final boolean hasStarted() { return started; }

    private TorqueToggle approved = new TorqueToggle(false);
    public final void setApproved(final boolean approved) { this.approved.set(approved); }

    public final void reset() { started = false; approved = new TorqueToggle(false); }

    private ClimberState state = ClimberState.OFF;

    private final TorqueSparkMax setupArmMotors(final int port) {
        final TorqueSparkMax motor = new TorqueSparkMax(port);
        motor.configurePID(new KPID(.1, 0, 0, 0, -1., 1.));
        motor.configurePositionalCANFrame();
        motor.setEncoderZero(0);
        return motor;
    }

    /**
     * Set the servos on the claws.
     * 
     * @param engaged If they will attach or not. 
     * 
     * @apiNote False is to release.
     */
    private final void setServos(final boolean engaged) {
        leftServo.set(engaged ? LEFT_SERVO_ENGAGED : LEFT_SERVO_DISENGAGED);
        rightServo.set(engaged ? RIGHT_SERVO_ENGAGED : RIGHT_SERVO_DISENGAGED);
    }

    private Climber() {
        left = setupArmMotors(Ports.CLIMBER.ARMS.LEFT);
        right = setupArmMotors(Ports.CLIMBER.ARMS.RIGHT);
        
        // Positive is pull together
        winch = new TorqueSparkMax(Ports.CLIMBER.WINCH);
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

        // left.setPercent(state.getLeft().calculate(left.getPosition(), leftSwitch.get()));
        // right.setPercent(-state.getRight().calculate(-right.getPosition(), rightSwitch.get()));
        // winch.setPercent(Math.max(Math.min(_winchState, 1), -1));

        SmartDashboard.putString("Winch", String.format("%03.2f", winch.getPosition()));

        if (state == ClimberState.OFF) handleOff();
        else if (state == ClimberState.INIT_PUSH) handleInitPush();
        else if (state == ClimberState.INIT_PULL) handleInitPull();
        else if (state == ClimberState.TILT_OUT) handleTiltOut();
        else if (state == ClimberState.TILT_IN) handleTiltIn();
        else if (state == ClimberState.ADVANCE) handleAdvance();
        else ;
    }

    private final void handleOff() {
        left.setPercent(0);
        right.setPercent(0);

        if (approved.get()) state = state.next();
    }

    private final void handleInitPush() { 
        final double toLeft = MAX_LEFT, toRight = MAX_RIGHT;
        left.setPosition(toLeft);
        right.setPosition(-toRight);

        if (left.getPosition() >= toLeft && -right.getPosition() >= toRight && approved.get()) 
            state = state.next();
    }

    private final void handleInitPull() { 
        if (leftSwitch.get()) left.setPercent(0);
        else left.setPosition(0);

        if (rightSwitch.get()) right.setPercent(0);
        else right.setPosition(0);

        if (leftSwitch.get() && rightSwitch.get() && approved.get())
            state = state.next();
    }

    private final void handleTiltOut() {
        final double OFFSET = 40, toLeft = MAX_LEFT - OFFSET, toRight = MAX_RIGHT - OFFSET, toWinch = -85;

        winch.setPosition(toWinch);
        left.setPosition(toLeft);
        right.setPosition(-toRight);

        if (left.getPosition() >= toLeft && -right.getPosition() >= toRight && approved.get()
                && TorqueMathUtil.toleranced(winch.getPosition(), toWinch, 2)) 
            state = state.next();
    }

    private final void handleTiltIn() { 
        final double offset = 15, toLeft = MAX_LEFT, toRight = MAX_RIGHT, toWinch = -83;

        if (left.getPosition() >= toLeft - offset && -right.getPosition() >= toRight - offset) 
            winch.setPosition(toWinch);
        
        if (left.getPosition() >= toLeft && -right.getPosition() >= toRight && approved.get()
                && TorqueMathUtil.toleranced(winch.getPosition(), toWinch, 2))
            state = state.next();
    }

    private final void handleAdvance() {
        final double offsetRelease = 40, 
                     leftRelease = MAX_LEFT - offsetRelease,
                     rightRelease = MAX_RIGHT - offsetRelease,
                     offsetWinch = 80,
                     leftWinch = MAX_LEFT - offsetWinch,
                     rightWinch = MAX_RIGHT - offsetWinch,
                     toWinch = -10,
                     leftWait = 30,
                     rightWait = 30;

        final boolean winchGood = TorqueMathUtil.toleranced(winch.getPosition(), toWinch, 2);

        if (left.getPosition() <= leftWinch && -right.getPosition() <= rightWinch)
            winch.setPosition(toWinch);
        else
            winch.setPercent(0);

        if (left.getPosition() <= leftWait && -right.getPosition() <= rightWait) {
            setServos(true);
            if (!winchGood) {
                left.setPercent(0);
                right.setPercent(0);
                return;
            }
        }

        if (TorqueMathUtil.toleranced(left.getPosition(), leftRelease, 5) 
                && TorqueMathUtil.toleranced(-right.getPosition(), rightRelease, 5))
            setServos(false); 

        if (leftSwitch.get()) left.setPercent(0);
        else left.setPosition(0);

        if (rightSwitch.get()) right.setPercent(0);
        else right.setPosition(0);

        if (leftSwitch.get() && rightSwitch.get() && approved.get())
            state = state.next();
    }

    public static final synchronized Climber getInstance() {
        return instance == null ? instance = new Climber() : instance;
    }

}
