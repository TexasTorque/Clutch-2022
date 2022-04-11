package org.texastorque.auto.sequences.mode3;

import org.texastorque.auto.commands.*;
import org.texastorque.constants.Constants;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.*;

public class Mode3CenterRight extends TorqueSequence {
    public Mode3CenterRight(String name) {
        super(name);

        init();
    }

    @Override
    protected void init() {
        // Start Intake, Automag
        addBlock(new TorqueBlock(
                new PrepareTurret(173),
                new PrepareShooter(26, 1830),
                new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
                new SetMagazine(BeltDirections.INTAKE, GateSpeeds.CLOSED)));

        // Run path
        addBlock(new TorqueBlock(new Pathplanner("Mode3CenterRight")));

        // Shoot!
        addBlock(new TorqueBlock(new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED),
                new ShootConst(1830, 26, 173, true, 1.6)));

        // Shut off
        addBlock(new TorqueBlock(new SetMagazine(BeltDirections.OFF, GateSpeeds.CLOSED)));
    }
}
