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
        TorqueBlock c = new TorqueBlock();
        c.add(new ShootAtTarget());
        addBlock(c);
    }
}
