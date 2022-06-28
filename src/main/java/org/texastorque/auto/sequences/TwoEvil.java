package org.texastorque.auto.sequences;

import org.texastorque.Subsystems;
import org.texastorque.auto.commands.*;
import org.texastorque.subsystems.Shooter;
import org.texastorque.subsystems.Intake.IntakeState;
import org.texastorque.subsystems.Magazine.BeltDirection;
import org.texastorque.subsystems.Magazine.GateDirection;
import org.texastorque.subsystems.Shooter.ShooterState;
import org.texastorque.subsystems.Turret.TurretState;
import org.texastorque.torquelib.auto.*;
import org.texastorque.torquelib.auto.commands.*;

public class TwoEvil extends TorqueSequence implements Subsystems {
    public TwoEvil() { super("TwoEvil"); init(); }

    @Override
    protected void init() {
        addBlock(new TorqueBlock(
                new Path("Two1", true, 2, 1),
                new Execute(() -> { 
                    // magazine.setBeltDirection(BeltDirection.INTAKING);
                    intake.setState(IntakeState.INTAKE);

                    turret.setState(TurretState.POSITIONAL);
                    turret.setPosition(-171.15);

                    shooter.setState(ShooterState.SETPOINT);
                    // shooter.setFlywheelSpeed(1000);
                    shooter.setHoodPosition(26);
                    // magazine.setGateDirection(GateDirection.REVERSE);
                })
        ));
        addBlock(new TorqueBlock(new Shoot(1450, 30, -171.15, true, 1.6)));
        addBlock(new TorqueBlock(new Path("Two2", false, 1, .5)));
        addBlock(new TorqueBlock(new Shoot(800, 30, 145, true, 1)));
        addBlock(new TorqueBlock(new Execute(() -> { 
            magazine.setBeltDirection(BeltDirection.OFF); 
            intake.setState(IntakeState.PRIMED);
            turret.setState(TurretState.CENTER);
        })));
    }
}
