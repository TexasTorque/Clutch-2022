package org.texastorque.auto.sequences;

import org.texastorque.auto.commands.ShootAway;
import org.texastorque.torquelib.auto.TorqueBlock;
import org.texastorque.torquelib.auto.TorqueSequence;

public class AutoReflect extends TorqueSequence {
    public AutoReflect() {
        super("AutoReflect");
        init();
    }

    @Override
    protected void init() {
        addBlock(new TorqueBlock(new ShootAway()));
    }
}
