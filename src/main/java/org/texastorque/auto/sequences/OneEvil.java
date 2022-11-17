/**
 * Copyright 2022 Texas Torque.
 *
 * This file is part of Clutch-2022, which is not licensed for distribution.
 * For more details, see ./license.txt or write <jus@gtsbr.org>.
 */
package org.texastorque.auto.sequences;

import org.texastorque.Input;
import org.texastorque.Subsystems;
import org.texastorque.auto.commands.Path;
import org.texastorque.auto.commands.Shoot;
import org.texastorque.subsystems.Intake.IntakeState;
import org.texastorque.torquelib.auto.TorqueBlock;
import org.texastorque.torquelib.auto.TorqueSequence;
import org.texastorque.torquelib.auto.commands.TorqueExecute;
import org.texastorque.torquelib.base.TorqueDirection;

public class OneEvil extends TorqueSequence implements Subsystems {
    public OneEvil() {
        addBlock(new TorqueBlock(new Shoot(1350, 10, 0, true, 3)));
        addBlock(new TorqueBlock(new Path("One1", true, 1, .5),
                                 new TorqueExecute(() -> { intake.setState(IntakeState.INTAKE); })));
        addBlock(new TorqueBlock(new Path("One2", false, 1, .5)));
        addBlock(new TorqueBlock(new Shoot(1200, 30, -135, true, 1)));
        addBlock(new TorqueBlock(new TorqueExecute(() -> {
            magazine.setBeltDirection(TorqueDirection.OFF);
            intake.setState(IntakeState.PRIMED);
            Input.getInstance().invertDrivebaseControls();
        })));
    }
}
