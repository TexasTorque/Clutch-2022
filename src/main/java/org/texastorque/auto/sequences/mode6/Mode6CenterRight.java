package org.texastorque.auto.sequences.mode6;

import org.texastorque.auto.commands.*;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.*;

public class Mode6CenterRight extends TorqueSequence {
    public Mode6CenterRight(String name) {
        super(name);

        init();
    }

    @Override
    protected void init() {
        // Start Intake, Automag
        addBlock(new TorqueBlock(
            new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
            new SetMagazine(BeltDirections.INTAKE, GateSpeeds.CLOSED)));

        addBlock(new TorqueBlock(new Pathplanner("Mode6CenterRight_1")));

        addBlock(new TorqueBlock(new ShootAtTarget()));

        addBlock(new TorqueBlock(new Pathplanner("Mode6CenterRight_2", false)));

        addBlock(new TorqueBlock(new Wait(3)));

        addBlock(new TorqueBlock(new Pathplanner("Mode6CenterRight_3", false)));

        addBlock(new TorqueBlock(new ShootAtTarget()));

        addBlock(new TorqueBlock(new Pathplanner("Mode6CenterRight_4", false)));

        addBlock(new TorqueBlock(new ShootAtTarget()));

        // Shut off
        addBlock(new TorqueBlock(
            new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED),
            new SetMagazine(BeltDirections.OFF, GateSpeeds.CLOSED)));
    }
}
