package org.texastorque.auto.sequences.mode4;

import org.texastorque.auto.commands.*;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.*;

public class Mode4Left extends TorqueSequence {
    public Mode4Left(String name) {
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

        // Prepare hood for end shoot
        addBlock(new TorqueBlock(new PrepareShooter(50)));

        // Run path
        addBlock(new TorqueBlock(new Pathplanner("Mode4Left")));

        // Shoot!
        addBlock(new TorqueBlock(new ShootAtTarget(4)));

        // Shut off
        addBlock(new TorqueBlock(
                new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED),
                new SetMagazine(BeltDirections.OFF, GateSpeeds.CLOSED)));
    }
}
