package org.texastorque.auto.sequences.mode1;

import org.texastorque.auto.commands.Pathplanner;
import org.texastorque.torquelib.auto.*;

public class Mode1Left extends TorqueSequence {
    public Mode1Left(String name) {
        super(name);

        init();
    }

    @Override
    protected void init() {
        addBlock(new TorqueBlock(new Pathplanner("Mode1Left")));
    }
}
