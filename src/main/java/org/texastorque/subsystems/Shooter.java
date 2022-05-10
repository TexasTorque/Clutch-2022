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
import org.texastorque.torquelib.util.TorqueMathUtil;

public final class Shooter extends TorqueSubsystem {
    private static volatile Shooter instance;

    public static final double HOOD_MIN = 0;
    public static final double HOOD_MAX = 40;
    public static final double ERROR = 45;
    public static final double FLYWHEEEL_MAX = 3000;
    public static final double FLYWHEEEL_REDUCTION = 5 / 3.;

    public static enum ShooterState implements TorqueState {
        OFF, REGRESSION;
    }

    private final TorqueSparkMax hood;
    private final TorqueFalcon flywheel;

    private ShooterState state = ShooterState.OFF;

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
        state = ShooterState.OFF; 
    }

    @Override
    public void updateTeleop() {
        if (state == ShooterState.REGRESSION) {
            flywheel.setVelocityRPM(regressionRPM(0));
            hood.setPosition(regressionHood(0));
        }
        
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

    /**
     * @param distance Distance (m)
     * @return RPM the shooter should go at
     */
    private double regressionRPM(final double distance) {
        return TorqueMathUtil.constrain((173.5 * distance) + 1316, 0, FLYWHEEEL_MAX);
    }

    /**
     * @param distance Distance (m)
     * @return Hood the shooter should go at
     */

    private double regressionHood(final double distance) {
        if (distance > 3.5) return HOOD_MAX;
        return TorqueMathUtil.constrain(-72.22 * Math.exp(-0.5019 * distance) + 46.01,
                HOOD_MIN, HOOD_MAX);
        }

    public static synchronized Shooter getInstance() {
        return instance == null ? instance = new Shooter() : instance;
    }
}
