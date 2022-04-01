package org.texastorque.auto.commands;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.inputs.AutoInput;
import org.texastorque.inputs.Feedback;
import org.texastorque.inputs.State;
import org.texastorque.inputs.State.AutoClimb;
import org.texastorque.subsystems.Climber.ClimberDirection;
import org.texastorque.torquelib.auto.TorqueCommand;

public class ClimbToSetpoint extends TorqueCommand {
    private static double allowedError = 3;

    private double leftSetpoint;
    private double rightSetpoint;

    public ClimbToSetpoint(double leftSetpoint, double rightSetpoint) {
        this.leftSetpoint = leftSetpoint;
        this.rightSetpoint = rightSetpoint;
    }

    @Override
    protected void init() {
        System.out.println("Starting climb to setpoint!");
        State.getInstance().setAutoClimb(AutoClimb.ON);
        if (leftSetpoint > Feedback.getInstance().getClimberFeedback().getLeftPosition()) {
            AutoInput.getInstance().setClimberDirection(ClimberDirection.PUSH);
        } else {
            AutoInput.getInstance().setClimberDirection(ClimberDirection.PULL);
        }
        AutoInput.getInstance().setClimberLeftSetpoint(leftSetpoint);
        AutoInput.getInstance().setClimberRightSetpoint(rightSetpoint);

    }

    @Override
    protected void continuous() {
    }

    @Override
    protected boolean endCondition() {
        return Math.abs(Feedback.getInstance()
                .getClimberFeedback()
                .getLeftPosition() -
                leftSetpoint) < allowedError &&
                Math.abs(
                        Feedback.getInstance().getClimberFeedback().getRightPosition() -
                                rightSetpoint) < allowedError;
    }

    @Override
    protected void end() {
        System.out.println("Climb to setpoint done!");
        State.getInstance().setAutoClimb(AutoClimb.OFF);
    }
}
