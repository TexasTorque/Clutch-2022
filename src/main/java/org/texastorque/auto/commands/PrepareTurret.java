package org.texastorque.auto.commands;

import org.texastorque.inputs.AutoInput;
import org.texastorque.torquelib.auto.TorqueCommand;

/**
 * Have turret go to position to make auto faster
 */
public class PrepareTurret extends TorqueCommand {

    private final double turretPosition;

    public PrepareTurret(double turretPosition) {
        this.turretPosition = turretPosition;
    }

    @Override
    protected void init() {
        AutoInput.getInstance().setTurretPosition(turretPosition);
        AutoInput.getInstance().setSetTurretPosition(true);
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
