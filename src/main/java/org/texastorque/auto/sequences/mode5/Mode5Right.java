package org.texastorque.auto.sequences.mode5;

import com.pathplanner.lib.PathPlanner;

import org.texastorque.auto.commands.*;
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
        // Start Intake, Automag
        addBlock(new TorqueBlock(
                new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
                new SetMagazine(BeltDirections.INTAKE, GateSpeeds.CLOSED), new PrepareTurret(-75),
                new PrepareShooter(50, 1960),
                new Pathplanner("Mode5Right_1")));

        addBlock(new TorqueBlock(
                new ShootAtTarget(1.4, false)));

        addBlock(new TorqueBlock(new Pathplanner("Mode5Right_2", false)));

        addBlock(new TorqueBlock(new Wait(1)));

        addBlock(new TorqueBlock(new PrepareTurret(5), new PrepareShooter(50, 1960),
                new Pathplanner("Mode5Right_3", false)));

        addBlock(new TorqueBlock(new ShootAtTarget(1.6, true)));

        // Shut off
        addBlock(new TorqueBlock(
                new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED),
                new SetMagazine(BeltDirections.OFF, GateSpeeds.CLOSED)));
    }
}
