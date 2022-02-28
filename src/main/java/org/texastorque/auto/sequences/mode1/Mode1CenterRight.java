package org.texastorque.auto.sequences.mode1;

import org.texastorque.torquelib.auto.*;

import org.texastorque.auto.commands.*;

public class Mode1CenterRight extends TorqueSequence {
    public Mode1CenterRight(String name) {
        super(name);

        init();
    }

    @Override
    protected void init() {
        addBlock(new TorqueBlock(new Pathplanner("Mode1CenterRight")));
    }
}