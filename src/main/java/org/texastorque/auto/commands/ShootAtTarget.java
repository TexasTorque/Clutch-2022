package org.texastorque.auto.commands;

import org.texastorque.inputs.Feedback;
import org.texastorque.inputs.Input;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateDirections;
import org.texastorque.torquelib.auto.TorqueCommand;

import edu.wpi.first.wpilibj.Timer;

public class ShootAtTarget extends TorqueCommand {
    private static final double tolerance = 100; // 100 rpm tolerance
    private static final double magOutputTime = 2; // 2 seconds

    private boolean done = false;
    private boolean runMag = false;
    private double startMagTime = 0;

    @Override
    protected void init() {
    }

    @Override
    protected void continuous() {
        // get LL distance
        double distance = Feedback.getInstance().getLimelightFeedback().getDistance();

        // plug into formula
        // TODO: create formula
        double outputRPM = distance * 500;

        // output to input
        Input.getInstance().getShooterInput().setFlywheelSpeed(outputRPM);

        if (!runMag) {
            // check if rpm is in range (+-x)
            if (Math.abs(outputRPM - Feedback.getInstance().getShooterFeedback().getRPM()) < tolerance) {
                // if so, launch magazine for x seconds
                runMag = true;
                startMagTime = Timer.getFPGATimestamp();
            }
        } else {
            Input.getInstance().getMagazineInput().setBeltDirection(BeltDirections.FORWARDS);
            Input.getInstance().getMagazineInput().setGateDirection(GateDirections.OPEN);
            if (Timer.getFPGATimestamp() - startMagTime >= magOutputTime)
                done = true;
        }

    }

    @Override
    protected boolean endCondition() {
        return done;
    }

    @Override
    protected void end() {
        done = false;
        runMag = false;
        Input.getInstance().getShooterInput().setFlywheelSpeed(0);
        Input.getInstance().getMagazineInput().setBeltDirection(BeltDirections.OFF);
        Input.getInstance().getMagazineInput().setGateDirection(GateDirections.CLOSED);
    }

}
