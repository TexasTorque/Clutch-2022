package org.texastorque.auto.sequences.mode5;

import org.texastorque.auto.commands.*;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.*;

public class Mode5RightB extends TorqueSequence {
    public Mode5RightB(String name) {
        super(name);

        init();
    }

    @Override
    protected void init() {
        // Start Intake, Automag
        addBlock(new TorqueBlock(
            new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
            new SetMagazine(BeltDirections.INTAKE, GateSpeeds.CLOSED)));

        // Run path
        addBlock(new TorqueBlock(
            new Pathplanner("rightbottompickuponeballandreturn")));

        // Shoot!
        addBlock(new TorqueBlock(new ShootAtTarget(4)));

        // Shut off
        addBlock(new TorqueBlock(
            new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED),
            new SetMagazine(BeltDirections.OFF, GateSpeeds.CLOSED)));
    }
}
