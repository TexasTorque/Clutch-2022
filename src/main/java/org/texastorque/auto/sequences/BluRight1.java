package org.texastorque.auto.sequences;

import org.texastorque.auto.commands.Pathplanner;
import org.texastorque.auto.commands.SetIntake;
import org.texastorque.auto.commands.SetMagazine;
import org.texastorque.auto.commands.ShootAtTarget;
import org.texastorque.auto.commands.Wait;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.TorqueBlock;
import org.texastorque.torquelib.auto.TorqueSequence;

/**
 * Example sequence.
 */
public class BluRight1 extends TorqueSequence {
    public BluRight1(String name) {
        super(name);

        init();
    }

    @Override
    protected void init() {
        // Shoot preload
        addBlock(new TorqueBlock(new ShootAtTarget()));

        // Start Intake, Automag
        addBlock(new TorqueBlock(new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
                new SetMagazine(BeltDirections.BACKWARDS, GateSpeeds.CLOSED)));

        // Run path
        addBlock(new TorqueBlock(new Pathplanner("BLURight1a")));

        // Shoot 2 balorqueBlock(new ShootAtTarget()));

        // Get more balls
        addBlock(new TorqueBlock(new Pathplanner("BLURight1b", false)));

        // Wait x seconds for human player
        addBlock(new TorqueBlock(new Wait(3)));

        // Go to shoot
        addBlock(new TorqueBlock(new Pathplanner("BLURight1c", false)));

        // Shoot!
        addBlock(new TorqueBlock(new ShootAtTarget()));

        // Shut off
        addBlock(new TorqueBlock(new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED),
                new SetMagazine(BeltDirections.OFF, GateSpeeds.CLOSED)));
    }
}
