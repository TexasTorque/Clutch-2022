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

    private final double tooHigh = Integer.MAX_VALUE;
    private final double tooLow = Integer.MIN_VALUE;

    private TorqueSparkMax left;
    private TorqueSparkMax right;

    private double climberSpeeds;

    private double climberPosition;

    private Climber() {
        left = new TorqueSparkMax(Ports.CLIMBER_LEFT);
        right = new TorqueSparkMax(Ports.CLIMBER_RIGHT);
    }

    @Override
    public void updateTeleop() {
        climberSpeeds = Input.getInstance().getClimberInput().getDirection().getDirection()
                * Constants.CLIMBER_SPEED;
        // if (left.getPosition() > Constants.CLIMBER_LIMIT_HIGH) {
        //     climberSpeeds = Math.max(climberSpeeds, 0);
        // } else if (left.getPosition() < Constants.CLIMBER_LIMIT_LOW) {
        //     climberSpeeds = Math.min(climberSpeeds, 0);
        // }
    }

    @Override
    public void updateFeedbackTeleop() {
        climberPosition = left.getPosition(); // left or right, doesnt matter
    }

    @Override
    public void output() {
        left.set(-climberSpeeds);
        right.set(climberSpeeds);
    }

    @Override
    public void updateSmartDashboard() {
        SmartDashboard.putNumber("[Climber]Speed", climberSpeeds);
        SmartDashboard.putNumber("[Climber]Position", climberPosition);
    }

    public static synchronized Climber getInstance() {
        return instance == null ? instance = new Climber() : instance;
    }
}