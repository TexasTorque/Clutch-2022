package org.texastorque.auto.commands;

import edu.wpi.first.wpilibj.Timer;
import org.texastorque.constants.Constants;
import org.texastorque.inputs.AutoInput;
import org.texastorque.inputs.Feedback;
import org.texastorque.inputs.Input;
import org.texastorque.inputs.State;
import org.texastorque.inputs.State.AutomaticMagazineState;
import org.texastorque.inputs.State.TurretState;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.TorqueCommand;

public class ShootAtTarget extends TorqueCommand {
    private final double magOutputTime;

    private boolean done = false;
    private boolean runMag = false;
    private double startMagTime = 0;

    private boolean turretOn = true;
    private double distance;
    private double outputRPM;

    public ShootAtTarget() {
        this.magOutputTime = 2; // TBD
    }

    public ShootAtTarget(double magOutputTime) {
        this.magOutputTime = magOutputTime;
    }

    public ShootAtTarget(boolean turretOn) {
        this();
        this.turretOn = turretOn;
    }

    public ShootAtTarget(double magOutputTime, boolean turretOn) {
        this.magOutputTime = magOutputTime;
        this.turretOn = turretOn;
    }

    @Override
    protected void init() {
        // distance = Feedback.getInstance().getLimelightFeedback().getDistance();
        // outputRPM = Input.getInstance().getShooterInput().regressionRPM(distance);
        // AutoInput.getInstance().setFlywheelSpeed(outputRPM);
        // AutoInput.getInstance().setHoodPosition(Input.getInstance().getShooterInput().regressionHood(distance));
        outputRPM = 1500;

        AutoInput.getInstance().setFlywheelSpeed(outputRPM);
        if (turretOn) {
            State.getInstance().setTurretState(TurretState.ON);
        } else {
            State.getInstance().setTurretState(TurretState.OFF);
        }
        System.out.println("Shoot at target locked & loaded!");
    }

    @Override
    protected void continuous() {
        if (!runMag) {
            // check if rpm is in range (+-x)
            if (Math.abs(outputRPM -
                    Feedback.getInstance().getShooterFeedback().getRPM()) < Constants.SHOOTER_ERROR) {
                // if so, launch magazine for x seconds
                runMag = true;
                startMagTime = Timer.getFPGATimestamp();
            }
        } else {
            AutoInput.getInstance().setGateDirection(GateSpeeds.OPEN);
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
        System.out.println("ShootAtTarget done, have a good day!");
        done = false;
        runMag = false;
        AutoInput.getInstance().setFlywheelSpeed(0);
        AutoInput.getInstance().setGateDirection(GateSpeeds.CLOSED);
        State.getInstance().setTurretState(TurretState.OFF);
    }
}
