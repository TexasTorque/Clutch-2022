package org.texastorque.auto.commands;

import org.texastorque.inputs.AutoInput;
import org.texastorque.torquelib.auto.TorqueCommand;

/**
 * Preset a hood to extend to. Useful to do prior to paths so shooter is ready.
 */
public class PrepareHood extends TorqueCommand {

    private final double hoodSetpoint;

    public PrepareHood(double hoodSetpoint) {
        this.hoodSetpoint = hoodSetpoint;
    }

    @Override
    protected void init() {
        AutoInput.getInstance().setHoodPosition(hoodSetpoint);
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
