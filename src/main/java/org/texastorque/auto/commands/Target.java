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
    private final double time, rpm, hood, tur;
    private double start = -1;
    private final boolean stop;

    public Target(final boolean stop, final double time) { this(stop, time, -1); }

    public Target(final boolean stop, final double time, final double tur) { this(stop, time, -1, -1, tur); }

    public Target(final boolean stop, final double time, final double rpm, final double hood, final double tur) {
        this.time = time;
        this.stop = stop;
        this.rpm = rpm;
        this.hood = hood;
        this.tur = tur;
    }

    @Override
    protected final void init() {
        // if (tur != -1)
        // turret.setPosition(tur);
        turret.setState(TurretState.TRACK);
        if (rpm == -1)
            shooter.setState(ShooterState.REGRESSION);
        else {
            shooter.setFlywheelSpeed(rpm);
            shooter.setHoodPosition(hood);
        }
    }

    @Override
    protected final void continuous() {
        // I want to die
        if ((Timer.getFPGATimestamp() - start) > .5) magazine.setManualState(true, false);

        if (shooter.isReady() && start == -1) start = Timer.getFPGATimestamp();
    }

    @Override
    protected final boolean endCondition() {
        // return start != -1 && (Timer.getFPGATimestamp() - start) > time;
        return false;
    }

    @Override
    protected final void end() {
        if (false) {
            shooter.setState(ShooterState.OFF);
            turret.setState(TurretState.CENTER);
        } else {
            shooter.setState(ShooterState.SETPOINT);
            shooter.setFlywheelSpeed(1000);
        }

        magazine.setGateDirection(TorqueDirection.OFF);
    }
}