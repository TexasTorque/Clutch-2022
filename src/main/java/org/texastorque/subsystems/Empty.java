package org.texastorque.subsystems;

import org.texastorque.Subsystems;
import org.texastorque.torquelib.base.TorqueMode;
import org.texastorque.torquelib.base.TorqueSubsystem;

public final class Empty extends TorqueSubsystem implements Subsystems {
    private static volatile Empty instance;

    private Empty() {}

    @Override
    public final void initialize(final TorqueMode mode) {
    }

    @Override
    public final void update(final TorqueMode mode) {
    }
        
    public static final synchronized Empty getInstance() {
        return instance == null ? instance = new Empty() : instance;
    }
}
