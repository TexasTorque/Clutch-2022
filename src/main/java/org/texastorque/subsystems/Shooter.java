package org.texastorque.subsystems;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatorCurrentLimitConfiguration;
import com.ctre.phoenix.motorcontrol.SupplyCurrentLimitConfiguration;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.Ports;
import org.texastorque.Subsystems;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.base.TorqueSubsystemState;
import org.texastorque.torquelib.motors.TorqueFalcon;
import org.texastorque.torquelib.motors.TorqueSparkMax;
import org.texastorque.torquelib.sensors.TorqueLight;
import org.texastorque.torquelib.util.KPID;
import org.texastorque.torquelib.util.TorqueMathUtil;

public final class Shooter extends TorqueSubsystem implements Subsystems {
    private static volatile Shooter instance;

    public static final double HOOD_MIN = 0;
    public static final double HOOD_MAX = 40;
    public static final double ERROR = 83.24;
    public static final double FLYWHEEEL_MAX = 3000;
    public static final double FLYWHEEEL_IDLE = 0;
    public static final double FLYWHEEEL_REDUCTION = 5 / 3.;
    public static final double CAMERA_HEIGHT = Units.inchesToMeters(38.5);
    public static final double TARGET_HEIGHT = 2.6416;
    public static final Rotation2d CAMERA_ANGLE = Rotation2d.fromDegrees(30);

    public enum ShooterState implements TorqueSubsystemState {
        OFF,
        REGRESSION,
        SETPOINT,
        DISTANCE;
    }

    private final TorqueLight camera;

    private final TorqueSparkMax hood;
    private final TorqueFalcon flywheel;

    private double hoodSetpoint, flywheelSpeed, distance;

    private ShooterState state = ShooterState.OFF;

    private Shooter() {
        camera = new TorqueLight(CAMERA_HEIGHT, TARGET_HEIGHT, CAMERA_ANGLE);

        flywheel = new TorqueFalcon(Ports.SHOOTER.FLYWHEEL.LEFT);
        flywheel.addFollower(Ports.SHOOTER.FLYWHEEL.RIGHT, true);

        //flywheel.configurePID(new KPID(0.0999999046, 5e-05, 0, 0.0603409074, -1, 1, 1000));
        flywheel.configurePID(new KPID(0.5, 5e-05, 0, 0.0603409074, -1, 1, 1000));
        flywheel.setNeutralMode(NeutralMode.Coast);
        flywheel.setStatorLimit(new StatorCurrentLimitConfiguration(true, 80, 1, .001));
        flywheel.setSupplyLimit(new SupplyCurrentLimitConfiguration(true, 80, 1, .001));

        hood = new TorqueSparkMax(Ports.SHOOTER.HOOD);
        hood.configurePID(new KPID(.1, .001, 0, 0, -.70, .70, .3));
        hood.configurePositionalCANFrame();
        hood.burnFlash();
    }

    public final void setState(final ShooterState state) { this.state = state; }

    public final void setHoodPosition(final double hoodSetpoint) { this.hoodSetpoint = hoodSetpoint; }

    public final void setFlywheelSpeed(final double flywheelSpeed) { this.flywheelSpeed = flywheelSpeed; }

    public final void setDistance(final double distance) { this.distance = distance; }

    @Override
    public final void initTeleop() {
        state = ShooterState.OFF;
    }

    @Override
    public final void updateTeleop() {
        // camera.update();

        if (state == ShooterState.REGRESSION) {
            // distance = camera.getDistance();
            distance = 0;
            flywheelSpeed = regressionRPM(distance);
            hoodSetpoint = regressionHood(distance);
        } else if (state == ShooterState.DISTANCE) {
            flywheelSpeed = regressionRPM(distance);
            hoodSetpoint = regressionHood(distance);
        } else if (state == ShooterState.SETPOINT) {

        } else {
            flywheelSpeed = 0;
            flywheel.setPercent(FLYWHEEEL_IDLE);
        }

        if (state != ShooterState.OFF) {
            flywheel.setVelocityRPM(clampRPM(flywheelSpeed));
        }
        hood.setPosition(clampHood(hoodSetpoint));

        TorqueSubsystemState.logState(state);

        SmartDashboard.putNumber("Flywheel Real", flywheel.getVelocityRPM());
        SmartDashboard.putNumber("Flywheel Req", flywheelSpeed);

        SmartDashboard.putNumber("Flywheel Delta", Math.abs(flywheelSpeed - flywheel.getVelocityRPM()));
        SmartDashboard.putBoolean("Is Shooting", isShooting());
        SmartDashboard.putBoolean("Is Ready", isReady());
    }

    @Override
    public final void initAuto() {
        state = ShooterState.OFF;
    }

    @Override
    public final void updateAuto() {
        updateTeleop();
    }

    public final boolean isShooting() { return state != ShooterState.OFF; }

    public final boolean isReady() {
        return isShooting() && Math.abs(flywheelSpeed - flywheel.getVelocityRPM()) < ERROR;
    }

    /**
     * @param distance Distance (m)
     * @return RPM the shooter should go at
     */
    private final double regressionRPM(final double distance) { return clampRPM((173.5 * distance) + 1316); }

    /**
     * @param distance Distance (m)
     * @return Hood the shooter should go at
     */
    private final double regressionHood(final double distance) {
        if (distance > 3.5) return HOOD_MAX;
        return clampHood(-72.22 * Math.exp(-0.5019 * distance) + 46.01);
    }

    private final double clampRPM(final double rpm) {
        return TorqueMathUtil.constrain(rpm, FLYWHEEEL_IDLE, FLYWHEEEL_MAX);
    }

    private final double clampHood(final double hood) { return TorqueMathUtil.constrain(hood, HOOD_MIN, HOOD_MAX); }

    public final TorqueLight getCamera() { return camera; }

    public static final synchronized Shooter getInstance() {
        return instance == null ? instance = new Shooter() : instance;
    }
}
