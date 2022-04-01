package org.texastorque.auto.commands;

import org.texastorque.inputs.AutoInput;
import org.texastorque.inputs.Feedback;
import org.texastorque.inputs.State;
import org.texastorque.inputs.State.AutoClimb;
import org.texastorque.subsystems.Climber.ClimberDirection;
import org.texastorque.torquelib.auto.TorqueCommand;

/**
 * Go until the climber is latched :)
 */
public class PullUntillLatch extends TorqueCommand {

    @Override
    protected void init() {
        System.out.println("Pulling until latch!");
        State.getInstance().setAutoClimb(AutoClimb.ON);
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
        System.out.println("Latched :)!");
        AutoInput.getInstance().setClimberDirection(ClimberDirection.STOP);
        State.getInstance().setAutoClimb(AutoClimb.OFF);
    }

}
