package org.texastorque.subsystems;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.constants.Constants;
import org.texastorque.constants.Ports;
import org.texastorque.inputs.Input;
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
        double climberSpeeds = Input.getInstance().getClimberInput().getDirection().getDirection()
                * Constants.CLIMBER_SPEED;
        if (Input.getInstance().getClimberInput().runLeft) {
            climberSpeedsLeft = Constants.CLIMBER_SPEED;
        } else if (Input.getInstance().getClimberInput().runRight) {
            climberSpeedsRight = Constants.CLIMBER_SPEED;
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
    public void updateFeedbackTeleop() {
        // climberPosition = left.getPosition(); // left or right, doesnt matter
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
        SmartDashboard.putNumber("[Climber]Position Left", left.getPosition());
        SmartDashboard.putNumber("[Climber]Position Right", right.getPosition());
    }

    public static synchronized Climber getInstance() {
        return instance == null ? instance = new Climber() : instance;
    }
}