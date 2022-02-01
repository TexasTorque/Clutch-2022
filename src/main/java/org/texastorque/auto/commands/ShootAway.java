package org.texastorque.auto.commands;

import org.texastorque.inputs.Input;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateDirections;
import org.texastorque.torquelib.auto.TorqueCommand;

import edu.wpi.first.wpilibj.Timer;

public class ShootAway extends TorqueCommand {
    private static final double shootTime = 2;

    private double time;

    @Override
    protected void init() {
        time = Timer.getFPGATimestamp();
    }

    @Override
    protected void continuous() {
        // Just get rid of it asap
        Input.getInstance().getShooterInput().setFlywheelSpeed(2000);
        Input.getInstance().getMagazineInput().setBeltDirection(BeltDirections.FORWARDS);
        Input.getInstance().getMagazineInput().setGateDirection(GateDirections.OPEN);
    }

    @Override
    protected boolean endCondition() {
        return Timer.getFPGATimestamp() - time >= shootTime;
    }

    @Override
    protected void end() {
        Input.getInstance().getShooterInput().setFlywheelSpeed(0);
        Input.getInstance().getMagazineInput().setBeltDirection(BeltDirections.OFF);
        Input.getInstance().getMagazineInput().setGateDirection(GateDirections.CLOSED);

    }

}
