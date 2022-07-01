package org.texastorque.auto.sequences;

import org.texastorque.Subsystems;
import org.texastorque.auto.commands.Path;
import org.texastorque.auto.commands.Shoot;
import org.texastorque.subsystems.Intake.IntakeState;
import org.texastorque.subsystems.Magazine.BeltDirection;
import org.texastorque.torquelib.auto.TorqueBlock;
import org.texastorque.torquelib.auto.TorqueSequence;
import org.texastorque.torquelib.auto.commands.Execute;
import org.texastorque.torquelib.util.TorqueUtil;

public class One extends TorqueSequence implements Subsystems {
    public One() {
        super("One");
        init();
    }

    @Override
    protected void init() {
        // TorqueUtil.outOfDate();

        addBlock(new TorqueBlock(new Shoot(1300, 10, 0, true, 1)));
        addBlock(new TorqueBlock(new Path("One1", true, 1, .5)));
        addBlock(new TorqueBlock(new Execute(() -> {
            magazine.setBeltDirection(BeltDirection.OFF);
            intake.setState(IntakeState.PRIMED);
        })));
    }
}
