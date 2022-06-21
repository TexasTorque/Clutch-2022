package org.texastorque.auto.sequences;

import org.texastorque.auto.commands.*;
import org.texastorque.torquelib.auto.TorqueBlock;
import org.texastorque.torquelib.auto.TorqueSequence;

public class Five extends TorqueSequence {
    public Five() { super("Five"); init(); }

    @Override
    protected void init() {
        // addBlock(new TorqueBlock(new FollowPathPlanner("Five1", true, 2, 1)));
        addBlock(new TorqueBlock(new Wait(2.)));
        addBlock(new TorqueBlock(new Shoot(1700, 26, 172.15, true, 2)));
        // addBlock(new TorqueBlock(new FollowPathPlanner("Five2", false, 2, 1)));
        // addBlock(new TorqueBlock(new Wait(1.)));
        // addBlock(new TorqueBlock(new FollowPathPlanner("Five3", false, 2, 1)));
    }
}
