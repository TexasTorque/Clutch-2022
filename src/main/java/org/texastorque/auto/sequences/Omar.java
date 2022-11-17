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
        final int paths = 30;

        for (int i = 0; i < paths; i++)
            addBlock(new TorqueBlock(new Path("Omar", i == 0, 2, 1))); 
    }
}
