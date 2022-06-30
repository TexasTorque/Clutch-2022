package org.texastorque.subsystems;

import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.Ports;
import org.texastorque.Subsystems;
import org.texastorque.torquelib.base.TorqueMode;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.base.TorqueSubsystemState;
import org.texastorque.torquelib.control.complex.TorqueTraversableSelection;
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
@SuppressWarnings("deprecation")
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

    private final TorqueSwerveModule2021 backLeft, backRight, frontLeft, frontRight;
    private SwerveModuleState[] swerveModuleStates; // This can be made better

    private DrivebaseState state = DrivebaseState.FIELD_RELATIVE;
    private ChassisSpeeds speeds = new ChassisSpeeds(0, 0, 0);

    private final TorqueNavXGyro gyro = TorqueNavXGyro.getInstance();

    private double translationalSpeedCoef, rotationalSpeedCoef;
    private final double SHOOTING_TRANSLATIONAL_SPEED_COEF = .4, SHOOTING_ROTATIONAL_SPEED_COEF = .5;

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
    public final void update(final TorqueMode mode)  {
        if (state == DrivebaseState.X_FACTOR)
            for (int i = 0; i < swerveModuleStates.length; i++) 
                swerveModuleStates[i] = new SwerveModuleState(0, 
                        new Rotation2d((i == 0 || i == 3) ? 135 : 45));
       
        else if (state == DrivebaseState.FIELD_RELATIVE)
            swerveModuleStates = kinematics.toSwerveModuleStates(
                    ChassisSpeeds.fromFieldRelativeSpeeds(
                            speeds.vxMetersPerSecond * (shooter.isShooting() 
                                    ? SHOOTING_TRANSLATIONAL_SPEED_COEF : translationalSpeedCoef), 
                            speeds.vyMetersPerSecond * (shooter.isShooting() 
                                    ? SHOOTING_TRANSLATIONAL_SPEED_COEF : translationalSpeedCoef),
                            speeds.omegaRadiansPerSecond * (shooter.isShooting() 
                                    ? SHOOTING_ROTATIONAL_SPEED_COEF : rotationalSpeedCoef), 
                            gyro.getRotation2dClockwise()
                    ));

        else if (state == DrivebaseState.ROBOT_RELATIVE)
            swerveModuleStates = kinematics.toSwerveModuleStates(speeds);

        // Literaly does the same thing LMFAO, my implementaiton is actually better 0:
        // TorqueSwerveModule2021.equalizedDriveRatio(swerveModuleStates, DRIVE_MAX_TRANSLATIONAL_SPEED);
        SwerveDriveKinematics.desaturateWheelSpeeds(swerveModuleStates, DRIVE_MAX_TRANSLATIONAL_SPEED);

        frontLeft.setDesiredState(swerveModuleStates[2]);
        frontRight.setDesiredState(swerveModuleStates[1]);
        backLeft.setDesiredState(swerveModuleStates[0]);
        backRight.setDesiredState(swerveModuleStates[3]);

        odometry.update(gyro.getRotation2dClockwise().times(-1),
                        frontLeft.getState(), frontRight.getState(), backLeft.getState(), backRight.getState());

        log();
    }

    public final SwerveDriveKinematics getKinematics() { return kinematics; }

    public final TorqueSwerveOdometry getOdometry() { return odometry; }

    public final TorqueNavXGyro getGyro() { return gyro; }

    public final void log() {
        SmartDashboard.putString("OdomPos", String.format("(%02.3f, %02.3f)", 
                odometry.getPoseMeters().getX(), odometry.getPoseMeters().getY()));

        SmartDashboard.putString("Speeds", String.format("(%02.3f, %02.3f, %02.3f)", 
                speeds.vxMetersPerSecond, speeds.vyMetersPerSecond, speeds.omegaRadiansPerSecond));
    }

    public final void reset() { speeds = new ChassisSpeeds(0, 0, 0); }

    public static final synchronized Drivebase getInstance() {
        return instance == null ? instance = new Drivebase() : instance;
    }
}
