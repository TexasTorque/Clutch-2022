package org.texastorque.auto.sequences;

import org.texastorque.auto.commands.FollowPathPlanner;
import org.texastorque.torquelib.auto.TorqueBlock;
import org.texastorque.torquelib.auto.TorqueSequence;

public class Omar extends TorqueSequence {
    public Omar() { super("Omar"); init(); }

    @Override
    protected void init() {
        addBlock(new TorqueBlock(new FollowPathPlanner("Omar", true, 1.5, .75)));
    }
}
