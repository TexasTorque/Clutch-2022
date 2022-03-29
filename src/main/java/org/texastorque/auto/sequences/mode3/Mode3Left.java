package org.texastorque.auto.sequences.mode3;

import org.texastorque.auto.commands.*;
import org.texastorque.constants.Constants;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.*;

public class Mode3Left extends TorqueSequence {
    public Mode3Left(String name) {
        super(name);

        init();
    }

    @Override
    protected void init() {
        // Start Intake, Automag
        addBlock(new TorqueBlock(
                new PrepareTurret(Constants.TURRET_BACK_ROT),
                new PrepareShooter(50, 1800),
                new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
                new SetMagazine(BeltDirections.INTAKE, GateSpeeds.CLOSED), new PrepareTurret(0)));

        // Run path
        addBlock(new TorqueBlock(new Pathplanner("Mode3Left")));

        // Shoot!
        addBlock(new TorqueBlock(new ShootConst(1800, 50, Constants.TURRET_BACK_ROT, true, 1.6)));

        // Shut off
        addBlock(new TorqueBlock(
                new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED),
                new SetMagazine(BeltDirections.OFF, GateSpeeds.CLOSED)));
    }
}
