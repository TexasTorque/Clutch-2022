package org.texastorque.auto.commands;

import org.texastorque.inputs.Input;
import org.texastorque.modules.RotateManager;
import org.texastorque.torquelib.auto.TorqueCommand;

public class BallRotator extends TorqueCommand {
    public BallRotator() {
    }

    @Override
    protected void init() {
        Input.getInstance().getDrivebaseRotationInput().setRot(0);
    }

    @Override
    protected void continuous() {
        Input.getInstance().getDrivebaseRotationInput().setRot(RotateManager.getInstance().process());
    }

    @Override
    protected boolean endCondition() {
        return false;
    }

    @Override
    protected void end() {
        Input.getInstance().getDrivebaseRotationInput().setRot(0);
    }
}