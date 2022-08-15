/**
 * Copyright 2022 Texas Torque.
 * 
 * This file is part of Clutch-2022, which is not licensed for distribution.
 * For more details, see ./license.txt or write <jus@gtsbr.org>.
 */
package org.texastorque.auto.commands;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.Timer;
import org.texastorque.Subsystems;
import org.texastorque.torquelib.auto.TorqueCommand;

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
        drivebase.setSpeeds(speeds);
    }

    @Override
    protected final void continuous() {}

    @Override
    protected final boolean endCondition() {
        return (Timer.getFPGATimestamp() - start) > time;
    }

    @Override
    protected final void end() {
        drivebase.setSpeeds(new ChassisSpeeds(0, 0, 0));
    }
}