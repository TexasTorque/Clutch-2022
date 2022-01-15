package org.texastorque.subsystems;

import org.texastorque.torquelib.base.TorqueSubsystem;

public class Drivebase extends TorqueSubsystem {
    private static volatile Drivebase instance;

    private Drivebase() {
    }

    @Override
    public void initTeleop() {
    }

    @Override 
    public void initAuto() {
    }

    @Override
    public void updateTeleop() {
    }

    @Override
    public void updateAuto() {
    }

    @Override
    public void output() {
    }

    @Override
    public void updateFeedbackTeleop() {
    }

    @Override
    public void updateFeedbackAuto() {
    }

    @Override
    public void updateSmartDashboard() {
    }

    public static synchronized Drivebase getInstance() {
        return (instance == null) ? instance = new Drivebase() : instance;
    }
}
