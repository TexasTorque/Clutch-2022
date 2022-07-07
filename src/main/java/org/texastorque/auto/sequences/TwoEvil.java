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
import org.texastorque.subsystems.Intake.IntakeState;
import org.texastorque.subsystems.Magazine.BeltDirection;
import org.texastorque.subsystems.Shooter.ShooterState;
import org.texastorque.subsystems.Turret.TurretState;
import org.texastorque.torquelib.auto.TorqueBlock;
import org.texastorque.torquelib.auto.TorqueSequence;
import org.texastorque.torquelib.auto.commands.Execute;
import org.texastorque.torquelib.auto.commands.Wait;

public class TwoEvil extends TorqueSequence implements Subsystems {
    public TwoEvil() {
        super("TwoEvil");
        init();
    }

    @Override
    protected void init() {
        final double firstTurret = -165.15;
        
        addBlock(new TorqueBlock(new Wait(2), new Execute(() -> {
                                     // magazine.setBeltDirection(BeltDirection.INTAKING);
                                     intake.setState(IntakeState.INTAKE);

                                     turret.setState(TurretState.POSITIONAL);
                                     turret.setPosition(firstTurret);

                                     shooter.setState(ShooterState.WARMUP);
                                     shooter.setFlywheelSpeed(1000);
                                     shooter.setHoodPosition(26);
                                 })));
        addBlock(new TorqueBlock(new Path("Two1", true, 2, 1)));
        addBlock(new TorqueBlock(new Shoot(1500, 20, firstTurret, true, 2)));
        addBlock(new TorqueBlock(new Path("Two2", false, 1, .5)));
        addBlock(new TorqueBlock(new Shoot(800, 30, 160, true, 1)));
        addBlock(new TorqueBlock(new Execute(() -> {
            magazine.setBeltDirection(BeltDirection.OFF);
            intake.setState(IntakeState.PRIMED);
            turret.setState(TurretState.CENTER);
        })));
    }
}
