package org.texastorque.auto.sequences;

import org.texastorque.auto.commands.BallRotator;
import org.texastorque.torquelib.auto.TorqueBlock;
import org.texastorque.torquelib.auto.TorqueSequence;

public class RotateToBall extends TorqueSequence {

    public RotateToBall() {
        super("RotateToBall (ASSIST)");
        init();
    }

    @Override
    protected void init() {
        addBlock(new TorqueBlock(new BallRotator()));

    }

}
