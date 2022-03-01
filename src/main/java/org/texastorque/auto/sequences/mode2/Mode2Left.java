package org.texastorque.auto.sequences.mode2;

import org.texastorque.auto.commands.*;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.*;

public class Mode2Left extends TorqueSequence {
    public Mode2Left(String name) {
        super(name);

        init();
    }

    @Override
    protected void init() {
        // Start up mag
        addBlock(new TorqueBlock(
            new SetMagazine(BeltDirections.INTAKE, GateSpeeds.CLOSED)));

        // Shoot
        addBlock(new TorqueBlock(new ShootAtTarget()));

        // Taxi
        addBlock(new TorqueBlock(new Pathplanner("Mode2Left")));

        // Stop mag
        addBlock(new TorqueBlock(
            new SetMagazine(BeltDirections.OFF, GateSpeeds.CLOSED)));
    }
}
