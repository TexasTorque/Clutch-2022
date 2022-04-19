package org.texastorque.auto.sequences.mode5;

import org.texastorque.auto.commands.*;
import org.texastorque.constants.Constants;
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
                                new PrepareTurret(-171.15),
                                new PrepareShooter(26, 1840),
                                new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
                                new SetMagazine(BeltDirections.INTAKE, GateSpeeds.CLOSED),
                                new Pathplanner("Mode6Left_1", true, 1.5, 1)));

                addBlock(new TorqueBlock(new ShootConst(1840, 26, -171.15, true, 1),
                                new SetIntake(IntakePosition.DOWN, IntakeDirection.STOPPED)));

                addBlock(new TorqueBlock(
                                new PrepareTurret(38),
                                new PrepareShooter(28, 1800),
                                new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
                                new Pathplanner("Mode6Left_2", false)));

                addBlock(new TorqueBlock(new Wait(.1)));

                addBlock(new TorqueBlock(new Pathplanner("Mode6Left_3", false)));

                // Shoot!
                addBlock(new TorqueBlock(new ShootAtTarget(4, true, true, -100, -5)));

                // Shut off
                addBlock(new TorqueBlock(
                                new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED),
                                new SetMagazine(BeltDirections.OFF, GateSpeeds.CLOSED)));
        }
}
