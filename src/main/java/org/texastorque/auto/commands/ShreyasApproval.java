package org.texastorque.auto.commands;

import org.texastorque.inputs.Input;
import org.texastorque.torquelib.auto.TorqueCommand;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class ShreyasApproval extends TorqueCommand {

    @Override
    protected void init() {
        DriverStation.reportWarning("SHREYAS ACTIVATE MEEEEEE", false);
        SmartDashboard.putBoolean("Shreyas Approval", true);
    }

    @Override
    protected void continuous() {
    }

    @Override
    protected boolean endCondition() {
        return Input.getInstance().getClimberInput().getShreyasApproval();
    }

    @Override
    protected void end() {
        DriverStation.reportWarning("thank you shreyas :)", false);
        SmartDashboard.putBoolean("Shreyas Approval", false);
    }

}
