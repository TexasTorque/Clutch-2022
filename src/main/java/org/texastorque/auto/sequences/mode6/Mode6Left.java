package org.texastorque.auto.sequences.mode6;

import org.texastorque.auto.commands.*;
import org.texastorque.constants.Constants;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.*;

public class Mode6Left extends TorqueSequence {
        public Mode6Left(String name) {
                super(name);

                init();
        }

        @Override
        protected void init() {
                addBlock(new TorqueBlock(
                                new PrepareTurret(-171.15),
                                new PrepareShooter(30, 1840),
                                new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
                                new SetMagazine(BeltDirections.INTAKE, GateSpeeds.CLOSED),
                                new Wait(0.8),
                                new Pathplanner("Mode6Left_1")));

                addBlock(new TorqueBlock(new ShootConst(1840, 30, -171.15, true, 1),
                                new SetIntake(IntakePosition.DOWN, IntakeDirection.STOPPED)));

                addBlock(new TorqueBlock(
                                new PrepareTurret(38),
                                new PrepareShooter(28, 1800),
                                new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
                                new Pathplanner("Mode6Left_2", false)));

                // Shoot!
                addBlock(new TorqueBlock(new ShootConst(1890, 28, 38, true, 3), new CreepForward(2.5)));

                // Shut off
                addBlock(new TorqueBlock(
                                new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED),
                                new SetMagazine(BeltDirections.OFF, GateSpeeds.CLOSED)));
        }
}
