package org.texastorque.auto.sequences;

import org.texastorque.Subsystems;
import org.texastorque.auto.commands.*;
import org.texastorque.subsystems.Shooter;
import org.texastorque.subsystems.Intake.IntakeState;
import org.texastorque.subsystems.Magazine.BeltDirection;
import org.texastorque.torquelib.auto.*;
import org.texastorque.torquelib.auto.commands.*;

public class OneEvil extends TorqueSequence implements Subsystems {
    public OneEvil() { super("OneEvil"); init(); }

    @Override
    protected void init() {
        addBlock(new TorqueBlock(new Shoot(1300, 10, 0, true, 1)));
        addBlock(new TorqueBlock(new Path("One1", true, 1, .5)));
        addBlock(new TorqueBlock(
                new Path("One2", false, 1, .5),
                new Execute(() -> { 
                    intake.setState(IntakeState.INTAKE);
                })
        ));
        addBlock(new TorqueBlock(new Shoot(1200, Shooter.HOOD_MAX, -135, true, 1)));
        addBlock(new TorqueBlock(new Execute(() -> { 
            magazine.setBeltDirection(BeltDirection.OFF); 
            intake.setState(IntakeState.PRIMED);
        })));
    }
}
