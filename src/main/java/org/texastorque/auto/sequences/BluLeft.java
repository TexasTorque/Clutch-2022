package org.texastorque.auto.sequences;

import org.texastorque.auto.commands.Pathplanner;
import org.texastorque.auto.commands.SetIntake;
import org.texastorque.auto.commands.SetMagazine;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.TorqueBlock;
import org.texastorque.torquelib.auto.TorqueSequence;

/**
 * Example sequence.
 */
public class BluLeft extends TorqueSequence {
    public BluLeft(String name) {
        super(name);

        init();
    }

    @Override
    protected void init() {
        // Start Intake, Automag
        addBlock(new TorqueBlock(new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
                new SetMagazine(BeltDirections.FORWARDS, GateSpeeds.CLOSED)));
        // Run path
        addBlock(new TorqueBlock(new Pathplanner("BLULeft")));
        // Shoot
        // TODO
    }
}
