package org.texastorque.auto.commands;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.inputs.AutoInput;
import org.texastorque.inputs.Feedback;
import org.texastorque.inputs.State;
import org.texastorque.inputs.State.AutoClimb;
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
        State.getInstance().setAutoClimb(AutoClimb.ON);
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
        State.getInstance().setAutoClimb(AutoClimb.OFF);
    }
}
