package org.texastorque.auto.sequences.mode6;

import org.texastorque.auto.commands.*;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.*;

public class Mode6Right extends TorqueSequence {
        public Mode6Right(String name) {
                super(name);

                init();
        }

        @Override
        protected void init() {
                addBlock(new TorqueBlock(
                                new PrepareTurret(-75),
                                new PrepareShooter(50, 1800),
                                new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
                                new SetMagazine(BeltDirections.INTAKE, GateSpeeds.CLOSED),
                                new Pathplanner("Mode6Right_1")));

                addBlock(new TorqueBlock((new ShootConst(1810, 50, -75, false, 1))));

                addBlock(new TorqueBlock(
                                new PrepareTurret(-75),
                                new PrepareShooter(50, 1960),
                                new Pathplanner("Mode6Right_1_5", false)));

                addBlock(new TorqueBlock(new ShootConst(1960, 50, -75, false, .8)));

                // Go to human player
                addBlock(new TorqueBlock(new Pathplanner("Mode6Right_2", false)));

                // Waiting for human player
                addBlock(new TorqueBlock(new Wait(1)));

                // Go to the last shoot
                addBlock(new TorqueBlock(new PrepareTurret(-5), new PrepareShooter(50, 1975),
                                new Pathplanner("Mode6Right_3", false)));

                // Shoot!
                addBlock(new TorqueBlock(new ShootConst(1975, 50, -5, true, 1.6)));

                // Shut off
                addBlock(new TorqueBlock(
                                new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED),
                                new SetMagazine(BeltDirections.OFF, GateSpeeds.CLOSED)));
        }
}
