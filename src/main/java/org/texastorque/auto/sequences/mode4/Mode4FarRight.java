package org.texastorque.auto.sequences.mode4;

import org.texastorque.auto.commands.*;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.*;

public class Mode4FarRight extends TorqueSequence {
        public Mode4FarRight(String name) {
                super(name);

                init();
        }

        @Override
        protected void init() {
                addBlock(new TorqueBlock(
                                new PrepareTurret(-75),
                                new PrepareShooter(26, 1800),
                                new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
                                new SetMagazine(BeltDirections.INTAKE, GateSpeeds.CLOSED),
                                new Pathplanner("Mode4FarRight_1")));

                addBlock(new TorqueBlock(new ShootConst(1800, 26, -75, false, 1)));

                addBlock(new TorqueBlock(
                                new PrepareTurret(-75),
                                new PrepareShooter(26, 1860),
                                new Pathplanner("Mode4FarRight_2", false)));

                addBlock(new TorqueBlock(new ShootConst(1860, 26, 0, true, 1.5)));

                // Shut off
                addBlock(new TorqueBlock(
                                new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED),
                                new SetMagazine(BeltDirections.OFF, GateSpeeds.CLOSED)));
        }
}
