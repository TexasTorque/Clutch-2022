package org.texastorque.auto.sequences;

import org.texastorque.auto.commands.ShootAtTarget;
import org.texastorque.torquelib.auto.TorqueBlock;
import org.texastorque.torquelib.auto.TorqueSequence;

public class AutoLaunch extends TorqueSequence {
    public AutoLaunch() {
        super("AutoLaunch");
    }

    @Override
    protected void init() {
        addBlock(new TorqueBlock(new ShootAtTarget()));
    }
}
