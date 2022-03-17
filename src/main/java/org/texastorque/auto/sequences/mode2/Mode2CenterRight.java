package org.texastorque.auto.sequences.mode2;

import org.texastorque.auto.commands.*;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.*;

public class Mode2CenterRight extends TorqueSequence {
    public Mode2CenterRight(String name) {
        super(name);

        init();
    }

    @Override
    protected void init() {
        // Start up mag
        addBlock(new TorqueBlock(
                new SetMagazine(BeltDirections.INTAKE, GateSpeeds.CLOSED),
                new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED)));

        // Shoot
        addBlock(new TorqueBlock(new ShootAtTarget(2, true)));

        // Taxi
        addBlock(new TorqueBlock(new Pathplanner("Mode2CenterRight")));

        // Stop mag
        addBlock(new TorqueBlock(
                new SetMagazine(BeltDirections.OFF, GateSpeeds.CLOSED)));
    }
}
