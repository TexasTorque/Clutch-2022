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

<<<<<<< HEAD
    private boolean stop = false, done = false, runMag = false;
    private double rpm, hood, turret, startMagTime = 0;
=======
    private boolean stop = false;
    private boolean done = false;
    private boolean runMag = false;
    private double startMagTime = 0;
    private double rpm, hood, turret;
>>>>>>> 8f52bb6445a34761be5bb2e59df29c37385793e3

    private int readyIterations = 0;
    private final int neededReadyIterations = 4;

    public ShootConst(double rpm, double hood, double turret) {
<<<<<<< HEAD
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
=======
        ShootConst(rpm, hood, turret, 2.);
    }

    public ShootConst(double rpm, double hood, double turret, double magOutputTime) {
        this.rpm = rpm;
        this.hood = hood;
        this.turret = turret;
>>>>>>> 8f52bb6445a34761be5bb2e59df29c37385793e3
        this.magOutputTime = magOutputTime;
    }

    @Override
    protected void init() {
<<<<<<< HEAD
        AutoInput.getInstance().setSetTurretPosition(true);
        AutoInput.getInstance().setTurretPosition(turret);
        AutoInput.getInstance().setHoodPosition(hood);
        AutoInput.getInstance().setFlywheelSpeed(rpm);
=======
        AutoInput.getInstance().setSetTurretPosition(false);

>>>>>>> 8f52bb6445a34761be5bb2e59df29c37385793e3
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
<<<<<<< HEAD
=======

>>>>>>> 8f52bb6445a34761be5bb2e59df29c37385793e3
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
<<<<<<< HEAD
=======
        System.out.println("ShootAtTarget done, have a good day!");
>>>>>>> 8f52bb6445a34761be5bb2e59df29c37385793e3
        done = false;
        runMag = false;
        if (stop)
            AutoInput.getInstance().setFlywheelSpeed(0);
        AutoInput.getInstance().setGateDirection(GateSpeeds.CLOSED);
        State.getInstance().setTurretState(TurretState.OFF);
    }
}
