package org.texastorque.auto.sequences.assists;

import org.texastorque.torquelib.auto.*;

import org.texastorque.auto.commands.*;

public class RotateToBall extends TorqueSequence {

    public RotateToBall() {
        super("RotateToBall");
        init();
    }

    @Override
    protected void init() {
        addBlock(new TorqueBlock(new BallRotator()));

    }

}
