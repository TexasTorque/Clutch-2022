package org.texastorque.auto.commands;

import org.texastorque.inputs.AutoInput;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.TorqueCommand;

public class SetMagazine extends TorqueCommand {
    private BeltDirections beltDirection;
    private GateSpeeds gateSpeeds;

    public SetMagazine(BeltDirections beltDirection, GateSpeeds gateSpeeds) {
        this.beltDirection = beltDirection;
        this.gateSpeeds = gateSpeeds;
    }

    @Override
    protected void init() {
        AutoInput.getInstance().setBeltDirection(beltDirection);
        AutoInput.getInstance().setGateDirection(gateSpeeds);
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
