package org.texastorque.auto.sequences.mode5;

import org.texastorque.auto.commands.*;
import org.texastorque.constants.Constants;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.*;

public class Mode5Right extends TorqueSequence {
    public Mode5Right(String name) {
        super(name);

        init();
    }

    @Override
    protected void init() {
        addBlock(new TorqueBlock(
            new PrepareTurret(173.15), new PrepareShooter(30, 1870),
            new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
            new SetMagazine(BeltDirections.INTAKE, GateSpeeds.CLOSED),
            new Pathplanner("Mode6Right_1")));

        addBlock(new TorqueBlock(
            new ShootConst(1870, 30, 173.15, true, 1),
            new SetIntake(IntakePosition.DOWN, IntakeDirection.STOPPED)));

        addBlock(new TorqueBlock(
            new PrepareTurret(42), new PrepareShooter(34.5, 1800),
            new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
            new Pathplanner("Mode6Right_2", false)));

        // Shoot!
        addBlock(new TorqueBlock(new ShootConst(1890, 30, 42, true, 3),
                new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED)));

        // Shut off
        addBlock(new TorqueBlock(
            new SetMagazine(BeltDirections.OFF, GateSpeeds.CLOSED)));
    }
}
