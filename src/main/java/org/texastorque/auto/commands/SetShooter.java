package org.texastorque.auto.commands;

import org.texastorque.inputs.AutoInput;
import org.texastorque.torquelib.auto.TorqueCommand;

public class SetShooter extends TorqueCommand {
    private double rpm;
    private double hood;

    public SetShooter(double rpm, double hood) {
        this.rpm = rpm;
        this.hood = hood;
    }

    @Override
    protected void init() {
        AutoInput.getInstance().setFlywheelSpeed(rpm);
        AutoInput.getInstance().setHoodPosition(hood);
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
