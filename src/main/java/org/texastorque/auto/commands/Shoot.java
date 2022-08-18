/**
 * Copyright 2022 Texas Torque.
 * 
 * This file is part of Clutch-2022, which is not licensed for distribution.
 * For more details, see ./license.txt or write <jus@gtsbr.org>.
 */
package org.texastorque.auto.commands;

import edu.wpi.first.wpilibj.Timer;
import org.texastorque.Subsystems;
import org.texastorque.torquelib.base.TorqueDirection;
import org.texastorque.subsystems.Shooter.ShooterState;
import org.texastorque.subsystems.Turret.TurretState;
import org.texastorque.torquelib.auto.TorqueCommand;

public final class Shoot extends TorqueCommand implements Subsystems {
    private final double rpm, hood, tur, time;
    private double start = -1;
    private final boolean stop;

    public Shoot(final double rpm, final double hood, final boolean stop, final double time) {
        this.rpm = rpm;
        this.hood = hood;
        this.tur = Double.NaN; 
        this.time = time;
        this.stop = stop;
    }

    public Shoot(final double rpm, final double hood, final double tur, final boolean stop, final double time) {
        this.rpm = rpm;
        this.hood = hood;
        this.tur = tur;
        this.time = time;
        this.stop = stop;
    }

    @Override
    protected final void init() {
        shooter.setState(ShooterState.SETPOINT);
        shooter.setFlywheelSpeed(rpm);
        shooter.setHoodPosition(hood);
        turret.setState(TurretState.POSITIONAL);
        turret.setPosition(Double.isNaN(tur) ? turret.calculateAngleWithOdometry() : tur);
        turret.setPosition(tur);
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
        if (stop)
            shooter.setState(ShooterState.OFF);
        else {
            shooter.setState(ShooterState.SETPOINT);
            shooter.setFlywheelSpeed(1000);
        }

        // turret.setState(TurretState.CENTER);
        magazine.setGateDirection(TorqueDirection.OFF);
    }
}