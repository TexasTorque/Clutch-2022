package org.texastorque.auto.sequences.mode1;

import org.texastorque.auto.commands.Pathplanner;
import org.texastorque.auto.commands.SetIntake;
import org.texastorque.auto.commands.SetMagazine;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.*;

public class Mode1Left extends TorqueSequence {
    public Mode1Left(String name) {
        super(name);

        init();
    }

    @Override
    protected void init() {
        addBlock(new TorqueBlock(new SetMagazine(BeltDirections.OFF, GateSpeeds.OFF),
                new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED)));
        addBlock(new TorqueBlock(new Pathplanner("Mode1Left")));
    }
}
