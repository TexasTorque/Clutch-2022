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
import org.texastorque.torquelib.util.TorqueUtil;

public class Two extends TorqueSequence implements Subsystems {
    public Two() {
        super("Two");
        init();
    }

    @Override
    protected void init() {
        // TorqueUtil.outOfDate();

        addBlock(new TorqueBlock(new Path("Two1", true, 2, 1), new Execute(() -> {
                                     magazine.setBeltDirection(BeltDirection.INTAKING);
                                     intake.setState(IntakeState.INTAKE);

                                     turret.setState(TurretState.POSITIONAL);
                                     turret.setPosition(-171.15);

                                     shooter.setState(ShooterState.SETPOINT);
                                     shooter.setFlywheelSpeed(1400);
                                     shooter.setHoodPosition(26);
                                 })));
        addBlock(new TorqueBlock(new Shoot(1600, 30, -171.15, true, 1.6)));
        addBlock(new TorqueBlock(new Execute(() -> {
            magazine.setBeltDirection(BeltDirection.OFF);
            intake.setState(IntakeState.PRIMED);
        })));
    }
}
