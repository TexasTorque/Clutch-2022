package org.texastorque.auto.sequences;

import org.texastorque.auto.commands.Pathplanner;
import org.texastorque.torquelib.auto.TorqueBlock;
import org.texastorque.torquelib.auto.TorqueSequence;

public class PathTuning extends TorqueSequence {

    public PathTuning() {
        super("PathTuning");
        init();
    }

    @Override
    protected void init() {
        addBlock(new TorqueBlock(new Pathplanner("BLURight1a")));
        addBlock(new TorqueBlock(new Pathplanner("BLURight1b", false)));
        addBlock(new TorqueBlock(new Pathplanner("BLURight1c", false)));
    }

}
