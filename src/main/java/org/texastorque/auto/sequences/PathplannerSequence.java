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
        TorqueBlock plannerSequence = new TorqueBlock();
        plannerSequence.add(new Pathplanner("FirstPlannerSequence"));
        addBlock(plannerSequence);
    }

}