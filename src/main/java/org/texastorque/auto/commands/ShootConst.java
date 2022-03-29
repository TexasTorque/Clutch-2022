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
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.TorqueCommand;

public class ShootConst extends TorqueCommand {
    private final double magOutputTime;

    private boolean stop = false;
    private boolean done = false;
    private boolean runMag = false;
    private double startMagTime = 0;
    private double rpm, hood, turret;

    private int readyIterations = 0;
    private final int neededReadyIterations = 4;

    public ShootConst(double rpm, double hood, double turret) {
        this(rpm, hood, turret, 2.);
    }

    public ShootConst(double rpm, double hood, double turret, double magOutputTime) {
        this.rpm = rpm;
        this.hood = hood;
        this.turret = turret;
        this.magOutputTime = magOutputTime;
    }

    @Override
    protected void init() {
        AutoInput.getInstance().setSetTurretPosition(true);
        AutoInput.getInstance().setTurretPosition(turret);
        AutoInput.getInstance().setHoodPosition(hood);
        AutoInput.getInstance().setFlywheelSpeed(rpm);
    }

    @Override
    protected void continuous() {
        if (!runMag) {
            // check if rpm is in range (+-x)
            if (Math.abs(rpm - Feedback.getInstance().getShooterFeedback().getRPM()) < Constants.SHOOTER_ERROR) {
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