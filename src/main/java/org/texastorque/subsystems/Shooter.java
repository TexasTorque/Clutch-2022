/**
 * Copyright 2022 Texas Torque.
 *
 * This file is part of Clutch-2022, which is not licensed for distribution.
 * For more details, see ./license.txt or write <jus@gtsbr.org>.
 */
package org.texastorque.subsystems;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatorCurrentLimitConfiguration;
import com.ctre.phoenix.motorcontrol.SupplyCurrentLimitConfiguration;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.photonvision.PhotonUtils;
import org.texastorque.Ports;
import org.texastorque.Subsystems;
import org.texastorque.subsystems.Turret.TurretState;
import org.texastorque.torquelib.base.TorqueMode;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.base.TorqueSubsystemState;
import org.texastorque.torquelib.control.TorquePID;
import org.texastorque.torquelib.motors.TorqueFalcon;
import org.texastorque.torquelib.motors.TorqueSparkMax;
import org.texastorque.torquelib.sensors.TorqueLight;
import org.texastorque.torquelib.util.KPID;
import org.texastorque.torquelib.util.TorqueMath;
import org.texastorque.torquelib.util.TorqueUtil;

public final class Shooter extends TorqueSubsystem implements Subsystems {
    private static volatile Shooter instance;

    public static final double HOOD_MIN = 0, HOOD_MAX = 40, ERROR = 100, FLYWHEEEL_MAX = 3000, FLYWHEEEL_IDLE = 0,
            FLYWHEEEL_REDUCTION = 5 / 3., CAMERA_HEIGHT = Units.inchesToMeters(33),
            TARGET_HEIGHT = 2.6416;

    public static final double HUB_RADIUS = .6778625; //

    public static final double TURRET_RADIUS = Units.inchesToMeters(7.2);

    public static final Rotation2d CAMERA_ANGLE = Rotation2d.fromDegrees(30);

    public static final Translation2d HUB_CENTER_POSITION = new Translation2d(8.2, 4.1);

    public static final Pose2d HUB_ORIGIN = new Pose2d(HUB_CENTER_POSITION.getX(), HUB_CENTER_POSITION.getY(),
            new Rotation2d());

    public static final Translation2d CAMERA_TO_ROBOT = new Translation2d(Units.inchesToMeters(2), CAMERA_HEIGHT);

    private static final Transform2d TRANSFORM_ADJUSTMENT = new Transform2d(new Translation2d(.9, 0), new Rotation2d());

    public enum ShooterState implements TorqueSubsystemState {
        OFF,
        REGRESSION,
        SETPOINT,
        DISTANCE,
        WARMUP;

        public final boolean isShooting() {
            return this != OFF && this != WARMUP;
        }
    }

    private final TorqueLight camera;

    private final TorqueSparkMax hood;
    private final TorqueFalcon flywheel;

    private double hoodSetpoint, flywheelSpeed, distance, autoOffset = 0;

    private ShooterState state = ShooterState.OFF;

    private Shooter() {
        camera = new TorqueLight();

        flywheel = new TorqueFalcon(Ports.SHOOTER.FLYWHEEL.LEFT);
        flywheel.addFollower(Ports.SHOOTER.FLYWHEEL.RIGHT, true);

        flywheel.configurePID(new KPID(0.5, 5e-05, 0, 0.0603409074, -1, 1, 1000));
        // flywheel.configurePID(new KPID(0.0999999046, 0, 0,  0.0603409074, -1, 1, 1000));

        // flywheel.configurePID(TorquePID.create(0.0999999046)
        //         .addFeedForward(0.0603409074)
        //         .addFeedForward(0.07)
        //         .addIntegralZone(1000).build());

        // flywheel.configurePID(TorquePID.create(0.0999999046)
        //         // .addFeedForward(0.0603409074)
        //         .addFeedForward(0.08)
        //         .addIntegralZone(1000).build());

        flywheel.setNeutralMode(NeutralMode.Coast);
        flywheel.setStatorLimit(new StatorCurrentLimitConfiguration(true, 80, 1, .001));
        flywheel.setSupplyLimit(new SupplyCurrentLimitConfiguration(true, 80, 1, .001));

        hood = new TorqueSparkMax(Ports.SHOOTER.HOOD);
        // hood.configurePID(new KPID(.1, .001, 0, 0, -.70, .70, .3));
        hood.configurePID(TorquePID.create(.1).addIntegral(.001).addOutputRange(-.7, .7).addIntegralZone(.3).build());

        hood.configurePositionalCANFrame();
        hood.burnFlash();
    }

    public final void setState(final ShooterState state) {
        this.state = state;
    }

    public final void setHoodPosition(final double hoodSetpoint) {
        this.hoodSetpoint = hoodSetpoint;
    }

    public final void setFlywheelSpeed(final double flywheelSpeed) {
        this.flywheelSpeed = flywheelSpeed;
    }

    public final void setDistance(final double distance) {
        this.distance = distance;
    }

    public final void setAutoOffset(final double autoOffset) {
        this.autoOffset = autoOffset;
    }

    @Override
    public final void initialize(final TorqueMode mode) {
        state = ShooterState.OFF;
    }

    @Override
    public final void update(final TorqueMode mode) {
        camera.update();

        if (climber.hasStarted()) {
            flywheelSpeed = 0;
            hoodSetpoint = HOOD_MIN;
        } else if (state == ShooterState.REGRESSION) {
            distance = getDistance();
            flywheelSpeed = regressionRPM(distance) + (mode.isAuto() ? autoOffset : 0);
            hoodSetpoint = regressionHood(distance);
        } else if (state == ShooterState.DISTANCE) {
            distance = getDistance();
            flywheelSpeed = regressionRPM(distance);
            hoodSetpoint = regressionHood(distance);
        } else if (state == ShooterState.SETPOINT || state == ShooterState.WARMUP) {
            // LMFAO
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
        SmartDashboard.putNumber("IDIST", getDistance());

        SmartDashboard.putNumber("Flywheel Delta", flywheelSpeed - flywheel.getVelocityRPM());
        SmartDashboard.putBoolean("Is Shooting", isShooting());
        SmartDashboard.putBoolean("Is Ready", isReady());
    }

    public final boolean isShooting() {
        return state.isShooting();
    }

    public final boolean isReady() {
        return isShooting() && Math.abs(flywheelSpeed - flywheel.getVelocityRPM()) < ERROR;
    }

    /**
     * @param distance Distance (m)
     * @return RPM the shooter should go at
     */
    private final double regressionRPM(final double distance) {
        // return clampRPM(26.83 * distance * 24 + 1350);
        // return clampRPM(285.7 * distance + 893);
        // return clampRPM(520 * distance + 500); // 450 => 470
        return clampRPM(500 * distance + 100);
    }

    /**
     * @param distance Distance (m)
     * @return Hood the shooter should go at
     */
    private final double regressionHood(final double distance) {
        // if (distance > 3.5) return HOOD_MAX;
        // return clampHood(1.84 * distance * 24 + 19.29 - 5);
        // return clampHood(14.29 * distance - 3);
        return clampHood(12 * distance - 2);
    }

    private final double clampRPM(final double rpm) {
        return TorqueMath.constrain(rpm, FLYWHEEEL_IDLE, FLYWHEEEL_MAX);
    }

    private final double clampHood(final double hood) {
        return TorqueMath.constrain(hood, HOOD_MIN, HOOD_MAX);
    }

    public final TorqueLight getCamera() {
        return camera;
    }

    public final ShooterState getState() {
        return state;
    }

    public final double getDistance() {
        return TorqueLight.getDistanceToElevatedTarget(camera, CAMERA_HEIGHT, TARGET_HEIGHT, CAMERA_ANGLE);
    }

    public final Pose2d getVisionPositionEstimate() {
        TorqueUtil.notImplemented();

        return new Pose2d();
    }

    public final double calculateDistance() {
        return TorqueLight.getDistanceToElevatedTarget(camera, CAMERA_HEIGHT, TARGET_HEIGHT, CAMERA_ANGLE);
    }

    public final Pose2d getEstimatedPositionRelativeToRobot() {
        final Transform2d targetRelativeToCamera = camera.getCameraToTarget();
        final Transform2d targetRelativeToCenterOfHub = targetRelativeToCamera.plus(TRANSFORM_ADJUSTMENT);

        final Pose2d estimatedPositionOfRobot = PhotonUtils.estimateFieldToRobot(
                targetRelativeToCenterOfHub, HUB_ORIGIN,
                new Transform2d(CAMERA_TO_ROBOT, Rotation2d.fromDegrees(-turret.getDegrees())));

        return estimatedPositionOfRobot;
    }

    public static final synchronized Shooter getInstance() {
        return instance == null ? instance = new Shooter() : instance;
    }
}
