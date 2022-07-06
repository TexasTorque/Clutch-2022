/**
 * Copyright 2022 Texas Torque.
 * 
 * This file is part of Clutch-2022, which is not licensed for distribution.
 * For more details, see ./license.txt or write <jus@gtsbr.org>.
 */
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

/**
 * What is faller?
 * 
 * Faller is a subsystem built around an alternative design pattern 
 * for a manual only climber that utalizes setpoints.
 * 
 * Why? Not even fucking talking about it. The next time a driver
 * lies about shit so they can drive around I am going to jump off a bridge.
 * 
 * @author Justus Languell
 */
public final class Faller extends TorqueSubsystem implements Subsystems {
    private static volatile Faller instance;

    private static final double MAX_LEFT = 180, MAX_RIGHT = 180, LEFT_SERVO_ENGAGED = .6, LEFT_SERVO_DISENGAGED = 1.0,
                                RIGHT_SERVO_ENGAGED = .5, RIGHT_SERVO_DISENGAGED = .1, ARM_PWR = .75, WINCH_PWR = .35;

    public static enum WinchState implements TorqueSubsystemState {
        IN, OFF, OUT;
        public final double getDirection() { return this.ordinal() - 1; }
        public final boolean isOn() { return this != OFF; }
    }

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
            return TorqueMath.linearConstraint(speed, current, min, max);
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
            return TorqueMath.linearConstraint(limit ? (upper ? max : min) : speed, current, min, max);
        }
    }

    public static enum FallerState implements TorqueSubsystemState {
        OFF(new ArmConfig(0), new ArmConfig(0)),
        BOTH_UP(new ConstrainedArmConfig(1, 0, MAX_LEFT), 
                new ConstrainedArmConfig(1, 0, MAX_RIGHT)),
        BOTH_DOWN(new LimitedArmConfig(-1, 0, MAX_LEFT, false), 
                new LimitedArmConfig(-1, 0, MAX_RIGHT, false)),
        ZERO_LEFT(new ArmConfig(-.3), new ArmConfig(0)),
        ZERO_RIGHT(new ArmConfig(0), new ArmConfig(-.3));
       
        private final ArmConfig left, right;

        FallerState(final ArmConfig left, final ArmConfig right) {
            this.left = left;
            this.right = right;
        }

        public final ArmConfig getLeft() { return left; }
        public final ArmConfig getRight() { return right; }
    }

    private final TorqueSparkMax left, right, winch;
    private final Servo leftServo, rightServo;
    private final DigitalInput leftSwitch, rightSwitch;

    private boolean started = false;


    public final boolean hasStarted() { return started; }

    public final void reset() {
        started = false;
        winchState = WinchState.OFF;
    }

    private FallerState state = FallerState.OFF;
    private WinchState winchState = WinchState.OFF;

    public final void setState(final FallerState state) { this.state = state; }
    public final void setWinch(final WinchState winch) { this.winchState = winch; }

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
        // These are non-blocking operations down to the HAL.
        // It is completely impossible that the servos are actuating at 
        // times further away than a few thousand clock cycles,
        // a.k.a. nanoseconds.
        // I am completely done with this $h!t.
        leftServo.set(engaged ? LEFT_SERVO_ENGAGED : LEFT_SERVO_DISENGAGED);
        rightServo.set(engaged ? RIGHT_SERVO_ENGAGED : RIGHT_SERVO_DISENGAGED);
    }

    private Faller() {
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
        SmartDashboard.putString("Arms", String.format("%03.2f   %03.2f", left.getPosition(), right.getPosition()));

        SmartDashboard.putString("Winch", String.format("%03.2f", winch.getPosition()));

        SmartDashboard.putBoolean("Left Switch", leftSwitch.get());
        SmartDashboard.putBoolean("Right Switch", rightSwitch.get());

        winch.setPercent(winchState.getDirection() * WINCH_PWR);

        left.setPercent(state.getLeft().calculate(left.getPosition(), leftSwitch.get()));
        right.setPercent(-state.getRight().calculate(-right.getPosition(), rightSwitch.get()));
    }

    public static final synchronized Faller getInstance() {
        return instance == null ? instance = new Faller() : instance;
    }
}
