package org.texastorque.auto.sequences.mode6;

import org.texastorque.auto.commands.*;
import org.texastorque.constants.Constants;
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
                addBlock(new TorqueBlock(new PrepareShooter(26, 1750),
                                new PrepareTurret(173.15),
                                new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
                                new SetMagazine(BeltDirections.INTAKE, GateSpeeds.CLOSED),
                                new Pathplanner("Mode6Right_1")));

                addBlock(new TorqueBlock(new ShootConst(1750, 26, 173.15, true, 1),
                                new SetIntake(IntakePosition.DOWN, IntakeDirection.STOPPED)));

                addBlock(new TorqueBlock(
                                new PrepareTurret(35),
                                new PrepareShooter(26, 1800),
                                new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
                                new Pathplanner("Mode6Right_2", false)));

                addBlock(new TorqueBlock(new Wait(1)));

                addBlock(new TorqueBlock(new Pathplanner("Mode6Right_3", false)));

                // Shoot!
                addBlock(new TorqueBlock(new ShootConst(1800, 26, 35, true, 3), new CreepForward(2.5)));
                // addBlock(new TorqueBlock(new ShootAtTarget(4, true), new CreepForward(2.5)));

                // Shut off
                addBlock(new TorqueBlock(
                                new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED),
                                new SetMagazine(BeltDirections.OFF, GateSpeeds.CLOSED)));
        }
}
