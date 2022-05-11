package org.texastorque.subsystems;

import org.texastorque.torquelib.base.TorqueSubsystem;

public class Empty extends TorqueSubsystem {
    private static volatile Empty instance;

    private Empty() {
        
    }

    @Override
    public final void initTeleop() {
        
    }

    @Override
    public final void updateTeleop() {
        
    }

    @Override
    public final void initAuto() {
        
    }

    @Override
    public final void updateAuto() {
        
    }

    public static final synchronized Empty getInstance() {
        return instance == null ? instance = new Empty() : instance;
    }
}
