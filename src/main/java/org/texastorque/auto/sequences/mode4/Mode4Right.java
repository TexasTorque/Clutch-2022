package org.texastorque.auto.sequences.mode4;

import org.texastorque.torquelib.auto.*;

import org.texastorque.auto.commands.*;

import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;

public class Mode4Right extends TorqueSequence {
    public Mode4Right(String name) {
        super(name);

        init();
    }

    @Override
    protected void init() {
        // Start Intake, Automag
        addBlock(new TorqueBlock(
                new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
                new SetMagazine(BeltDirections.INTAKE, GateSpeeds.CLOSED),
                new ShootAtTarget(2)));

        // Run path
        addBlock(new TorqueBlock(new Pathplanner("Mode4Right")));

        // Shoot!
        addBlock(new TorqueBlock(new ShootAtTarget(4)));

        // Shut off
        addBlock(new TorqueBlock(new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED),
                new SetMagazine(BeltDirections.OFF, GateSpeeds.CLOSED)));
    }
}
