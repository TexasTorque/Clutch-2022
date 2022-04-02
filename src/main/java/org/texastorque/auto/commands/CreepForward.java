package org.texastorque.auto.commands;

import org.texastorque.inputs.AutoInput;
import org.texastorque.inputs.Input;
import org.texastorque.modules.RotateManager;
import org.texastorque.subsystems.Drivebase;
import org.texastorque.torquelib.auto.TorqueCommand;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.Timer;

public class CreepForward extends TorqueCommand {
    private double speed, time, start;

    public CreepForward(double time) {
        this.time = time;
        this.speed = .4;
    }

    public CreepForward(double time, double speed) {
        this.time = time;
        this.speed = speed;
    }

    @Override
    protected void init() {
        start = Timer.getFPGATimestamp();
        AutoInput.getInstance().setDriveStates(
                Drivebase.getInstance().kinematics.toSwerveModuleStates(new ChassisSpeeds(-speed, 0, 0)));
    }

    @Override
    protected void continuous() {

    }

    @Override
    protected boolean endCondition() {
        return (Timer.getFPGATimestamp() - start) > time;
    }

    @Override
    protected void end() {
        AutoInput.getInstance()
                .setDriveStates(Drivebase.getInstance().kinematics.toSwerveModuleStates(new ChassisSpeeds(0, 0, 0)));
    }
}