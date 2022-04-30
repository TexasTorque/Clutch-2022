package org.texastorque.subsystems;

import org.texastorque.constants.Ports;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.modules.TorqueSwerveModule2021;
import org.texastorque.torquelib.sensors.TorqueNavXGyro;
import org.texastorque.torquelib.util.TorqueSwerveOdometry;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.util.Units;

/**
 * The drivebase subsystem. Drives with 4 2021 swerve modules.
 * 
 * @author Jack Pittenger
 * @author Justus Languell
 */
public class Drivebase extends TorqueSubsystem {
    private static volatile Drivebase instance;

    public enum DrivebaseState {
        ROBOT_RELATIVE,
        FIELD_RELATIVE,
        X_FACTOR
    }

    private static final double DRIVE_GEARING = .1875; // Drive rotations per motor rotations
    private static final double DRIVE_WHEEL_RADIUS = Units.inchesToMeters(1.788);

    private static final double DISTANCE_TO_CENTER_X = Units.inchesToMeters(10.875);
    private static final double DISTANCE_TO_CENTER_Y = Units.inchesToMeters(10.875);

    private final Translation2d locationBackLeft = new Translation2d(DISTANCE_TO_CENTER_X, -DISTANCE_TO_CENTER_Y);
    private final Translation2d locationBackRight = new Translation2d(DISTANCE_TO_CENTER_X, DISTANCE_TO_CENTER_Y);
    private final Translation2d locationFrontLeft = new Translation2d(-DISTANCE_TO_CENTER_X, -DISTANCE_TO_CENTER_Y);
    private final Translation2d locationFrontRight = new Translation2d(-DISTANCE_TO_CENTER_X, DISTANCE_TO_CENTER_Y);

    private final SwerveDriveKinematics kinematics;
    private final TorqueSwerveOdometry odometry;

    private final TorqueSwerveModule2021 backLeft, backRight, frontLeft, frontRight;
    private SwerveModuleState[] swerveModuleStates; // This can be made better

    private DrivebaseState state = DrivebaseState.FIELD_RELATIVE;
    private ChassisSpeeds speeds = new ChassisSpeeds(0, 0, 0);

    private Drivebase() {
        backLeft = new TorqueSwerveModule2021(0, Ports.DRIVE_TRANS_LEFT_BACK, Ports.DRIVE_ROT_LEFT_BACK, 
                DRIVE_GEARING, DRIVE_WHEEL_RADIUS);
        backRight = new TorqueSwerveModule2021(1, Ports.DRIVE_TRANS_RIGHT_BACK, Ports.DRIVE_ROT_RIGHT_BACK,
                DRIVE_GEARING, DRIVE_WHEEL_RADIUS);
        frontLeft = new TorqueSwerveModule2021(2, Ports.DRIVE_TRANS_LEFT_FRONT, Ports.DRIVE_ROT_LEFT_FRONT,
                DRIVE_GEARING, DRIVE_WHEEL_RADIUS);
        frontRight = new TorqueSwerveModule2021(3, Ports.DRIVE_TRANS_RIGHT_FRONT, Ports.DRIVE_ROT_RIGHT_FRONT,
                DRIVE_GEARING, DRIVE_WHEEL_RADIUS);

        kinematics = new SwerveDriveKinematics(locationBackLeft, locationBackRight,
                locationFrontLeft, locationFrontRight);

        odometry = new TorqueSwerveOdometry(kinematics, TorqueNavXGyro.getInstance().getRotation2dClockwise());
    }

    public void setState(final DrivebaseState state) {
        this.state = state;
    }

    public DrivebaseState getState() {
        return state;
    }

    public void setSpeeds(final ChassisSpeeds speeds) {
        this.speeds = speeds;
    }

    @Override
    public void initTeleop() {
        speeds = new ChassisSpeeds(0, 0, 0);
    }

    @Override
    public void updateTeleop() {
        if (state == DrivebaseState.X_FACTOR) {
            swerveModuleStates[0].angle = Rotation2d.fromDegrees(135);
            swerveModuleStates[1].angle = Rotation2d.fromDegrees(45);
            swerveModuleStates[2].angle = Rotation2d.fromDegrees(45);
            swerveModuleStates[3].angle = Rotation2d.fromDegrees(135);
        }

        else if (state == DrivebaseState.FIELD_RELATIVE)
            swerveModuleStates = kinematics.toSwerveModuleStates(ChassisSpeeds.fromFieldRelativeSpeeds(
                            speeds.vxMetersPerSecond, speeds.vyMetersPerSecond, speeds.omegaRadiansPerSecond,
                            TorqueNavXGyro.getInstance().getRotation2dClockwise()));
        
        else if (state == DrivebaseState.ROBOT_RELATIVE)
            swerveModuleStates = kinematics.toSwerveModuleStates(speeds);

        frontLeft.setDesiredState(swerveModuleStates[0]);
        frontRight.setDesiredState(swerveModuleStates[1]);
        backLeft.setDesiredState(swerveModuleStates[2]);
        backRight.setDesiredState(swerveModuleStates[3]);
    }

    @Override
    public void initAuto() {
        
    }

    @Override
    public void updateAuto() {
        
    }

    public SwerveDriveKinematics getKinematics() {
        return kinematics;
    }

    public TorqueSwerveOdometry getOdometry() {
        return odometry;
    }

    public static synchronized Drivebase getInstance() {
        return instance == null ? instance = new Drivebase() : instance;
    }
}
