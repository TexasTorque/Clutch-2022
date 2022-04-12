package org.texastorque.constants;

import edu.wpi.first.math.geometry.Translation2d;

public class Constants {
    // Conversions
    public static final double FOOT_TO_METER = 0.3048;
    public static final double INCH_TO_FOOT = 1. / 12.;

    // Drivebase
    public static final double DRIVE_WHEEL_RADIUS_METERS = 1.788 * INCH_TO_FOOT * FOOT_TO_METER; // 1.788 is width with
    // wear
    public static final double DRIVE_MAX_ANGUAR_SPEED_RADIANS_DRIVER = 2 * Math.PI;

    public static final double DISTANCE_TO_CENTER_X = 10.875 * INCH_TO_FOOT * FOOT_TO_METER;
    public static final double DISTANCE_TO_CENTER_Y = 10.875 * INCH_TO_FOOT * FOOT_TO_METER;

    public static final double ROTATE_MANAGER_PID_P = .6;
    public static final double ROTATE_MANAGER_PID_I = 0;
    public static final double ROTATE_MANAGER_PID_D = 0;

    public static final double DRIVE_ROT_Kp = 0.3;
    public static final double DRIVE_ROT_Ki = 0.;
    public static final double DRIVE_ROT_Kd = 0;
    public static final double DRIVE_ROT_Ks = 0;

    public static final double DRIVE_ROT_TOLERANCE = 1;

    public static final double DRIVE_Ks = 0.27024;
    public static final double DRIVE_Kv = 2.4076;
    public static final double DRIVE_Ka = 0.5153;
    public static final double DRIVE_Kp = 0.00048464;
    public static final double DRIVE_Ki = 0;
    public static final double DRIVE_Kd = 0;

    public static final double DRIVE_ALLOWED_ERROR = 0.1;
    public static final double DRIVE_MINIMUM_VELOCITY = 0.1;

    public static final double DRIVE_KIz = 0.2;
    public static final double DRIVE_GEARING = .1875; // amount of drive rotations per neo rotations

    // Magazine
    public static final double MAGAZINE_BELT_SPEED = 1.;

    // Intake
    public static final double INTAKE_ROLLER_SPEED = 1.0;

    public static final double INTAKE_ROTARY_MIN_SPEED = -.35;
    public static final double INTAKE_ROTARY_MAX_SPEED = .35;

    // Shooter
    public static final double FLYWHEEL_Ks = 0.37717;
    public static final double FLYWHEEL_Kv = 0.19542; // Values are for rotations/sec
    public static final double FLYWHEEL_Ka = 0.016159;
    public static final double FLYWHEEL_Kp = 0.0003800000122282654;
    public static final double FLYWHEEL_Ki = 0;
    public static final double FLYWHEEL_Kd = 0;
    public static final double FLYWHEEL_Kf = 0.000271599992320872843;
    public static final double FLYWHEEL_Iz = 150;
    public static final double FLYWHEEEL_MAX_SPEED = 3000;
    public static final double FLYWHEEL_MAX_ACCELERATION = 10000;
    public static final double HOOD_MIN = 0;
    public static final double HOOD_MAX = 40;
    public static final double HOOD_ERROR = 1;
    public static final double SHOOTER_ERROR = 45;

    public static final double HOOD_Kp = 0.1;
    public static final double HOOD_Ki = 0.001;
    public static final double HOOD_kd = 0;
    public static final double HOOD_Iz = .3;

    // Climber
    public static final double CLIMBER_SPEED = 1;
    public static final double CLIMBER_LEFT_LIMIT_HIGH = 182;
    public static final double CLIMBER_RIGHT_LIMIT_HIGH = -182;
    public static final double CLIMBER_LEFT_LIMIT_LOW = 0;
    public static final double CLIMBER_RIGHT_LIMIT_LOW = 0;
    public static final double CLIMBER_RIGHT_SERVO_ATTACHED = 0.5;
    public static final double CLIMBER_RIGHT_SERVO_DETACHED = 0.1;
    public static final double CLIMBER_LEFT_SERVO_ATTACHED = 0.5;
    public static final double CLIMBER_LEFT_SERVO_DETACHED = 0.9;
    public static final double CLIMBER_SLOW_FACTOR = .5;

    // 63:1 gear ratio
    // Turret
    public static final double TURRET_Ks = 0.14066;
    public static final double TURRET_Kv = 0.059555;
    public static final double TURRET_Ka = 0.0029152;
    public static final double TURRET_Kp = 0.24539;
    public static final double TURRET_Ki = 0;
    public static final double TURRET_Kd = 0;
    // public static final double TURRET_Kd = 0.033865;
    public static final double TURRET_RATIO = 128.4722; // to 1
    public static final double TURRET_CENTER_ROT = 0; // 30 degrees center
    public static final double TURRET_BACK_ROT = 180;
    public static final double TOLERANCE_DEGREES = .8;
    public static final double TURRET_MAX_ROTATION_LEFT = 93;
    public static final double TURRET_MAX_ROTATION_RIGHT = -93;

    // Information
    public static final double DRIVE_MAX_SPEED_METERS = 4;
    public static final double DRIVE_MAX_ACCELERATION_METERS = 2;
    public static final double MAX_ANGULAR_SPEED = 6 * Math.PI;
    public static final double MAX_ANGULAR_ACCELERATION = 6 * Math.PI;

    // Path Planner
    public static final double PATH_PLANNER_X_P = 1;
    public static final double PATH_PLANNER_X_I = 0;
    public static final double PATH_PLANNER_X_D = 0;

    public static final double PATH_PLANNER_Y_P = 1;
    public static final double PATH_PLANNER_Y_I = 0;
    public static final double PATH_PLANNER_Y_D = 0;

    public static final double PATH_PLANNER_R_P = 2;
    public static final double PATH_PANNER_R_I = 0;
    public static final double PATH_PLANNER_R_D = 0;

    // Physical
    public static final double HEIGHT_OF_VISION_STRIP_METERS = 2.6416;
    public static final double HEIGHT_TO_LIMELIGHT_METERS = 35 * INCH_TO_FOOT * FOOT_TO_METER;
    public static final double LIMELIGHT_ANGEL_DEG = 45;
    public static final Translation2d HUB_CENTER_POSITION = new Translation2d(8.2, 4.1);

    public static final double IDLE_SHOOTER_AMPS = 0;

}
