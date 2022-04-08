package org.texastorque.auto.sequences.mode5;

import org.texastorque.auto.commands.*;
import org.texastorque.constants.Constants;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.*;

public class Mode5CenterRight extends TorqueSequence {
        public Mode5CenterRight(String name) {
                super(name);

                init();
        }

        @Override
        protected void init() {
                // Start Intake, Automag
                addBlock(new TorqueBlock(
                                new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
                                new SetMagazine(BeltDirections.INTAKE, GateSpeeds.CLOSED),
                                new PrepareTurret(170),
                                new PrepareShooter(26, 1800),
                                new Pathplanner("Mode5CenterRight_1")));

                // Shoot preload and pickup
                addBlock(new TorqueBlock(new SetIntake(IntakePosition.DOWN, IntakeDirection.STOPPED),
                                new ShootConst(1800, 26, 170, false, 1)));

                // Go to human player
                addBlock(new TorqueBlock(new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
                                new Pathplanner("Mode5CenterRight_2", false), new PrepareTurret(175)));

                // Shoot for the gold
                addBlock(new TorqueBlock(new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED),
                                new ShootConst(1800, 26, 175, false, 3)));

                // Shut off
                addBlock(new TorqueBlock(
                                new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED),
                                new SetMagazine(BeltDirections.OFF, GateSpeeds.CLOSED)));
        }
}
