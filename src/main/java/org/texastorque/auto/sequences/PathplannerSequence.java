package org.texastorque.auto.sequences;

import org.texastorque.torquelib.auto.TorqueBlock;
import org.texastorque.torquelib.auto.TorqueSequence;
import org.texastorque.auto.commands.Pathplanner;

public class PathplannerSequence extends TorqueSequence {

    public PathplannerSequence() {
        super("PathplannerSequence");
    }

    @Override
    protected void init() {
        addBlock(new TorqueBlock(new Pathplanner("FirstPlannerSequence")));
    }

}