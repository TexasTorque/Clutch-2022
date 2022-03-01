package org.texastorque.auto.sequences.assists;

import org.texastorque.auto.commands.*;
import org.texastorque.torquelib.auto.*;

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
