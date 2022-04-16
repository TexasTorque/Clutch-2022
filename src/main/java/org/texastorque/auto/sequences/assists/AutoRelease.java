package org.texastorque.auto.sequences.assists;

import org.texastorque.auto.commands.PullAndRelease;
import org.texastorque.torquelib.auto.TorqueBlock;
import org.texastorque.torquelib.auto.TorqueSequence;

public class AutoRelease extends TorqueSequence {

    @Override
    protected void init() {
        addBlock(new TorqueBlock(new PullAndRelease(.5, .25)));
    }

}
