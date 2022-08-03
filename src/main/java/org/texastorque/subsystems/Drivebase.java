/**
 * Copyright 2022 Texas Torque.
 * 
 * This file is part of Clutch-2022, which is not licensed for distribution.
 * For more details, see ./license.txt or write <jus@gtsbr.org>.
 */
package org.texastorque.subsystems;

import edu.wpi.first.math.MatBuilder;
import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.Nat;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.Vector;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.Ports;
import org.texastorque.Robot;
import org.texastorque.Subsystems;
import org.texastorque.torquelib.base.TorqueMode;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.base.TorqueSubsystemState;
import org.texastorque.torquelib.control.TorquePID;
import org.texastorque.torquelib.modules.TorqueSwerveModule2021;
import org.texastorque.torquelib.sensors.TorqueNavXGyro;
import org.texastorque.torquelib.util.TorqueSwerveOdometry;
import org.texastorque.torquelib.util.TorqueUtil;

/**
 * The drivebase subsystem. Drives with 4 2021 swerve modules.
 *
 * @author Jack Pittenger
 * @author Justus Languell
 */
@SuppressWarnings("deprecation")
public final class Drivebase extends TorqueSubsystem implements Subsystems {
    private static volatile Drivebase instance;

    public enum DrivebaseState implements TorqueSubsystemState { ROBOT_RELATIVE, FIELD_RELATIVE, X_FACTOR, ALIGN }

    public static final double DRIVE_MAX_TRANSLATIONAL_SPEED = 4, DRIVE_MAX_TRANSLATIONAL_ACCELERATION = 2,
                               DRIVE_MAX_ROTATIONAL_SPEED = 6;

    private static final double DRIVE_GEARING = .1875, // Drive rotations per motor rotations
            DRIVE_WHEEL_RADIUS = Units.inchesToMeters(1.788), DISTANCE_TO_CENTER_X = Units.inchesToMeters(10.875),
                                DISTANCE_TO_CENTER_Y = Units.inchesToMeters(10.875);
// 
//     public static final KPID DRIVE_PID = new KPID(.00048464, 0, 0, 0, -1, 1, .2), 
                        // ROTATE_PID = new KPID(.3, 0, 0, 0, -1, 1);
    public static final TorquePID DRIVE_PID = TorquePID.create(.00048464).addIntegralZone(.2).build();
    public static final TorquePID ROTATE_PID = TorquePID.create(.3).build();

    public static final SimpleMotorFeedforward DRIVE_FEED_FORWARD = new SimpleMotorFeedforward(.27024, 2.4076, .5153);

    private static final Translation2d locationBackLeft = new Translation2d(DISTANCE_TO_CENTER_X, -DISTANCE_TO_CENTER_Y),
                                locationBackRight = new Translation2d(DISTANCE_TO_CENTER_X, DISTANCE_TO_CENTER_Y),
                                locationFrontLeft = new Translation2d(-DISTANCE_TO_CENTER_X, -DISTANCE_TO_CENTER_Y),
                                locationFrontRight = new Translation2d(-DISTANCE_TO_CENTER_X, DISTANCE_TO_CENTER_Y);

    private static final Matrix<N3, N1> STATE_STDS = new MatBuilder<>(Nat.N3(), Nat.N1()).fill(0.4, 0.4, .9);
    private static final Matrix<N1, N1> LOCAL_STDS = new MatBuilder<>(Nat.N1(), Nat.N1()).fill(.3);
    private static final Matrix<N3, N1> VISION_STDS = new MatBuilder<>(Nat.N3(), Nat.N1()).fill(.5, .5, 1);

    // private static final Vector<N3> STATE_STDS = VecBuilder.fill(.4, .4, .9);
    // private static final Vector<N1> LOCAL_STDS = VecBuilder.fill(Units.degreesToRadians(.3));
    // private static final Vector<N3> VISION_STDS = VecBuilder.fill(.5, .5, 1.);

    private final SwerveDriveKinematics kinematics;
    private final SwerveDrivePoseEstimator poseEstimator;
    private final Field2d field2d = new Field2d();

    private final TorqueSwerveModule2021 backLeft, backRight, frontLeft, frontRight;
    private SwerveModuleState[] swerveModuleStates; // This can be made better
    private int hotdogIndex = -1;

    public final void setHotdogIndex(final int index) {
        hotdogIndex = index;
    }

    private DrivebaseState state = DrivebaseState.FIELD_RELATIVE;
    private ChassisSpeeds speeds = new ChassisSpeeds(0, 0, 0);

    private final TorqueNavXGyro gyro = TorqueNavXGyro.getInstance();

    private double translationalSpeedCoef, rotationalSpeedCoef;
    private final double SHOOTING_TRANSLATIONAL_SPEED_COEF = .4, SHOOTING_ROTATIONAL_SPEED_COEF = .5;

    private Drivebase() {
        backLeft = buildSwerveModule(0, Ports.DRIVEBASE.TRANSLATIONAL.LEFT.BACK, Ports.DRIVEBASE.ROTATIONAL.LEFT.BACK);
        backLeft.setLogging(true);
        backRight =
                buildSwerveModule(1, Ports.DRIVEBASE.TRANSLATIONAL.RIGHT.BACK, Ports.DRIVEBASE.ROTATIONAL.RIGHT.BACK);
        frontLeft =
                buildSwerveModule(2, Ports.DRIVEBASE.TRANSLATIONAL.LEFT.FRONT, Ports.DRIVEBASE.ROTATIONAL.LEFT.FRONT);
        frontRight =
                buildSwerveModule(3, Ports.DRIVEBASE.TRANSLATIONAL.RIGHT.FRONT, Ports.DRIVEBASE.ROTATIONAL.RIGHT.FRONT);

        kinematics =
                new SwerveDriveKinematics(locationBackLeft, locationBackRight, locationFrontLeft, locationFrontRight);

        poseEstimator = new SwerveDrivePoseEstimator(gyro.getRotation2dClockwise(),
                                new Pose2d(), kinematics, STATE_STDS,
                                LOCAL_STDS, VISION_STDS, Robot.PERIOD);

        reset();
    }

    public final void setSpeedCoefs(final double translational, final double rotational) {
        this.translationalSpeedCoef = translational;
        this.rotationalSpeedCoef = rotational;
    }

    public final void setState(final DrivebaseState state) { this.state = state; }

    public final DrivebaseState getState() { return state; }

    public final void setSpeeds(final ChassisSpeeds speeds) { this.speeds = speeds; }

    public final ChassisSpeeds getSpeeds() { return speeds; }

    @Override
    public final void initialize(final TorqueMode mode) {
        reset();
        state = mode.isTeleop() ? DrivebaseState.FIELD_RELATIVE : DrivebaseState.ROBOT_RELATIVE;
    }

    @Override
    public final void update(final TorqueMode mode) {
        if (shooter.getCamera().getNumberOfTargets() >= 3)
            updateWithVision();


        if (state == DrivebaseState.ALIGN)
            for (int i = 0; i < swerveModuleStates.length; i++)                
                swerveModuleStates[i] = new SwerveModuleState(0, Rotation2d.fromDegrees(0));

        else if (state == DrivebaseState.X_FACTOR)
            for (int i = 0; i < swerveModuleStates.length; i++)
                swerveModuleStates[i] = new SwerveModuleState(0, new Rotation2d((i == 0 || i == 3) ? 135 : 45));

        else if (state == DrivebaseState.FIELD_RELATIVE)
            swerveModuleStates = kinematics.toSwerveModuleStates(ChassisSpeeds.fromFieldRelativeSpeeds(
                    speeds.vxMetersPerSecond *
                            (shooter.isShooting() ? SHOOTING_TRANSLATIONAL_SPEED_COEF : translationalSpeedCoef),
                    speeds.vyMetersPerSecond *
                            (shooter.isShooting() ? SHOOTING_TRANSLATIONAL_SPEED_COEF : translationalSpeedCoef),
                    speeds.omegaRadiansPerSecond *
                            (shooter.isShooting() ? SHOOTING_ROTATIONAL_SPEED_COEF : rotationalSpeedCoef),
                    gyro.getRotation2dClockwise()));


        else if (state == DrivebaseState.ROBOT_RELATIVE)
            swerveModuleStates = kinematics.toSwerveModuleStates(speeds);

        // Literaly does the same thing LMFAO, my implementaiton is actually better 0:
        // TorqueSwerveModule2021.equalizedDriveRatio(swerveModuleStates, DRIVE_MAX_TRANSLATIONAL_SPEED);
        SwerveDriveKinematics.desaturateWheelSpeeds(swerveModuleStates, DRIVE_MAX_TRANSLATIONAL_SPEED);

        setDesiredState(frontLeft, 2);
        setDesiredState(frontRight, 1);
        setDesiredState(backLeft, 0);
        setDesiredState(backRight, 3);

        // odometry.update(gyro.getRotation2dClockwise().times(-1), frontLeft.getState(), frontRight.getState(),
                        // backLeft.getState(), backRight.getState());

        poseEstimator.update(gyro.getRotation2d().times(-1),
                frontLeft.getState(), frontRight.getState(),
                backLeft.getState(), backRight.getState());
        field2d.setRobotPose(poseEstimator.getEstimatedPosition());
                        
        log();
    }

    private final void setDesiredState(TorqueSwerveModule2021 module, final int index) {
        if (index == hotdogIndex) module.hotdog();
        else module.setDesiredState(swerveModuleStates[index]);
    }

    public final SwerveDriveKinematics getKinematics() { return kinematics; }

    public final SwerveDrivePoseEstimator getOdometry() { return poseEstimator; }

    public final SwerveDrivePoseEstimator getPoseEstimator() { return poseEstimator; }

    public final Pose2d getPose() { return poseEstimator.getEstimatedPosition(); }

    public final Pose2d getPoseEstimated() { return poseEstimator.getEstimatedPosition(); }

    public final TorqueNavXGyro getGyro() { return gyro; }

    public final void log() {
        SmartDashboard.putString("OdomPos", String.format("(%02.3f, %02.3f)", getPose().getX(), getPose().getY()));
        SmartDashboard.putString("FusedPos", String.format("(%02.3f, %02.3f)", getPoseEstimated().getX(), getPoseEstimated().getY()));

        SmartDashboard.putNumber("Odom Rot", getPose().getRotation().getDegrees());
        SmartDashboard.putNumber("Gyro Rot", gyro.getRotation2dClockwise().getDegrees());
        SmartDashboard.putNumber("Gyro Rot -1", gyro.getRotation2dClockwise().times(-1).getDegrees());

        SmartDashboard.putString("Speeds", String.format("(%02.3f, %02.3f, %02.3f)", speeds.vxMetersPerSecond,
                                                         speeds.vyMetersPerSecond, speeds.omegaRadiansPerSecond));

        SmartDashboard.putString("Drive State", state.toString());
    }

    public final void reset() { speeds = new ChassisSpeeds(0, 0, 0); }

    private final TorqueSwerveModule2021 buildSwerveModule(final int id, final int drivePort, final int rotatePort) {
        return new TorqueSwerveModule2021(id, drivePort, rotatePort, DRIVE_GEARING, DRIVE_WHEEL_RADIUS, DRIVE_PID,
                                          ROTATE_PID, DRIVE_MAX_TRANSLATIONAL_SPEED,
                                          DRIVE_MAX_TRANSLATIONAL_ACCELERATION, DRIVE_FEED_FORWARD);
    }

    public final void updateWithVision() {
        try {
            final Pose2d pose = shooter.getCamera().getRobotPose(shooter.getCamera(), getGyro().getRotation2dCounterClockwise(), Rotation2d.fromDegrees(shooter.getCamera().getTargetPitch()), Rotation2d.fromDegrees(shooter.getCamera().getTargetYaw()), Shooter.TARGET_HEIGHT,
                    Shooter.CAMERA_HEIGHT, Shooter.CAMERA_ANGLE, Shooter.TURRET_RADIUS, turret.getDegrees(), Shooter.HUB_RADIUS, Shooter.HUB_CENTER_POSITION.getX(),
                    Shooter.HUB_CENTER_POSITION.getY());
            SmartDashboard.putString("VisionPos", String.format("(%02.3f, %02.3f)", pose.getX(), pose.getY()));
            poseEstimator.addVisionMeasurement(pose, TorqueUtil.time() - shooter.getCamera().getLatency() - .0011);
        } catch (final Exception e) {
            System.out.println("Failed to add vision measurement to pose estimator."
                    + "Likely due to Cholesky decomposition failing due to it not being the sqrt method."
                    + "Full details on the error: \n" + e.getMessage());
        }
    }

    /**
     * In a nutshell, we want to prevent cholesky decomposition failures by limiting
     * the addition of vision to joint probabilistic space (± 2 std).
     * 
     * @return If it is highly likely that cholesky decomposition will pass.
     */
    private final boolean choleskyErrorUnlikely(final Pose2d pose) {
        final Pose2d estimatedPosition = poseEstimator.getEstimatedPosition();
        final double diffX = Math.abs(estimatedPosition.getX() - pose.getX());
        final double diffY = Math.abs(estimatedPosition.getY() - pose.getY());
        return (diffX < VISION_STDS.get(1, 1) * 2) && (diffY < VISION_STDS.get(2, 1) * 2);
    }

    public static final synchronized Drivebase getInstance() {
        return instance == null ? instance = new Drivebase() : instance;
    }
}