package org.texastorque.subsystems;

import org.texastorque.torquelib.base.TorqueSubsystem;

public class Empty extends TorqueSubsystem {
    private static volatile Empty instance;

    private Empty() {
        
    }

    @Override
    public void initTeleop() {
        
    }

    @Override
    public void updateTeleop() {
        
    }

    @Override
    public void initAuto() {
        
    }

    @Override
    public void updateAuto() {
        
    }

    public static synchronized Empty getInstance() {
        return instance == null ? instance = new Empty() : instance;
    }
}
