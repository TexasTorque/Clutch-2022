/**
 * Copyright 2022 Texas Torque.
 *
 * This file is part of Clutch-2022, which is not licensed for distribution.
 * For more details, see ./license.txt or write <jus@gtsbr.org>.
 */
package org.texastorque.auto.sequences;

import org.texastorque.Subsystems;
import org.texastorque.auto.commands.Path;
import org.texastorque.auto.commands.Shoot;
import org.texastorque.auto.commands.Target;
import org.texastorque.subsystems.Intake.IntakeState;
import org.texastorque.subsystems.Shooter.ShooterState;
import org.texastorque.subsystems.Turret.TurretState;
import org.texastorque.torquelib.auto.TorqueBlock;
import org.texastorque.torquelib.auto.TorqueSequence;
import org.texastorque.torquelib.auto.commands.TorqueExecute;
import org.texastorque.torquelib.base.TorqueDirection;

public class FiveCool extends TorqueSequence implements Subsystems {
    public FiveCool() {
        final double warmupDelta = 50;

        // Turn on subsystems and shoot the first ball

        final double rpm1 = 1400, hood1 = 20, turret1 = 165;

        addBlock(new TorqueBlock(new TorqueExecute(() -> {
            intake.setState(IntakeState.INTAKE);
            turret.setState(TurretState.POSITIONAL);
            turret.setPosition(turret1);
        })));

        addBlock(new TorqueBlock(new Shoot(rpm1, hood1, turret1, true, .5)));

        // Pick up and shoot the second and third balls

        final double rpm2 = 1575, hood2 = 30, turret2 = -95; // 1550 - 1575

        addBlock(new TorqueBlock(new TorqueExecute(() -> {
            magazine.setGateDirection(TorqueDirection.NEUTRAL);
            intake.setState(IntakeState.INTAKE);
            turret.setState(TurretState.POSITIONAL);
            turret.setPosition(turret2);
            shooter.setState(ShooterState.WARMUP);
            shooter.setFlywheelSpeed(rpm2 - warmupDelta);
            shooter.setHoodPosition(hood2);
        })));

        addBlock(new TorqueBlock(new Path("Five1", true, 4, 4)));
        addBlock(new TorqueBlock(new Shoot(rpm2, hood2, turret2, false, 1.4)));
        // addBlock(new TorqueBlock(new Shoot(rpm2, hood2, false, 2)));
        // addBlock(new TorqueBlock(new Shoot(rpm2, hood2, turret2, true, 1.5)));
        // addBlock(new TorqueBlock(new Target(true, 1.8)));

        // Pick up balls at human player

        final double rpm3 = 1625, hood3 = 28, turret3 = 180;

        addBlock(new TorqueBlock(new TorqueExecute(() -> {
            magazine.setGateDirection(TorqueDirection.NEUTRAL);
            intake.setState(IntakeState.INTAKE);
            turret.setState(TurretState.POSITIONAL);
            turret.setPosition(turret3);
            shooter.setState(ShooterState.WARMUP);
            shooter.setFlywheelSpeed(rpm3 - warmupDelta);
            shooter.setHoodPosition(hood3);
        })));

        addBlock(new TorqueBlock(new Path("Five2", false, 4, 2)));
        addBlock(new TorqueBlock(new Shoot(rpm3, hood3, turret3, true, 4)));
        // addBlock(new TorqueBlock(new Target(false, 4, rpm3, hood3, turret3)));

        // Turn off subsystems

        addBlock(new TorqueBlock(new TorqueExecute(() -> {
            magazine.setBeltDirection(TorqueDirection.NEUTRAL);
            intake.setState(IntakeState.PRIMED);
            turret.setState(TurretState.CENTER);
        })));
    }
}