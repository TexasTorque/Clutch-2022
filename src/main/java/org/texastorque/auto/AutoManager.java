/**
 * Copyright 2022 Texas Torque.
 *
 * This file is part of Clutch-2022, which is not licensed for distribution.
 * For more details, see ./license.txt or write <jus@gtsbr.org>.
 */
package org.texastorque.auto;

import org.texastorque.auto.sequences.FiveCool;
import org.texastorque.auto.sequences.FiveLame;
import org.texastorque.auto.sequences.Omar;
import org.texastorque.auto.sequences.OneEvil;
import org.texastorque.auto.sequences.TwoEvil;
import org.texastorque.torquelib.auto.TorqueAutoManager;

public final class AutoManager extends TorqueAutoManager {
    private static volatile AutoManager instance;

    @Override
    protected final void init() {
        addSequence("Omar", new Omar());
        addSequence("One (evil)", new OneEvil());
        addSequence("Two (evil)", new TwoEvil());
        addSequence("Five (cool)", new FiveCool());
        addSequence("Five (lame)", new FiveLame());
    }

    /**
     * Get the AutoManager instance
     *
     * @return AutoManager
     */
    public static final synchronized AutoManager getInstance() {
        return instance == null ? instance = new AutoManager() : instance;
    }
}