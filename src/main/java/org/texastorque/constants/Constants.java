package org.texastorque.constants;

public class Constants {
    // Conversions
    public static final double FOOT_TO_METER = 0.3048;
    public static final double INCH_TO_FOOT = 1. / 12.;

    // Drivebase
    public static final double DRIVE_WHEEL_RADIUS_METERS = 2 * INCH_TO_FOOT * FOOT_TO_METER; // 1.788 is width with
                                                                                             // wear
    public static final double DRIVE_MAX_ANGUAR_SPEED_RADIANS_DRIVER = 2 * Math.PI;

    public static final double DISTANCE_TO_CENTER_X = 10.875 * INCH_TO_FOOT * FOOT_TO_METER;
    public static final double DISTANCE_TO_CENTER_Y = 10.875 * INCH_TO_FOOT * FOOT_TO_METER;

    public static final double ROTATE_MANAGER_PID_P = .6;
    public static final double ROTATE_MANAGER_PID_I = 0;
    public static final double ROTATE_MANAGER_PID_D = 0;

    public static final double DRIVE_ROT_RIGHT_FRONT_Kp = 0.008;
    public static final double DRIVE_ROT_RIGHT_FRONT_Ki = 0.;
    public static final double DRIVE_ROT_RIGHT_FRONT_Kd = 0.;
    public static final double DRIVE_ROT_RIGHT_FRONT_Ks = 0.03;

    public static final double DRIVE_ROT_LEFT_FRONT_Kp = 0.008;
    public static final double DRIVE_ROT_LEFT_FRONT_Ki = 0.;
    public static final double DRIVE_ROT_LEFT_FRONT_Kd = 0.;
    public static final double DRIVE_ROT_LEFT_FRONT_Ks = 0.03;

    public static final double DRIVE_ROT_RIGHT_BACK_Kp = 0.008;
    public static final double DRIVE_ROT_RIGHT_BACK_Ki = 0;
    public static final double DRIVE_ROT_RIGHT_BACK_Kd = 0.;
    public static final double DRIVE_ROT_RIGHT_BACK_Ks = 0.03;

    public static final double DRIVE_ROT_LEFT_BACK_Kp = 0.008;
    public static final double DRIVE_ROT_LEFT_BACK_Ki = 0;
    public static final double DRIVE_ROT_LEFT_BACK_Kd = 0;
    public static final double DRIVE_ROT_LEFT_BACK_Ks = 0.03;

    public static final double DRIVE_ROT_TOLERANCE = 1;

    public static final double DRIVE_LEFT_Ks = 0.3192;
    public static final double DRIVE_LEFT_Kv = 2.1904;
    public static final double DRIVE_LEFT_Ka = 0.17902;
    public static final double DRIVE_LEFT_Kp = 1.5962E-05;
    public static final double DRIVE_LEFT_Ki = 0;
    public static final double DRIVE_LEFT_Kd = 0;

    public static final double DRIVE_RIGHT_Ks = 0.24187;
    public static final double DRIVE_RIGHT_Kv = 2.1974;
    public static final double DRIVE_RIGHT_Ka = 0.31871;
    public static final double DRIVE_RIGHT_Kp = 0.00078887;
    public static final double DRIVE_RIGHT_Ki = 0;
    public static final double DRIVE_RIGHT_Kd = 0;

    public static final double DRIVE_ALLOWED_ERROR = 0.1;
    public static final double DRIVE_MINIMUM_VELOCITY = 0.1;

    public static final double DRIVE_KIz = 0.2;
    public static final double DRIVE_GEARING = .1875; // amount of drive rotations per neo rotations

    // Magazine
    public static final double MAGAZINE_BELT_SPEED = 1.;

    // Intake
    public static final double INTAKE_ROLLER_SPEED = .75;

    public static final double INTAKE_ROTARY_MIN_SPEED = -.25;
    public static final double INTAKE_ROTARY_MAX_SPEED = .25;

    // Shooter
    public static final double FLYWHEEL_Kv = 0.15475; // Values are for rotations/sec
    public static final double FLYWHEEL_Ka = 0.042938;
    public static final double FLYWHEEL_Ks = 0.034329;
    public static final double FLYWHEEL_Kp = 0.0003;
    public static final double FLYWHEEL_Ki = 0.0000011;
    public static final double FLYWHEEL_Kd = 0;
    public static final double FLYWHEEL_Kf = 0.00024;
    public static final double FLYWHEEL_Iz = 150;
    public static final double HOOD_MIN = 0;
    public static final double HOOD_MAX = 50;
    public static final double SHOOTER_ERROR = 20;
    public static final double HOOD_ERROR = 1;

    // Climber
    public static final double CLIMBER_SPEED = 1;
    public static final double CLIMBER_LEFT_LIMIT_HIGH = 338;
    public static final double CLIMBER_RIGHT_LIMIT_HIGH = -327;
    public static final double CLIMBER_LEFT_LIMIT_LOW = 0;
    public static final double CLIMBER_RIGHT_LIMIT_LOW = 0;
    // 63:1 gear ratio
    // Turret
    public static final double TURRET_Ks = 0.19206;
    public static final double TURRET_Kv = 0.059555;
    public static final double TURRET_Ka = 0.0029152;
    public static final double TURRET_Kp = 0.20539;
    public static final double TURRET_Ki = 0;
    public static final double TURRET_Kd = 0;
    // public static final double TURRET_Kd = 0.033865;
    public static final double TURRET_RATIO = 128.4722; // to 1
    public static final double TURRET_CENTER_ROT = 0; // 30 degrees center
    public static final double TURRET_BACK_ROT = -180;
    public static final double TOLERANCE_DEGREES = 1.3;

    // Information
    public static final double DRIVE_MAX_SPEED_METERS = 5;
    public static final double DRIVE_MAX_ACCELERATION_METERS = 2.5;
    public static final double MAX_ANGULAR_SPEED = 3 * Math.PI;
    public static final double MAX_ANGULAR_ACCELERATION = 2 * Math.PI;

    // Path Planner
    public static final double PATH_PLANNER_X_P = 1;
    public static final double PATH_PLANNER_X_I = 0;
    public static final double PATH_PLANNER_X_D = 0;

    public static final double PATH_PLANNER_Y_P = 1;
    public static final double PATH_PLANNER_Y_I = 0;
    public static final double PATH_PLANNER_Y_D = 0;

    public static final double PATH_PLANNER_R_P = 3;
    public static final double PATH_PANNER_R_I = 0;
    public static final double PATH_PLANNER_R_D = 0;

    // Physical`
    public static final double HEIGHT_OF_VISION_STRIP_METERS = 2.6416;
    public static final double HEIGHT_TO_LIMELIGHT_METERS = 35 * INCH_TO_FOOT * FOOT_TO_METER;
    public static final double LIMELIGHT_ANGEL_DEG = 45;

}
