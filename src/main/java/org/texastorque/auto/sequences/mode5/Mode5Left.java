package org.texastorque.auto.sequences.mode5;

import org.texastorque.auto.commands.*;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.*;

public class Mode5Left extends TorqueSequence {
    public Mode5Left(String name) {
        super(name);

        init();
    }

    @Override
    protected void init() {
        addBlock(new TorqueBlock(
                new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
                new SetMagazine(BeltDirections.INTAKE, GateSpeeds.CLOSED), new PrepareTurret(0),
                new PrepareShooter(50, 1960),
                new Pathplanner("Mode5Left_1")));

        addBlock(new TorqueBlock(new ShootConst(1960, 50, 0, false, 1)));

        addBlock(new TorqueBlock(new Pathplanner("Mode5Left_2", false)));

        addBlock(new TorqueBlock(new Wait(1)));

        addBlock(new TorqueBlock(new PrepareTurret(0), new PrepareShooter(50, 1960),
                new Pathplanner("Mode5Left_3", false)));

        addBlock(new TorqueBlock(new ShootConst(1960, 50, 0, true, 1.6)));

        // Shut off
        addBlock(new TorqueBlock(
                new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED),
                new SetMagazine(BeltDirections.OFF, GateSpeeds.CLOSED)));

    }
}
