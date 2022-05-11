package org.texastorque.subsystems;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatorCurrentLimitConfiguration;
import com.ctre.phoenix.motorcontrol.SupplyCurrentLimitConfiguration;

import org.texastorque.Ports;
import org.texastorque.torquelib.base.TorqueState;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.motors.TorqueFalcon;
import org.texastorque.torquelib.motors.TorqueSparkMax;
import org.texastorque.torquelib.sensors.TorqueLight;
import org.texastorque.torquelib.util.KPID;
import org.texastorque.torquelib.util.TorqueMathUtil;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public final class Shooter extends TorqueSubsystem {
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

    public enum ShooterState implements TorqueState {
        OFF, REGRESSION, SETPOINT, DISTANCE;
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

        flywheel.configurePID(new KPID(0.0999999046, 5e-05, 0, 0.0603409074, -1, 1, 1000));
        flywheel.setNeutralMode(NeutralMode.Coast);
        flywheel.setStatorLimit(new StatorCurrentLimitConfiguration(true, 80, 1, .001));
        flywheel.setSupplyLimit(new SupplyCurrentLimitConfiguration(true, 80, 1, .001));

        hood = new TorqueSparkMax(Ports.SHOOTER.HOOD);
        hood.configurePID(new KPID(.1, .001,0, 0, -.70, .70, .3));
        hood.configurePositionalCANFrame();
        hood.burnFlash();
    }

    public void setState(final ShooterState state) { 
        this.state = state; 
    }

    public void setHoodPosition(final double hoodSetpoint) {
        this.hoodSetpoint = hoodSetpoint;
    }

    public void setFlywheelSpeed(final double flywheelSpeed) {
        this.flywheelSpeed = flywheelSpeed;
    }

    public void setDistance(final double distance) {
        this.distance = distance;
    }

    @Override
    public void initTeleop() {
        state = ShooterState.OFF; 
    }

    @Override
    public void updateTeleop() {
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
            return;
        }

        flywheel.setVelocityRPM(clampRPM(flywheelSpeed));
        hood.setPosition(clampHood(hoodSetpoint));

        SmartDashboard.putNumber("Flywheel Real", flywheel.getVelocityRPM());
        SmartDashboard.putNumber("Flywheel Req", flywheelSpeed);

        SmartDashboard.putNumber("Flywheel Delta", Math.abs(flywheelSpeed - flywheel.getVelocityRPM()));
        SmartDashboard.putBoolean("IsShooting", isShooting());
        SmartDashboard.putBoolean("IsReady", isReady());
    }

    @Override
    public void initAuto() {
        state = ShooterState.OFF; 
    }

    @Override
    public void updateAuto() {
        updateTeleop();
    }

    public boolean isShooting() {
        return state != ShooterState.OFF;
    }

    public boolean isReady() {
        return isShooting() && Math.abs(flywheelSpeed - flywheel.getVelocityRPM()) < ERROR;
    }

    /**
     * @param distance Distance (m)
     * @return RPM the shooter should go at
     */
    private double regressionRPM(final double distance) {
        return clampRPM((173.5 * distance) + 1316);
    }

    /**
     * @param distance Distance (m)
     * @return Hood the shooter should go at
     */
    private double regressionHood(final double distance) {
        if (distance > 3.5) return HOOD_MAX;
        return clampHood(-72.22 * Math.exp(-0.5019 * distance) + 46.01);
    }

    private double clampRPM(final double rpm) {
        return TorqueMathUtil.constrain(rpm, FLYWHEEEL_IDLE, FLYWHEEEL_MAX);
    }

    private double clampHood(final double hood) {
        return TorqueMathUtil.constrain(hood, HOOD_MIN, HOOD_MAX);
    }

    public TorqueLight getCamera() {
        return camera;
    }

    public static synchronized Shooter getInstance() {
        return instance == null ? instance = new Shooter() : instance;
    }
}
