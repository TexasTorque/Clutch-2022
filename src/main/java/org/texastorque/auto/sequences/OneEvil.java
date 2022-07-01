package org.texastorque.auto.sequences;

import org.texastorque.Subsystems;
import org.texastorque.auto.commands.Path;
import org.texastorque.auto.commands.Shoot;
import org.texastorque.subsystems.Intake.IntakeState;
import org.texastorque.subsystems.Magazine.BeltDirection;
import org.texastorque.torquelib.auto.TorqueBlock;
import org.texastorque.torquelib.auto.TorqueSequence;
import org.texastorque.torquelib.auto.commands.Execute;

public class OneEvil extends TorqueSequence implements Subsystems {
    public OneEvil() {
        super("OneEvil");
        init();
    }

    @Override
    protected void init() {
        addBlock(new TorqueBlock(new Shoot(1350, 10, 0, true, 3)));
        addBlock(new TorqueBlock(new Path("One1", true, 1, .5),
                                 new Execute(() -> { intake.setState(IntakeState.INTAKE); })));
        addBlock(new TorqueBlock(new Path("One2", false, 1, .5)));
        addBlock(new TorqueBlock(new Shoot(1200, 30, -135, true, 1)));
        addBlock(new TorqueBlock(new Execute(() -> {
            magazine.setBeltDirection(BeltDirection.OFF);
            intake.setState(IntakeState.PRIMED);
        })));
    }
}
