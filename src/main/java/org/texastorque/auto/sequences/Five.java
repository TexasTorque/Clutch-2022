package org.texastorque.auto.sequences;

import org.texastorque.Subsystems;
import org.texastorque.auto.commands.*;
import org.texastorque.subsystems.Magazine.BeltDirection;
import org.texastorque.subsystems.Magazine.GateDirection;
import org.texastorque.torquelib.auto.*;
import org.texastorque.torquelib.auto.commands.*;

public class Five extends TorqueSequence implements Subsystems {
    public Five() { super("Five"); init(); }

    @Override
    protected void init() {
        addBlock(new TorqueBlock(
                new FollowPathPlanner("Five1", true, 2, 1),
                new Execute(() -> { magazine.setBeltDirection(BeltDirection.INTAKING); })
        ));
        addBlock(new TorqueBlock(new Shoot(1700, 26, 172.15, true, 2)));
        // addBlock(new TorqueBlock(new Execute(() -> { magazine.setGateDirection(GateDirection.OFF); })));
        // addBlock(new TorqueBlock(new FollowPathPlanner("Five2", false, 2, 1)));
        // addBlock(new TorqueBlock(new Wait(1.)));
        // addBlock(new TorqueBlock(new FollowPathPlanner("Five3", false, 2, 1)));
        addBlock(new TorqueBlock(new Execute(() -> { magazine.setBeltDirection(BeltDirection.OFF); })));
    }
}
