package org.texastorque.auto.sequences;

import org.texastorque.auto.commands.Pathplanner;
import org.texastorque.torquelib.auto.TorqueBlock;
import org.texastorque.torquelib.auto.TorqueSequence;

/**
 * @deprecated Used for path testing, no longer needed
 */
public class PathplannerSequence extends TorqueSequence {
    private String pathName;

    public PathplannerSequence(String name) {
        super("PathplannerSequence");
        this.pathName = name;
        init();
    }

    @Override
    protected void init() {
        addBlock(new TorqueBlock(new Pathplanner(pathName)));
    }
}