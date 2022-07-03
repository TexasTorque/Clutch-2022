/**
 * Copyright 2022 Texas Torque.
 * 
 * This file is part of Clutch-2022, which is not licensed for distribution.
 * For more details, see ./license.txt or write <jus@gtsbr.org>.
 */
package org.texastorque.auto.sequences;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import org.texastorque.Subsystems;
import org.texastorque.auto.commands.Creep;
import org.texastorque.auto.commands.Path;
import org.texastorque.auto.commands.Shoot;
import org.texastorque.auto.commands.Target;
import org.texastorque.subsystems.Intake.IntakeState;
import org.texastorque.subsystems.Magazine.BeltDirection;
import org.texastorque.subsystems.Shooter.ShooterState;
import org.texastorque.subsystems.Turret.TurretState;
import org.texastorque.torquelib.auto.TorqueBlock;
import org.texastorque.torquelib.auto.TorqueSequence;
import org.texastorque.torquelib.auto.commands.Execute;
import org.texastorque.torquelib.auto.commands.Wait;

public class Five extends TorqueSequence implements Subsystems {
    public Five() {
        super("Five");
        init();
    }

    @Override
    protected void init() {
        
        addBlock(new TorqueBlock(new Execute(() -> {
                                    //  magazine.setBeltDirection(BeltDirection.INTAKING);
                                     intake.setState(IntakeState.INTAKE);

                                     turret.setState(TurretState.POSITIONAL);
                                     turret.setPosition(172.15);

                                     shooter.setState(ShooterState.WARMUP);
                                     shooter.setFlywheelSpeed(1000);
                                     shooter.setHoodPosition(26);
                                 }), new Wait(.75)));
        addBlock(new TorqueBlock(new Path("Five1", true, 2, 1)));
        addBlock(new TorqueBlock(new Shoot(1450, 30, 172.15, true, 2)));
        addBlock(new TorqueBlock(new Path("Five2", false, 4, 2)));
        addBlock(new TorqueBlock(new Execute(() -> {
            turret.setState(TurretState.POSITIONAL);
            turret.setPosition(30);
            turret.setOffset(0);

            shooter.setState(ShooterState.WARMUP);
            shooter.setFlywheelSpeed(1400);
        })));
        addBlock(new TorqueBlock(new Path("Five3", false, 4, 2)));
        addBlock(new TorqueBlock(new Creep(2, new ChassisSpeeds(-.75, 0, 0)), 
                                 new Target(true, 3)));

        addBlock(new TorqueBlock(new Execute(() -> {
            turret.setOffset(0);
            magazine.setBeltDirection(BeltDirection.OFF);
            intake.setState(IntakeState.PRIMED);
            turret.setState(TurretState.CENTER);
        })));
    }
}
