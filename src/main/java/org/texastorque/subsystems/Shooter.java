package org.texastorque.subsystems;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatorCurrentLimitConfiguration;
import com.ctre.phoenix.motorcontrol.SupplyCurrentLimitConfiguration;

import org.texastorque.Ports;
import org.texastorque.torquelib.base.TorqueState;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.motors.TorqueFalcon;
import org.texastorque.torquelib.motors.TorqueSparkMax;
import org.texastorque.torquelib.util.KPID;

public final class Shooter extends TorqueSubsystem {
    private static volatile Shooter instance;

    public static enum ShooterState implements TorqueState {
        OFF, REGRESSION;
    }

    private final TorqueSparkMax hood;
    private final TorqueFalcon flywheel;

    private Shooter() {
        flywheel = new TorqueFalcon(Ports.SHOOTER.FLYWHEEL.LEFT);
        flywheel.addFollower(Ports.SHOOTER.FLYWHEEL.RIGHT, true);

        flywheel.configurePID(new KPID(0.0999999046, 5e-05, 0, 0.0603409074, -1, 1, 1000));
        flywheel.setNeutralMode(NeutralMode.Coast);
        flywheel.setStatorLimit(new StatorCurrentLimitConfiguration(true, 80, 1, .001));
        flywheel.setSupplyLimit(new SupplyCurrentLimitConfiguration(true, 80, 1, .001));

        hood = new TorqueSparkMax(Ports.SHOOTER.HOOD);
        hood.configurePID(new KPID(.1, .001,0, 0, -.70, .70, .3));
        hood.configurePositionalCANFrame();
        hood.burnFlash();


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

    public boolean isReady() {
        return false;
    }

    public static synchronized Shooter getInstance() {
        return instance == null ? instance = new Shooter() : instance;
    }
}
