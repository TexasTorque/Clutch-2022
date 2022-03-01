package org.texastorque.auto.commands;

import edu.wpi.first.wpilibj.Timer;
import org.texastorque.inputs.Input;
import org.texastorque.inputs.State;
import org.texastorque.inputs.State.AutomaticMagazineState;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.TorqueCommand;

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
        Input.getInstance().getMagazineInput().setBeltDirection(
            BeltDirections.OUTTAKE);
        Input.getInstance().getMagazineInput().setGateDirection(
            GateSpeeds.OPEN);
    }

    @Override
    protected boolean endCondition() {
        return Timer.getFPGATimestamp() - time >= shootTime;
    }

    @Override
    protected void end() {
        Input.getInstance().getShooterInput().setFlywheelSpeed(0);
        Input.getInstance().getMagazineInput().setBeltDirection(
            BeltDirections.OFF);
        Input.getInstance().getMagazineInput().setGateDirection(
            GateSpeeds.CLOSED);
        State.getInstance().setAutomaticMagazineState(
            AutomaticMagazineState.OFF);
    }
}
