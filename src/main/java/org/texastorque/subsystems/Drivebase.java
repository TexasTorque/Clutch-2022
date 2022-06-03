package org.texastorque.subsystems;

import edu.wpi.first.math.MatBuilder;
import edu.wpi.first.math.Nat;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.Ports;
import org.texastorque.Subsystems;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.base.TorqueSubsystemState;
import org.texastorque.torquelib.modules.TorqueSwerveModule2021;
import org.texastorque.torquelib.sensors.TorqueNavXGyro;
import org.texastorque.torquelib.util.KPID;
import org.texastorque.torquelib.util.TorqueSwerveOdometry;

/**
 * The drivebase subsystem. Drives with 4 2021 swerve modules.
 *
 * @author Jack Pittenger
 * @author Justus Languell
 */
public final class Drivebase extends TorqueSubsystem implements Subsystems {
    private static volatile Drivebase instance;

    public enum DrivebaseState implements TorqueSubsystemState { ROBOT_RELATIVE, FIELD_RELATIVE, X_FACTOR }

    public static final double DRIVE_MAX_TRANSLATIONAL_SPEED = 4, DRIVE_MAX_TRANSLATIONAL_ACCELERATION = 2,
                               DRIVE_MAX_ROTATIONAL_SPEED = 6;

    private static final double DRIVE_GEARING = .1875, // Drive rotations per motor rotations
            DRIVE_WHEEL_RADIUS = Units.inchesToMeters(1.788), DISTANCE_TO_CENTER_X = Units.inchesToMeters(10.875),
                                DISTANCE_TO_CENTER_Y = Units.inchesToMeters(10.875);

    public static final KPID DRIVE_PID = new KPID(.00048464, 0, 0, 0, -1, 1, .2), ROTATE_PID =
                                                                                          new KPID(.3, 0, 0, 0, -1, 1);

    public static final SimpleMotorFeedforward DRIVE_FEED_FORWARD = new SimpleMotorFeedforward(.27024, 2.4076, .5153);

    private final Translation2d locationBackLeft = new Translation2d(DISTANCE_TO_CENTER_X, -DISTANCE_TO_CENTER_Y),
                                locationBackRight = new Translation2d(DISTANCE_TO_CENTER_X, DISTANCE_TO_CENTER_Y),
                                locationFrontLeft = new Translation2d(-DISTANCE_TO_CENTER_X, -DISTANCE_TO_CENTER_Y),
                                locationFrontRight = new Translation2d(-DISTANCE_TO_CENTER_X, DISTANCE_TO_CENTER_Y);

    private final SwerveDriveKinematics kinematics;
    private final TorqueSwerveOdometry odometry;
    private final SwerveDrivePoseEstimator poseEstimator;

    private final TorqueSwerveModule2021 backLeft, backRight, frontLeft, frontRight;
    private SwerveModuleState[] swerveModuleStates; // This can be made better

    private DrivebaseState state = DrivebaseState.FIELD_RELATIVE;
    private ChassisSpeeds speeds = new ChassisSpeeds(0, 0, 0);

    private final TorqueNavXGyro gyro = TorqueNavXGyro.getInstance();

    private final TorqueSwerveModule2021 buildSwerveModule(final int id, final int drivePort, final int rotatePort) {
        return new TorqueSwerveModule2021(id, drivePort, rotatePort, DRIVE_GEARING, DRIVE_WHEEL_RADIUS, DRIVE_PID,
                                          ROTATE_PID, DRIVE_MAX_TRANSLATIONAL_SPEED,
                                          DRIVE_MAX_TRANSLATIONAL_ACCELERATION, DRIVE_FEED_FORWARD);
    }

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

        odometry = new TorqueSwerveOdometry(kinematics, gyro.getRotation2dClockwise());

        poseEstimator = new SwerveDrivePoseEstimator(gyro.getRotation2dCounterClockwise(), new Pose2d(), kinematics,
                                                     new MatBuilder<>(Nat.N3(), Nat.N1()).fill(0.2, 0.2, 0.2),
                                                     new MatBuilder<>(Nat.N1(), Nat.N1()).fill(.2),
                                                     new MatBuilder<>(Nat.N3(), Nat.N1()).fill(.2, .2, .2));

        reset();
    }

    public final void setState(final DrivebaseState state) { this.state = state; }

    public final DrivebaseState getState() { return state; }

    public final void setSpeeds(final ChassisSpeeds speeds) { this.speeds = speeds; }

    public final ChassisSpeeds getSpeeds() { return speeds; }

    @Override
    public final void initTeleop() {
        reset();
        state = DrivebaseState.FIELD_RELATIVE;
    }

    @Override
    public final void updateTeleop() {
        SmartDashboard.putNumber("Speed X", speeds.vxMetersPerSecond);
        SmartDashboard.putNumber("Speed Y", speeds.vyMetersPerSecond);
        SmartDashboard.putNumber("Speed R", speeds.omegaRadiansPerSecond);

        if (state == DrivebaseState.X_FACTOR) {
            swerveModuleStates[0].angle = Rotation2d.fromDegrees(135);
            swerveModuleStates[1].angle = Rotation2d.fromDegrees(45);
            swerveModuleStates[2].angle = Rotation2d.fromDegrees(45);
            swerveModuleStates[3].angle = Rotation2d.fromDegrees(135);
        }

        else if (state == DrivebaseState.FIELD_RELATIVE)
            swerveModuleStates = kinematics.toSwerveModuleStates(
                    ChassisSpeeds.fromFieldRelativeSpeeds(speeds.vxMetersPerSecond, speeds.vyMetersPerSecond,
                                                          speeds.omegaRadiansPerSecond, gyro.getRotation2dClockwise()));

        else if (state == DrivebaseState.ROBOT_RELATIVE)
            swerveModuleStates = kinematics.toSwerveModuleStates(speeds);

        // I think this does the same thing ):
        // TorqueSwerveModule2021.equalizedDriveRatio(swerveModuleStates,
        // DRIVE_MAX_TRANSLATIONAL_SPEED);
        SwerveDriveKinematics.desaturateWheelSpeeds(swerveModuleStates, DRIVE_MAX_TRANSLATIONAL_SPEED);

        frontLeft.setDesiredState(swerveModuleStates[2]);
        frontRight.setDesiredState(swerveModuleStates[1]);
        backLeft.setDesiredState(swerveModuleStates[0]);
        backRight.setDesiredState(swerveModuleStates[3]);

        odometry.update(gyro.getRotation2dClockwise(), // .times(-1) ?
                        frontLeft.getState(), frontRight.getState(), backLeft.getState(), backRight.getState());

        poseEstimator.update(gyro.getRotation2dClockwise().times(-1), frontLeft.getState(), frontRight.getState(),
                             backLeft.getState(), backRight.getState());
        // The order of these might be wrong

        // SmartDashboard.putNumber("Rot3", backLeft.getRotation().getDegrees());
    }

    public final void initAuto() {
        reset();
        state = DrivebaseState.ROBOT_RELATIVE;
    }

    @Override
    public final void updateAuto() {
        updateTeleop();
    }

    public final SwerveDriveKinematics getKinematics() { return kinematics; }

    public final TorqueSwerveOdometry getOdometry() { return odometry; }

    public final SwerveDrivePoseEstimator getPoseEstimator() { return poseEstimator; }

    public final TorqueNavXGyro getGyro() { return gyro; }

    public final void reset() { speeds = new ChassisSpeeds(0, 0, 0); }

    public static final synchronized Drivebase getInstance() {
        return instance == null ? instance = new Drivebase() : instance;
    }
}
