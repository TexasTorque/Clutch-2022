package org.texastorque.auto.commands;

import org.texastorque.inputs.AutoInput;
import org.texastorque.subsystems.Climber.ServoDirection;
import org.texastorque.torquelib.auto.TorqueCommand;

public class SetClimberServos extends TorqueCommand {

    private ServoDirection servoDirection;

    public SetClimberServos(ServoDirection servoDirection) {

        this.servoDirection = servoDirection;
    }

    @Override
    protected void init() {
        AutoInput.getInstance().setServoDirection(servoDirection);
        System.out.println("Setting climber servos...");
    }

    @Override
    protected void continuous() {
    }

    @Override
    protected boolean endCondition() {
        return true;
    }

    @Override
    protected void end() {
        System.out.println("Climber servos set!");
    }

}
