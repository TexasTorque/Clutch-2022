package org.texastorque.subsystems;

import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.constants.Constants;
import org.texastorque.constants.Ports;
import org.texastorque.inputs.AutoInput;
import org.texastorque.inputs.Feedback;
import org.texastorque.inputs.Input;
import org.texastorque.inputs.State;
import org.texastorque.inputs.State.AutoClimb;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.component.TorqueLinearServo;
import org.texastorque.torquelib.component.TorqueSparkMax;

public class Climber extends TorqueSubsystem {
    private volatile static Climber instance = null;

    public static enum ClimberDirection {
        PULL(1),
        STOP(0),
        PUSH(-1);

        private final int direction;

        ClimberDirection(int direction) {
            this.direction = direction;
        }

        public int getDirection() {
            return direction;
        }
    }

    public static enum ServoDirection {
        DETACH(Constants.CLIMBER_LEFT_SERVO_DETACHED, Constants.CLIMBER_RIGHT_SERVO_DETACHED),
        ATTACH(Constants.CLIMBER_LEFT_SERVO_ATTACHED, Constants.CLIMBER_RIGHT_SERVO_ATTACHED);

        private double positionLeft;
        private double positionRight;

        ServoDirection(double positionLeft, double positionRight) {
            this.positionLeft = positionLeft;
            this.positionRight = positionRight;
        }

        /**
         * @return the positionLeft
         */
        public double getPositionLeft() {
            return positionLeft;
        }

        /**
         * @return the positionRight
         */
        public double getPositionRight() {
            return positionRight;
        }
    }

    private TorqueSparkMax left;
    private TorqueSparkMax right;

    private Servo leftServo;
    private Servo rightServo;

    private ServoDirection servoDirection = ServoDirection.ATTACH;

    private double climberSpeedsLeft;
    private double climberSpeedsRight;

    private Climber() {
        left = new TorqueSparkMax(Ports.CLIMBER_LEFT);
        right = new TorqueSparkMax(Ports.CLIMBER_RIGHT);

        leftServo = new Servo(Ports.CLIMBER_LEFT_SERVO);
        rightServo = new Servo(Ports.CLIMBER_RIGHT_SERVO);

        left.tareEncoder();
        right.tareEncoder();
        SmartDashboard.putNumber("servoleft", .5);
        SmartDashboard.putNumber("servoright", .5);
    }

    @Override
    public void updateTeleop() {
        if (State.getInstance().getAutoClimb() == AutoClimb.ON) {
            updateAssist();
            return;
        }

        servoDirection = Input.getInstance().getClimberInput().getServoDirection();

        double climberSpeeds = Input.getInstance()
                .getClimberInput()
                .getDirection()
                .getDirection() *
                Constants.CLIMBER_SPEED;
        if (Input.getInstance().getClimberInput().runLeft) {
            climberSpeedsLeft = Constants.CLIMBER_SPEED * .1;
        } else if (Input.getInstance().getClimberInput().runRight) {
            climberSpeedsRight = Constants.CLIMBER_SPEED * .1;
        } else {
            if (left.getPosition() > Constants.CLIMBER_LEFT_LIMIT_HIGH) {
                climberSpeedsLeft = Math.max(climberSpeeds, 0);
            } else if (left.getPosition() < Constants.CLIMBER_LEFT_LIMIT_LOW) {
                climberSpeedsLeft = Math.min(climberSpeeds, 0);
            } else {
                climberSpeedsLeft = climberSpeeds;
            }

            if (Math.abs(Constants.CLIMBER_LEFT_LIMIT_HIGH - left.getPosition()) < 25
                    && Input.getInstance().getClimberInput().getDirection() == ClimberDirection.PUSH) {
                climberSpeedsLeft *= .5;
            } else if (Math.abs(Constants.CLIMBER_LEFT_LIMIT_LOW - left.getPosition()) < 25
                    && Input.getInstance().getClimberInput().getDirection() == ClimberDirection.PULL) {
                climberSpeedsLeft *= .5;
            }

            if (right.getPosition() < Constants.CLIMBER_RIGHT_LIMIT_HIGH) {
                climberSpeedsRight = Math.max(climberSpeeds, 0);
            } else if (right.getPosition() > Constants.CLIMBER_RIGHT_LIMIT_LOW) {
                climberSpeedsRight = Math.min(climberSpeeds, 0);
            } else {
                climberSpeedsRight = climberSpeeds;
            }

            if (Math.abs(right.getPosition() - Constants.CLIMBER_RIGHT_LIMIT_HIGH) < 25
                    && Input.getInstance().getClimberInput().getDirection() == ClimberDirection.PUSH) {
                climberSpeedsRight *= .5;
            } else if (Math.abs(Constants.CLIMBER_RIGHT_LIMIT_LOW - right.getPosition()) < 25
                    && Input.getInstance().getClimberInput().getDirection() == ClimberDirection.PULL) {
                climberSpeedsRight *= .5;
            }
        }
    }

    public void updateAssist() {

        double climberLeftSetpoint = AutoInput.getInstance().getClimberLeftSetpoint();
        double climberRightSetpoint = AutoInput.getInstance().getClimberRightSetpoint();

        if (climberLeftSetpoint > left.getPosition()) {
            climberSpeedsLeft = -Constants.CLIMBER_SPEED; // need to go up
        } else {
            climberSpeedsLeft = Constants.CLIMBER_SPEED;
        }

        if (climberRightSetpoint < right.getPosition()) {
            climberSpeedsRight = -Constants.CLIMBER_SPEED;
        } else {
            climberSpeedsRight = Constants.CLIMBER_SPEED;
        }
    }

    @Override
    public void updateFeedbackTeleop() {

        Feedback.getInstance().getClimberFeedback().setLeftPosition(
                left.getPosition());
        Feedback.getInstance().getClimberFeedback().setRightPosition(
                right.getPosition());
    }

    @Override
    public void updateFeedbackAuto() {
        updateFeedbackTeleop();
    }

    @Override
    public void output() {
        left.set(-climberSpeedsLeft);
        right.set(climberSpeedsRight);

        leftServo.set(servoDirection.getPositionLeft());
        rightServo.set(servoDirection.getPositionRight());

        // leftServo.set(SmartDashboard.getNumber("servoleft", 0.5));
        // rightServo.set(SmartDashboard.getNumber("servoright", 0.5));
    }

    @Override
    public void updateSmartDashboard() {
        SmartDashboard.putNumber("[Climber]SpeedLeft", climberSpeedsLeft);
        SmartDashboard.putNumber("[Climber]SpeedRight", climberSpeedsRight);
    }

    public static synchronized Climber getInstance() {
        return instance == null ? instance = new Climber() : instance;
    }
}