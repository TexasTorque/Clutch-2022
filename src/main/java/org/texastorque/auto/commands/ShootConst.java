package org.texastorque.auto.commands;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.Timer;
import org.texastorque.constants.Constants;
import org.texastorque.inputs.AutoInput;
import org.texastorque.inputs.Feedback;
import org.texastorque.inputs.Input;
import org.texastorque.inputs.State;
import org.texastorque.inputs.State.AutomaticMagazineState;
import org.texastorque.inputs.State.TurretState;
import org.texastorque.subsystems.Drivebase;
import org.texastorque.subsystems.Turret;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.TorqueCommand;

public class ShootConst extends TorqueCommand {
    private final double magOutputTime;

    private boolean stop = false, done = false, runMag = false;
    private double rpm, hood, turret, startMagTime = 0;

    private int readyIterations = 0;
    private final int neededReadyIterations = 2;

    public ShootConst(double rpm, double hood, double turret) {
        this.rpm = rpm;
        this.hood = hood;
        this.turret = turret;
        this.stop = false;
        this.magOutputTime = 1;
    }

    public ShootConst(double rpm, double hood, double turret, boolean stop) {
        this.rpm = rpm;
        this.hood = hood;
        this.turret = turret;
        this.stop = stop;
        this.magOutputTime = 1;
    }

    public ShootConst(double rpm, double hood, double turret, boolean stop, double magOutputTime) {
        this.rpm = rpm;
        this.hood = hood;
        this.turret = turret;
        this.stop = stop;
        this.magOutputTime = magOutputTime;
    }

    private double timeMagDown;

    @Override
    protected void init() {
        AutoInput.getInstance().setSetTurretPosition(true);
        AutoInput.getInstance().setTurretPosition(turret);
        AutoInput.getInstance().setHoodPosition(hood);
        AutoInput.getInstance().setFlywheelSpeed(rpm);
        timeMagDown = Timer.getFPGATimestamp();
    }

    @Override
    protected void continuous() {
        if (Timer.getFPGATimestamp() - timeMagDown < .1) {
            AutoInput.getInstance().setGateDirection(GateSpeeds.CLOSED);
            AutoInput.getInstance().setBeltDirection(BeltDirections.OUTTAKE);
        } else {
            AutoInput.getInstance().setGateDirection(GateSpeeds.CLOSED);
            AutoInput.getInstance().setBeltDirection(BeltDirections.OFF);
        }
        if (!runMag) {
            // check if rpm is in range (+-x)
            if (Math.abs(rpm - Feedback.getInstance().getShooterFeedback().getRPM()) < Constants.SHOOTER_ERROR
                    && Math.abs(Turret.getInstance().getDegrees() - turret) < 5) {
                if (readyIterations >= neededReadyIterations) {
                    // if so, launch magazine for x seconds
                    runMag = true;
                    startMagTime = Timer.getFPGATimestamp();
                } else {
                    readyIterations++;
                }
            } else {
                readyIterations = 0;
            }
        } else {
            State.getInstance().setTurretState(TurretState.OFF);
            AutoInput.getInstance().setBeltDirection(BeltDirections.INTAKE);
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
        done = false;
        runMag = false;
        if (stop)
            AutoInput.getInstance().setFlywheelSpeed(0);
        AutoInput.getInstance().setGateDirection(GateSpeeds.CLOSED);
        State.getInstance().setTurretState(TurretState.OFF);
    }
}
