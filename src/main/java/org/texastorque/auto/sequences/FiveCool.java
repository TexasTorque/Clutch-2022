package org.texastorque.auto.sequences;

import edu.wpi.first.math.kinematics.ChassisSpeeds;

import org.texastorque.Subsystems;
import org.texastorque.auto.commands.Creep;
import org.texastorque.auto.commands.Path;
import org.texastorque.auto.commands.Shoot;
import org.texastorque.auto.commands.Target;
import org.texastorque.subsystems.Intake.IntakeState;
import org.texastorque.subsystems.Magazine.BeltDirection;
import org.texastorque.subsystems.Magazine.GateDirection;
import org.texastorque.subsystems.Shooter.ShooterState;
import org.texastorque.subsystems.Turret.TurretState;
import org.texastorque.torquelib.auto.TorqueBlock;
import org.texastorque.torquelib.auto.TorqueSequence;
import org.texastorque.torquelib.auto.commands.TorqueExecute;
import org.texastorque.torquelib.auto.commands.TorqueWait;

public class FiveCool extends TorqueSequence implements Subsystems {
    public FiveCool() {
        final double firstTurret = 165;
        addBlock(new TorqueBlock(new TorqueExecute(() -> {
                                    //  magazine.setBeltDirection(BeltDirection.INTAKING);
                                     intake.setState(IntakeState.INTAKE);

                                     turret.setState(TurretState.POSITIONAL);
                                     turret.setPosition(firstTurret);

                                     shooter.setState(ShooterState.WARMUP);
                                     shooter.setFlywheelSpeed(1000);
                                     shooter.setHoodPosition(15);
                                 })));

        addBlock(new TorqueBlock(new Shoot(1450, 15, firstTurret, false, 1)));
        addBlock(new TorqueBlock(new Path("Five1", true, 4, 2)));
        final double turret2 = -115, hood2 = 20;
        addBlock(new TorqueBlock(new TorqueExecute(() -> {

                                    //  magazine.setBeltDirection(BeltDirection.INTAKING);
                                     magazine.setGateDirection(GateDirection.OFF);
                                    intake.setState(IntakeState.INTAKE);

                                    turret.setState(TurretState.POSITIONAL);
                                    turret.setPosition(turret2);

                                    shooter.setState(ShooterState.WARMUP);
                                    shooter.setFlywheelSpeed(1000);
                                    shooter.setHoodPosition(hood2);
                                })));
        addBlock(new TorqueBlock(new Shoot(1900, hood2, turret2, false, 1.8)));
        // addBlock(new TorqueBlock(new Path("Five2", false, 4, 2)));
        // addBlock(new TorqueBlock(new TorqueExecute(() -> {
        //     turret.setState(TurretState.POSITIONAL);    
        //     turret.setPosition(-65);

        //     shooter.setState(ShooterState.WARMUP);
        //     shooter.setFlywheelSpeed(800);
        //     shooter.setAutoOffset(0);
        // })));
        // addBlock(new TorqueBlock(new Path("Five3", false, 4, 2)));
        // addBlock(new TorqueBlock(new Target(false, .5)));
        // addBlock(new TorqueBlock(new Creep(2, new ChassisSpeeds(-.5, 0, 0)), 
        //         new Target(false, 4)));

        // addBlock(new TorqueBlock(new TorqueExecute(() -> {
        //     turret.setOffset(0);
        //     magazine.setBeltDirection(BeltDirection.OFF);
        //     intake.setState(IntakeState.PRIMED);
        //     turret.setState(TurretState.CENTER);
        // })));
    }
}