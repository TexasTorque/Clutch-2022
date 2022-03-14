package org.texastorque.auto.commands;

import org.texastorque.inputs.AutoInput;
import org.texastorque.torquelib.auto.TorqueCommand;

/**
 * Preset a hood to extend to. Useful to do prior to paths so shooter is ready.
 */
public class PrepareShooter extends TorqueCommand {

    private final double hoodSetpoint;
    private final double RPMSetpoint;

    public PrepareShooter(double hoodSetpoint) {
        this(hoodSetpoint, 0);
    }

    public PrepareShooter(double hoodSetpoint, double RPMSetpoint) {
        this.hoodSetpoint = hoodSetpoint;
        this.RPMSetpoint = RPMSetpoint;
    }

    @Override
    protected void init() {
        AutoInput.getInstance().setHoodPosition(hoodSetpoint);
        AutoInput.getInstance().setFlywheelSpeed(RPMSetpoint);
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
