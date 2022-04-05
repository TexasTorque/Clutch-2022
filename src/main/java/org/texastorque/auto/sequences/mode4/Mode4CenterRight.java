package org.texastorque.auto.sequences.mode4;

import org.texastorque.auto.commands.*;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.*;

public class Mode4CenterRight extends TorqueSequence {
    private final double firstShotTurret = 171;
    private final double firstShotRPM = 1800;
    private final double firstShotHood = 26;

    private final double secondShotTurret = 78;
    private final double secondShotRPM = 1840;
    private final double secondShotHood = 28;

    public Mode4CenterRight(String name) {
        super(name);

        init();
    }

    @Override
    protected void init() {
        addBlock(new TorqueBlock(
                new PrepareTurret(firstShotTurret),
                new PrepareShooter(firstShotHood, firstShotRPM),
                new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
                new SetMagazine(BeltDirections.INTAKE, GateSpeeds.CLOSED),
                new Pathplanner("Mode4CenterRight_1")));

        addBlock(new TorqueBlock(new ShootConst(firstShotRPM, firstShotHood, firstShotTurret, false, 1),
                new SetIntake(IntakePosition.DOWN, IntakeDirection.STOPPED)));

        addBlock(new TorqueBlock(
                new PrepareTurret(secondShotTurret),
                new PrepareShooter(secondShotHood, secondShotRPM),
                new SetIntake(IntakePosition.DOWN, IntakeDirection.INTAKE),
                new Pathplanner("Mode4CenterRight_2", false)));

        // Shoot!
        addBlock(new TorqueBlock(new ShootConst(secondShotRPM, secondShotHood, secondShotTurret, true)));

        // Shut off
        addBlock(new TorqueBlock(
                new SetIntake(IntakePosition.PRIME, IntakeDirection.STOPPED),
                new SetMagazine(BeltDirections.OFF, GateSpeeds.CLOSED)));
    }
}
