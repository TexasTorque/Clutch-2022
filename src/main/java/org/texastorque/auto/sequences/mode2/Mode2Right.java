package org.texastorque.auto.sequences.mode2;

import org.texastorque.torquelib.auto.*;

import org.texastorque.auto.commands.*;

import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;

public class Mode2Right extends TorqueSequence {
    public Mode2Right(String name) {
        super(name);

        init();
    }

    @Override
    protected void init() {
        // Start up mag
        addBlock(new TorqueBlock(new SetMagazine(BeltDirections.INTAKE, GateSpeeds.CLOSED)));

        // Shoot
        addBlock(new TorqueBlock(new ShootAtTarget()));

        // Taxi
        addBlock(new TorqueBlock(new Pathplanner("Mode2Right")));

        // Stop mag
        addBlock(new TorqueBlock(new SetMagazine(BeltDirections.OFF, GateSpeeds.CLOSED)));
    }
}
