package org.texastorque.auto.sequences.mode6;

import org.texastorque.auto.commands.*;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.*;

public class Mode6Right extends TorqueSequence {
    public Mode6Right(String name) {
        super(name);

        init();
    }

    @Override
    protected void init() {
        // Shoot preload
        addBlock(new TorqueBlock(new ShootAtTarget()));

        // Start Intake, Automag
        addBlock(new TorqueBlock(
                new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
                new SetMagazine(BeltDirections.INTAKE, GateSpeeds.CLOSED)));

        // Run path
        addBlock(new TorqueBlock(new Pathplanner("Mode6Right_1")));

        // Shoot 2
        addBlock(new TorqueBlock((new ShootAtTarget())));

        // Get more balls
        addBlock(new TorqueBlock(new Pathplanner("Mode6Right_2", false)));

        // Wait x seconds for human player
        addBlock(new TorqueBlock(new Wait(3)));

        // Go to shoot
        addBlock(new TorqueBlock(new Pathplanner("Mode6Right_3", false)));

        // Shoot!
        addBlock(new TorqueBlock(new ShootAtTarget()));

        // Shut off
        addBlock(new TorqueBlock(
                new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED),
                new SetMagazine(BeltDirections.OFF, GateSpeeds.CLOSED)));
    }
}
