package org.texastorque.auto.sequences.assists;

import org.texastorque.torquelib.auto.*;

import org.texastorque.auto.commands.*;

public class AutoLaunch extends TorqueSequence {
    public AutoLaunch() {
        super("AutoLaunch");
        init();
    }

    @Override
    protected void init() {
        addBlock(new TorqueBlock(new ShootAtTarget()));
    }
}
