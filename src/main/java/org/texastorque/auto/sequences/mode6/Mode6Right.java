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
                addBlock(new TorqueBlock(
                                new PrepareTurret(176.15),
                                new PrepareShooter(50, 1850),
                                new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
                                new SetMagazine(BeltDirections.INTAKE, GateSpeeds.CLOSED),
                                new Pathplanner("Mode6Right_1")));

                addBlock(new TorqueBlock((new ShootConst(1850, 50, 176.15, true, 1))));

                addBlock(new TorqueBlock(
                                new PrepareTurret(30.6),
                                new PrepareShooter(50, 1960),
                                new Pathplanner("Mode6Right_2", false)));

                // Shoot!
                addBlock(new TorqueBlock(new ShootAtTarget(3), new CreepForward(3)));

                // Shut off
                addBlock(new TorqueBlock(
                                new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED),
                                new SetMagazine(BeltDirections.OFF, GateSpeeds.CLOSED)));
        }
}
