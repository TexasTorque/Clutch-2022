package org.texastorque.auto.sequences;

import org.texastorque.auto.commands.ShootAway;
import org.texastorque.torquelib.auto.TorqueBlock;
import org.texastorque.torquelib.auto.TorqueSequence;

public class AutoReflect extends TorqueSequence {
    public AutoReflect() {
        super("AutoReflect");
    }

    @Override
    protected void init() {
        TorqueBlock c = new TorqueBlock();
        c.add(new ShootAway());
        addBlock(c);
    }
}
