package org.texastorque.auto.commands;

import org.texastorque.inputs.AutoInput;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.torquelib.auto.TorqueCommand;

public class SetIntake extends TorqueCommand {
    private IntakePosition position;
    private IntakeDirection direction;

    public SetIntake(IntakePosition position, IntakeDirection direction) {
        this.position = position;
        this.direction = direction;
    }

    @Override
    protected void init() {
        AutoInput.getInstance().setIntakePosition(position);
        AutoInput.getInstance().setIntakeSpeed(direction);
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
    }

}
