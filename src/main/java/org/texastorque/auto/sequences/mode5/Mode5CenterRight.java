package org.texastorque.auto.sequences.mode5;

import org.texastorque.auto.commands.*;
import org.texastorque.constants.Constants;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.*;

public class Mode5CenterRight extends TorqueSequence {
    public Mode5CenterRight(String name) {
        super(name);

        init();
    }

    @Override
    protected void init() {
        // Start Intake, Automag
        addBlock(new TorqueBlock(
                new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
                new SetMagazine(BeltDirections.INTAKE, GateSpeeds.CLOSED),
                new PrepareTurret(Constants.TURRET_BACK_ROT),
                new PrepareShooter(50, 1800),
                new Pathplanner("Mode5CenterRight_1")));

        // Shoot preload and pickup
        addBlock(new TorqueBlock(new SetIntake(IntakePosition.PRIME, IntakeDirection.INTAKE),
                new ShootConst(1800, 50, Constants.TURRET_BACK_ROT, false, 1)));

        // Go to human player
        addBlock(new TorqueBlock(new SetIntake(IntakePosition.PRIME, IntakeDirection.INTAKE),
                new Pathplanner("Mode5CenterRight_2", false)));

        // Shoot for the gold
        addBlock(new TorqueBlock(new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED),
                new ShootConst(1800, 50, Constants.TURRET_BACK_ROT, false, 3)));

        // Shut off
        addBlock(new TorqueBlock(
                new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED),
                new SetMagazine(BeltDirections.OFF, GateSpeeds.CLOSED)));
    }
}
