/**
 * Copyright 2022 Texas Torque.
 *
 * This file is part of Clutch-2022, which is not licensed for distribution.
 * For more details, see ./license.txt or write <jus@gtsbr.org>.
 */
package org.texastorque.subsystems;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.Ports;
import org.texastorque.Subsystems;
import org.texastorque.torquelib.base.TorqueDirection;
import org.texastorque.torquelib.base.TorqueMode;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.base.TorqueSubsystemState;
import org.texastorque.torquelib.control.TorquePersistentBoolean;
import org.texastorque.torquelib.motors.TorqueSparkMax;

public final class Magazine extends TorqueSubsystem implements Subsystems {
    private static volatile Magazine instance;

    public static final TorqueDirection MAG_UP = TorqueDirection.REVERSE, MAG_DOWN = TorqueDirection.FORWARD;

    private final TorqueSparkMax belt, gate;
    private TorqueDirection beltDirection, gateDirection;

    private Magazine() {
        belt = new TorqueSparkMax(Ports.MAGAZINE.BELT);
        belt.configureDumbCANFrame();
        gate = new TorqueSparkMax(Ports.MAGAZINE.GATE);
        gate.configureDumbCANFrame();
    }

    public final void setState(final TorqueDirection state, final TorqueDirection direction) {
        this.beltDirection = state;
        this.gateDirection = direction;
    }

    public final void setBeltDirection(final TorqueDirection direction) {
        this.beltDirection = direction;
    }

    public final void setGateDirection(final TorqueDirection direction) {
        this.gateDirection = direction;
    }

    public final void setManualState(final boolean up, final boolean down) {
        setBeltDirection(up ? MAG_UP : down ? MAG_DOWN : TorqueDirection.NEUTRAL);
        setGateDirection(up ? TorqueDirection.FORWARD : down ? TorqueDirection.REVERSE : TorqueDirection.NEUTRAL);
    }

    public void setManualBeltDirection(boolean up, boolean down) {
        if (up)
            setBeltDirection(MAG_UP);
        else if (down)
            setBeltDirection(MAG_DOWN);
        else
            setBeltDirection(TorqueDirection.NEUTRAL);
    }

    public void setManualGateDirection(boolean up, boolean down) {
        if (up)
            setGateDirection(TorqueDirection.FORWARD);
        else if (down)
            setGateDirection(TorqueDirection.REVERSE);
        else
            setGateDirection(TorqueDirection.NEUTRAL);
    }

    @Override
    public final void initialize(final TorqueMode mode) {
        this.beltDirection = TorqueDirection.OFF;
        this.gateDirection = TorqueDirection.OFF;
    }

    private boolean shootingStarted = false;
    private double shootingStartedTime = 0;
    private final double DROP_TIME = .05;

    private final TorquePersistentBoolean shooterReady = new TorquePersistentBoolean(5),
            turretLocked = new TorquePersistentBoolean(5),
            shouldShoot = new TorquePersistentBoolean(5);

    @Override
    public final void update(final TorqueMode mode) {
        if (intake.isOutaking()) {
            beltDirection = MAG_DOWN;
        }
        if (intake.isIntaking()) {
            beltDirection = MAG_UP;
        }

        if (shooter.isShooting()) {
            if (!shootingStarted) {
                shootingStarted = true;
                shootingStartedTime = Timer.getFPGATimestamp();
            }
            if (Timer.getFPGATimestamp() - shootingStartedTime <= DROP_TIME) {
                beltDirection = MAG_DOWN;
                gateDirection = TorqueDirection.REVERSE;
            }
        } else {
            shootingStarted = false;
        }

        shooterReady.add(shooter.isReady());
        turretLocked.add(turret.isLocked());
        shouldShoot.add(shooter.isReady() && turret.isLocked());

        // if (shooter.isReady() && turret.isLocked()) {
        if (shouldShoot.any()) {
            // if (shooterReady.any() && turretLocked.all()) {
            beltDirection = MAG_UP;
            gateDirection = TorqueDirection.FORWARD;
        }

        belt.setPercent(beltDirection.get());
        SmartDashboard.putNumber("Gate", gateDirection.get());
        gate.setPercent(gateDirection.get());

        TorqueSubsystemState.logState(beltDirection);
        TorqueSubsystemState.logState(gateDirection);

        SmartDashboard.putNumber("Belt Amps", belt.getCurrent());

        // Should fix the shooting early bug that was introduced by the used of
        //       if (shooterReady.any() && turretLocked.any())
        if (mode.isAuto())
            gateDirection = TorqueDirection.OFF;
    }

    public static final synchronized Magazine getInstance() {
        return instance == null ? instance = new Magazine() : instance;
    }
}
