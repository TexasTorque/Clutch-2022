package org.texastorque.auto.sequences;

import org.texastorque.Subsystems;
import org.texastorque.auto.commands.*;
import org.texastorque.subsystems.Magazine.BeltDirection;
import org.texastorque.torquelib.auto.*;
import org.texastorque.torquelib.auto.commands.*;

public class Two extends TorqueSequence implements Subsystems {
    public Two() { super("Two"); init(); }

    @Override
    protected void init() {
        addBlock(new TorqueBlock(
                new Path("Two", true, 2, 1),
                new Execute(() -> { magazine.setBeltDirection(BeltDirection.INTAKING); })
        ));
        addBlock(new TorqueBlock(new Shoot(1840, 26, -171.15, true, 1.6)));
        addBlock(new TorqueBlock(new Execute(() -> { magazine.setBeltDirection(BeltDirection.OFF); })));
    }
}
