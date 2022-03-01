package org.texastorque.auto.sequences.assists;

import org.texastorque.auto.commands.*;
import org.texastorque.torquelib.auto.*;

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
