package org.texastorque.subsystems;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.constants.Constants;
import org.texastorque.constants.Ports;
import org.texastorque.inputs.AutoInput;
import org.texastorque.inputs.Feedback;
import org.texastorque.inputs.Input;
import org.texastorque.inputs.State;
import org.texastorque.inputs.State.AutoClimb;
import org.texastorque.torquelib.base.TorqueSubsystem;
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

    private TorqueSparkMax left;
    private TorqueSparkMax right;

    private double climberSpeedsLeft;
    private double climberSpeedsRight;

    private Climber() {
        left = new TorqueSparkMax(Ports.CLIMBER_LEFT);
        right = new TorqueSparkMax(Ports.CLIMBER_RIGHT);
        left.tareEncoder();
        right.tareEncoder();
    }

    @Override
    public void updateTeleop() {
        if (State.getInstance().getAutoClimb() == AutoClimb.ON) {
            updateAuto();
            return;
        }
        double climberSpeeds = Input.getInstance().getClimberInput().getDirection().getDirection()
                * Constants.CLIMBER_SPEED;
        if (Input.getInstance().getClimberInput().runLeft) {
            climberSpeedsLeft = Constants.CLIMBER_SPEED * .3;
        } else if (Input.getInstance().getClimberInput().runRight) {
            climberSpeedsRight = Constants.CLIMBER_SPEED * .3;
        } else {
            if (left.getPosition() > Constants.CLIMBER_LEFT_LIMIT_HIGH) {
                climberSpeedsLeft = Math.max(climberSpeeds, 0);
            } else if (left.getPosition() < Constants.CLIMBER_LEFT_LIMIT_LOW) {
                climberSpeedsLeft = Math.min(climberSpeeds, 0);
            } else {
                climberSpeedsLeft = climberSpeeds;
            }

            if (right.getPosition() < Constants.CLIMBER_RIGHT_LIMIT_HIGH) {
                climberSpeedsRight = Math.max(climberSpeeds, 0);
            } else if (right.getPosition() > Constants.CLIMBER_RIGHT_LIMIT_LOW) {
                climberSpeedsRight = Math.min(climberSpeeds, 0);
            } else {
                climberSpeedsRight = climberSpeeds;
            }
        }
    }

    @Override
    public void updateAuto() {

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

        Feedback.getInstance().getClimberFeedback().setLeftPosition(left.getPosition());
        Feedback.getInstance().getClimberFeedback().setRightPosition(right.getPosition());
    }

    @Override
    public void updateFeedbackAuto() {
        updateFeedbackTeleop();
    }

    @Override
    public void output() {
        left.set(-climberSpeedsLeft);
        right.set(climberSpeedsRight);
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