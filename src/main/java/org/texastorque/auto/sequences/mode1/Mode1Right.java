package org.texastorque.auto.sequences.mode1;

import org.texastorque.auto.commands.*;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.*;

public class Mode1Right extends TorqueSequence {
    public Mode1Right(String name) {
        super(name);

        init();
    }

    @Override
    protected void init() {
        addBlock(new TorqueBlock(new SetMagazine(BeltDirections.OFF, GateSpeeds.OFF),
                new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED), new Wait(8)));
        addBlock(new TorqueBlock(new Pathplanner("Mode1Right", true, 1, 1)));
    }
}
