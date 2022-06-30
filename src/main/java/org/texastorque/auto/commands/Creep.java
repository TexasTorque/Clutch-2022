package org.texastorque.auto.commands;

import org.texastorque.Subsystems;
import org.texastorque.subsystems.Drivebase.DrivebaseState;
import org.texastorque.torquelib.auto.TorqueCommand;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.Timer;

public final class Creep extends TorqueCommand implements Subsystems {
    private final double time; 
    private double start;
    private ChassisSpeeds speeds;

    public Creep(final double time, final ChassisSpeeds speeds) {
        this.time = time;
        this.speeds = speeds;
    }

    @Override
    protected final void init() {
        start = Timer.getFPGATimestamp();
        drivebase.setState(DrivebaseState.ROBOT_RELATIVE);
        drivebase.setSpeeds(speeds);
    }

    @Override
    protected final void continuous() {
    }

    @Override
    protected final boolean endCondition() {
        return(Timer.getFPGATimestamp() - start) > time;
    }

    @Override
    protected final void end() {
        drivebase.setSpeeds(new ChassisSpeeds(0, 0, 0));
    }
}