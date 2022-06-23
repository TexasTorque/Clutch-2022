package org.texastorque.auto.commands;

import edu.wpi.first.wpilibj.Timer;

import org.texastorque.Subsystems;
import org.texastorque.subsystems.Shooter.ShooterState;
import org.texastorque.subsystems.Turret.TurretState;
import org.texastorque.torquelib.auto.TorqueCommand;

public final class Shoot extends TorqueCommand implements Subsystems {
    private final double rpm, hood, tur, time;
    private double start = -1;
    private final boolean stop;

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
        turret.setState(TurretState.POSITIONAL); // this wont work
        turret.setPosition(tur);
    }

    @Override
    protected final void continuous() {
        if (shooter.isReady() && start == -1)
            start = Timer.getFPGATimestamp();
    }

    @Override
    protected final boolean endCondition() {
        return start != -1 && (Timer.getFPGATimestamp() - start) > time;
    }

    @Override
    protected final void end() {
        shooter.setState(ShooterState.OFF);
        turret.setState(TurretState.CENTER);
    }
}