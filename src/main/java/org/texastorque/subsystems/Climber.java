package org.texastorque.subsystems;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.Ports;
import org.texastorque.Subsystems;
import org.texastorque.torquelib.base.TorqueMode;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.base.TorqueSubsystemState;
import org.texastorque.torquelib.control.TorqueClick;
import org.texastorque.torquelib.motors.TorqueSparkMax;
import org.texastorque.torquelib.util.KPID;
import org.texastorque.torquelib.util.TorqueMath;

public final class Climber extends TorqueSubsystem implements Subsystems {
    private static volatile Climber instance;

    private static final double MAX_LEFT = 180, MAX_RIGHT = 180, LEFT_SERVO_ENGAGED = .6, LEFT_SERVO_DISENGAGED = 1.0,
                                RIGHT_SERVO_ENGAGED = .5, RIGHT_SERVO_DISENGAGED = .1, ARM_PWR = .75, WINCH_PWR = .3;

    public static enum AutoClimbState implements TorqueSubsystemState {
        OFF,
        INIT_PUSH,
        INIT_PULL,
        TILT_OUT,
        TILT_IN,
        ADVANCE;

        public final AutoClimbState next() { return values()[(this.ordinal() + 1) % values().length]; }
    }

    public static enum ManualClimbState implements TorqueSubsystemState {
        OFF(0, 0),
        ZERO_LEFT(-.3, 0),
        ZERO_RIGHT(0, -.3),
        BOTH_UP(1, 1),
        BOTH_DOWN(-1, -1);

        public final double left, right;

        ManualClimbState(final double left, final double right) {
            this.left = left;
            this.right = right;
        }
    }

    public static enum ManualWinchState implements TorqueSubsystemState {
        IN, OFF, OUT;
        public final double getDirection() { return this.ordinal() - 1; }
        public final boolean isOn() { return this != OFF; }
    }

    private final TorqueSparkMax left, right, winch;
    private final Servo leftServo, rightServo;
    private final DigitalInput leftSwitch, rightSwitch;

    private boolean started = false, approved = false, running = false;
    private final TorqueClick approvalReset = new TorqueClick();

    public final void setAuto(final boolean running) {
        this.running = running;
        if (!started && running) started = true;
        if (approvalReset.calculate(running)) approved = true;
    }

    public final void setManual(final ManualClimbState manual) { this.manualState = manual; }
    public final void setWinch(final ManualWinchState winch) { this.winchState = winch; }

    public final boolean hasStarted() { return started; }

    public final void reset() {
        started = false;
        approved = false;
        running = false;
        climbState = AutoClimbState.OFF;
        manualState = ManualClimbState.OFF;
        winchState = ManualWinchState.OFF;
        tooLowRight = false;
        tooLowLeft = false;
    }

    private final void advance() {
        climbState = climbState.next();
        approved = false;
        tooLowRight = false;
        tooLowLeft = false;
    }

    private final void advance(final AutoClimbState state) {
        climbState = state;
        approved = false;
        tooLowRight = false;
        tooLowLeft = false;
    }

    private AutoClimbState climbState = AutoClimbState.OFF;
    private ManualClimbState manualState = ManualClimbState.OFF;
    private ManualWinchState winchState = ManualWinchState.OFF;

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
    public final void setServos(final boolean engaged) {
        leftServo.set(engaged ? LEFT_SERVO_ENGAGED : LEFT_SERVO_DISENGAGED);
        rightServo.set(engaged ? RIGHT_SERVO_ENGAGED : RIGHT_SERVO_DISENGAGED);
    }

     public final void setLeftServo(final boolean engaged) {
        leftServo.set(engaged ? LEFT_SERVO_ENGAGED : LEFT_SERVO_DISENGAGED);
    }

     public final void setRightServo(final boolean engaged) {
        rightServo.set(engaged ? RIGHT_SERVO_ENGAGED : RIGHT_SERVO_DISENGAGED);
     }

    private Climber() {
        left = setupArmMotors(Ports.CLIMBER.ARMS.LEFT);
        right = setupArmMotors(Ports.CLIMBER.ARMS.RIGHT);

        // Positive is pull together
        winch = new TorqueSparkMax(Ports.CLIMBER.WINCH);
        winch.configurePID(new KPID(.1, 0, 0, 0, 1., -1.));
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
        TorqueSubsystemState.logState(climbState);
        SmartDashboard.putString("Arms", String.format("%03.2f   %03.2f", left.getPosition(), right.getPosition()));

        SmartDashboard.putString("Winch", String.format("%03.2f", winch.getPosition()));

        SmartDashboard.putBoolean("Approved", approved);
        SmartDashboard.putBoolean("Running", running);

        TorqueSubsystemState.logState(climbState);
        SmartDashboard.putString("Climb State", climbState.toString());

        SmartDashboard.putBoolean("Left Switch", leftSwitch.get());
        SmartDashboard.putBoolean("Right Switch", rightSwitch.get());

        if (winchState.isOn()) 
            winch.setPercent(winchState.getDirection() * WINCH_PWR);
        else if (running)
            handleAutoClimb();
        else
            handleManualState();
    }

    private final void handleAutoClimb() {
        // Would be a switch statement but they are ugly
        if (climbState == AutoClimbState.OFF)
            handleOff();
        else if (climbState == AutoClimbState.INIT_PUSH)
            handleInitPush();
        else if (climbState == AutoClimbState.INIT_PULL)
            handleInitPull();
        else if (climbState == AutoClimbState.TILT_OUT)
            handleTiltOut();
        else if (climbState == AutoClimbState.TILT_IN)
            handleTiltIn();
        else if (climbState == AutoClimbState.ADVANCE)
            handleAdvance();
        else
            killMotors();
    }

    private final void handleManualState() {
        left.setPercent(manualState.left);
        right.setPercent(-manualState.right);
        winch.setPercent(0);
    }

    private final void killMotors() {
        left.setPercent(0);
        right.setPercent(0);
        winch.setPercent(0);
    }

    private final void handleOff() {
        killMotors();
        if (approved) advance();
    }

    private final void handleInitPush() {
        final double toLeft = MAX_LEFT, toRight = MAX_RIGHT;
        left.setPercent(left.getPosition() <= toLeft ? ARM_PWR : 0);
        right.setPercent(-right.getPosition() <= toRight ? -ARM_PWR : 0);

        if (TorqueMath.toleranced(left.getPosition(), toLeft, 10) &&
            TorqueMath.toleranced(-right.getPosition(), toRight, 10) && approved)
            advance();
    }

    private boolean tooLowRight = false, tooLowLeft = false;

    private final void pullToLatch(final double speed) {
        left.setPercent(tooLowLeft ? (leftSwitch.get() ? 0 : .1) : -speed);
        right.setPercent(tooLowRight ? (rightSwitch.get() ? 0 : -.1) : speed);

        if (left.getPosition() <= 0) tooLowLeft = true;
        if (-right.getPosition() <= 0) tooLowRight = true;
    }

    private final void handleInitPull() {
        pullToLatch(ARM_PWR);
        if (leftSwitch.get() && rightSwitch.get() && approved) advance();
    }

    private final void handleTiltOut() {
        final double toLeft = 140, toRight = 140, toWinch = 90;

        winch.setPercent(winch.getPosition() <= toWinch ? WINCH_PWR : 0);
        right.setPercent(-right.getPosition() <= toRight ? -ARM_PWR : 0);
        left.setPercent(left.getPosition() <= toLeft ? ARM_PWR : 0);

        if (TorqueMath.toleranced(left.getPosition(), toLeft, 3) 
                && TorqueMath.toleranced(-right.getPosition(), toRight, 3) 
                && TorqueMath.toleranced(winch.getPosition(), toWinch, 10))
            advance();
    }

    private final void handleTiltIn() {
        final double offset = 30, toLeft = 200, toRight = 200, toWinch = 70;

        left.setPercent(left.getPosition() <= toLeft ? ARM_PWR : 0);
        right.setPercent(-right.getPosition() <= toRight ? -ARM_PWR : 0);

        // if (left.getPosition() >= toLeft - offset && -right.getPosition() >= toRight - offset)
        //     winch.setPercent(winch.getPosition() >= toWinch ? -.3 : 0);
        // else
        //     winch.setPercent(0);

        winch.setPercent(left.getPosition() >= toLeft - offset && -right.getPosition() >= toRight - offset
                ? (winch.getPosition() >= toWinch ? -WINCH_PWR : 0) : 0); //! <- WINCH IDLE IS .1

        if (TorqueMath.toleranced(left.getPosition(), toLeft, 5) &&
            TorqueMath.toleranced(-right.getPosition(), toRight, 5) && approved &&
            TorqueMath.toleranced(winch.getPosition(), toWinch, 15))
            advance();
    }

    private final void handleAdvance() {
        final double leftRelease = 185, rightRelease = 180, 
                     leftWinch = 100, rightWinch = 100, 
                     leftWait = 30, rightWait = 30,
                     toWinch = 0;

        // if (left.getPosition() <= leftWinch && -right.getPosition() <= rightWinch)
        //     winch.setPercent(winch.getPosition() >= toWinch ? -.3 : 0);
        // else
        //     winch.setPercent(0);
       
        // This shit gon need 2 be uncommented out!
        // winch.setPercent(left.getPosition() <= leftWinch && -right.getPosition() <= rightWinch 
        //         ? (winch.getPosition() >= toWinch ? -WINCH_PWR : 0) : 0);
        winch.setPercent(0);

        if (left.getPosition() <= leftWait && -right.getPosition() <= rightWait) {
            setServos(true);
            if (!TorqueMath.toleranced(winch.getPosition(), toWinch, 5)) {
                left.setPercent(0);
                right.setPercent(0);
                return;
            }
        }

        // if (TorqueMath.toleranced(left.getPosition(), leftRelease, 5) ||
        //     TorqueMath.toleranced(-right.getPosition(), leftRelease, 5))
        //     setLeftServo(false);
        // if (TorqueMath.toleranced(left.getPosition(), rightRelease, 5) ||
        //     TorqueMath.toleranced(-right.getPosition(), rightRelease, 5))
        //     setRightServo(false);

        if (TorqueMath.toleranced(left.getPosition(), leftRelease, 0, 20))
            setLeftServo(false);
        if (TorqueMath.toleranced(left.getPosition(), rightRelease, 0, 20))
            setRightServo(false);

        // if (TorqueMath.toleranced(left.getPosition(), leftRelease - 8, 5) &&
        //     TorqueMath.toleranced(-right.getPosition(), rightRelease - 8, 5))
        //     setRightServo(false);

        pullToLatch(ARM_PWR);

        // if (leftSwitch.get() && rightSwitch.get() && approved) 
            // advance(AutoClimbState.TILT_OUT);
    }

    public static final synchronized Climber getInstance() {
        return instance == null ? instance = new Climber() : instance;
    }
}
