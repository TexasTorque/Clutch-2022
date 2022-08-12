/**
 * Copyright 2022 Texas Torque.
 * 
 * This file is part of Clutch-2022, which is not licensed for distribution.
 * For more details, see ./license.txt or write <jus@gtsbr.org>.
 */
package org.texastorque.auto.commands;

import edu.wpi.first.wpilibj.Timer;
import org.texastorque.Subsystems;
import org.texastorque.subsystems.Shooter.ShooterState;
import org.texastorque.subsystems.Turret.TurretState;
import org.texastorque.torquelib.auto.TorqueCommand;
import org.texastorque.torquelib.base.TorqueDirection;

public final class Target extends TorqueCommand implements Subsystems {
    private final double time;
    private double start = -1;
    private final boolean stop;

    public Target(final boolean stop, final double time) {
        this.time = time;
        this.stop = stop;
    }

    @Override
    protected final void init() {
        shooter.setState(ShooterState.REGRESSION);
        turret.setState(TurretState.TRACK);
    }

    @Override
    protected final void continuous() {
        if (shooter.isReady() && start == -1) start = Timer.getFPGATimestamp();
    }

    @Override
    protected final boolean endCondition() {
        return start != -1 && (Timer.getFPGATimestamp() - start) > time;
    }

    @Override
    protected final void end() {
        if (stop) {
            shooter.setState(ShooterState.OFF);
            turret.setState(TurretState.CENTER);
        } else {
            shooter.setState(ShooterState.SETPOINT);
            shooter.setFlywheelSpeed(1000);
        }

        magazine.setGateDirection(TorqueDirection.OFF);
    }
}