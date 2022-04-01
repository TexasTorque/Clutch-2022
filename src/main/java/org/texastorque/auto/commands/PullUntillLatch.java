package org.texastorque.auto.commands;

import org.texastorque.inputs.AutoInput;
import org.texastorque.inputs.Feedback;
import org.texastorque.subsystems.Climber.ClimberDirection;
import org.texastorque.torquelib.auto.TorqueCommand;

/**
 * Go until the climber is latched :)
 */
public class PullUntillLatch extends TorqueCommand {

    @Override
    protected void init() {
        AutoInput.getInstance().setClimberDirection(ClimberDirection.PULL);
    }

    @Override
    protected void continuous() {
    }

    @Override
    protected boolean endCondition() {
        return Feedback.getInstance().getClimberFeedback().getLeftClaw()
                && Feedback.getInstance().getClimberFeedback().getRightClaw();
    }

    @Override
    protected void end() {
        AutoInput.getInstance().setClimberDirection(ClimberDirection.STOP);
    }

}
