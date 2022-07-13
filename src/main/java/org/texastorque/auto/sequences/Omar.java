/**
 * Copyright 2022 Texas Torque.
 * 
 * This file is part of Clutch-2022, which is not licensed for distribution.
 * For more details, see ./license.txt or write <jus@gtsbr.org>.
 */
package org.texastorque.auto.sequences;

import org.texastorque.auto.commands.Path;
import org.texastorque.torquelib.auto.TorqueBlock;
import org.texastorque.torquelib.auto.TorqueSequence;

public final class Omar extends TorqueSequence {
    public Omar() {
        super("Omar");
        init();
    }

    @Override
    protected final void init() {
        addBlock(new TorqueBlock(new Path("Omar", true, 1.5, .75)));
    }
}
