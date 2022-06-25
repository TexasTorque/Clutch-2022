package org.texastorque.auto.sequences;

import org.texastorque.Subsystems;
import org.texastorque.auto.commands.*;
import org.texastorque.subsystems.Intake.IntakeState;
import org.texastorque.subsystems.Magazine.BeltDirection;
import org.texastorque.subsystems.Shooter.ShooterState;
import org.texastorque.subsystems.Turret.TurretState;
import org.texastorque.torquelib.auto.*;
import org.texastorque.torquelib.auto.commands.*;

import edu.wpi.first.math.kinematics.ChassisSpeeds;

public class Five extends TorqueSequence implements Subsystems {
    public Five() { super("Five"); init(); }

    @Override
    protected void init() {
        addBlock(new TorqueBlock(
                new Path("Five1", true, 2, 1),
                new Execute(() -> { 
                    magazine.setBeltDirection(BeltDirection.INTAKING);
                    intake.setState(IntakeState.INTAKE);

                    turret.setState(TurretState.POSITIONAL);
                    turret.setPosition(172.15);

                    shooter.setState(ShooterState.SETPOINT);
                    shooter.setFlywheelSpeed(1400);
                    shooter.setHoodPosition(26);
                })
        ));
        addBlock(new TorqueBlock(new Shoot(1700, 26, 172.15, false, 2)));
        addBlock(new TorqueBlock(new Path("Five2", false, 3, 1)));
        addBlock(new TorqueBlock(
                new Wait(1.),
                new Execute(() -> {
                    turret.setState(TurretState.POSITIONAL);
                    turret.setPosition(30);

                    shooter.setState(ShooterState.SETPOINT);
                    shooter.setFlywheelSpeed(1400);
                })));
        addBlock(new TorqueBlock(new Path("Five3", false, 2, 1)));
        addBlock(new TorqueBlock(
            new Creep(2, new ChassisSpeeds(.5, 0, 0)),
            new Target(false, 3)
        ));

        addBlock(new TorqueBlock(new Execute(() -> { 
            magazine.setBeltDirection(BeltDirection.OFF); 
            intake.setState(IntakeState.PRIMED);
        })));
    }
}
