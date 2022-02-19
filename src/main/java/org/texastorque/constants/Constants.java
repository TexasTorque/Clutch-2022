package org.texastorque.constants;

public class Constants {
    // Conversions
    public static final double FOOT_TO_METER = 0.3048;
    public static final double INCH_TO_FOOT = 1. / 12.;

    // Drivebase
    public static final double DRIVE_WHEEL_RADIUS_METERS = 1.788 * INCH_TO_FOOT * FOOT_TO_METER; // 1.788 is width with
                                                                                                 // wear
    public static final double DRIVE_MAX_SPEED_METERS = 5;
    public static final double DRIVE_MAX_ANGUAR_SPEED_RADIANS = 4 * Math.PI;

    public static final double DISTANCE_TO_CENTER_X = 10.875 * INCH_TO_FOOT * FOOT_TO_METER;
    public static final double DISTANCE_TO_CENTER_Y = 10.875 * INCH_TO_FOOT * FOOT_TO_METER;

    public static final double ROTATE_MANAGER_PID_P = .6;
    public static final double ROTATE_MANAGER_PID_I = 0;
    public static final double ROTATE_MANAGER_PID_D = 0;

    public static final double DRIVE_Ks = 0.37843;
    public static final double DRIVE_Kv = 1.5423;
    public static final double DRIVE_Ka = 1.5065;
    public static final double DRIVE_Kp = 0;

    // Magazine
    public static final double MAGAZINE_BELT_SPEED = 1.;

    // Intake
    public static final double INTAKE_ROTARY_SPEED = 1.;

    // Shooter
    public static final double FLYWHEEL_Kv = 0.15475; // Values are for rotations/sec
    public static final double FLYWHEEL_Ka = 0.042938;
    public static final double FLYWHEEL_Ks = 0.034329;
    public static final double FLYWHEEL_Kp = 0.0003;
    public static final double FLYWHEEL_Ki = 0.0000006;
    public static final double FLYWHEEL_Kd = 0;
    public static final double FLYWHEEL_Kf = 0.00022;
    public static final double FLYWHEEL_Iz = 100;
    public static final double HOOD_MIN = 0;
    public static final double HOOD_MAX = 50;
    public static final double SHOOTER_ERROR = 10;

    // Climber
    public static final double CLIMBER_SPEED = .5;
    public static final double CLIMBER_LEFT_LIMIT_HIGH = 330;
    public static final double CLIMBER_RIGHT_LIMIT_HIGH = -345; // Will change again
    public static final double CLIMBER_LEFT_LIMIT_LOW = 0;
    public static final double CLIMBER_RIGHT_LIMIT_LOW = 0;
    // 63:1 gear ratio
    // Turret
    public static final double TURRET_Ks = 0.19206;
    public static final double TURRET_Kv = 0.059555;
    public static final double TURRET_Ka = 0.0029152;
    public static final double TURRET_Kp = 0.42539;
    public static final double TURRET_Ki = 0;
    public static final double TURRET_Kd = 0;
    // public static final double TURRET_Kd = 0.033865;
    public static final double TURRET_RATIO = 192.708; // to 1

    // Information
    public static final double TOP_SPEED_FEET = 16.52;
    public static final double TOP_SPEED_METERS = TOP_SPEED_FEET * FOOT_TO_METER;
    public static final double TOP_ACCELERATION_METERS = 2;
    public static final double MAX_ANGULAR_SPEED = 4 * Math.PI;
    public static final double MAX_ANGULAR_ACCELERATION = 2 * Math.PI;

    // Path Planner
    public static final double PATH_PLANNER_X_P = 1;
    public static final double PATH_PLANNER_X_I = 0;
    public static final double PATH_PLANNER_X_D = 0;

    public static final double PATH_PLANNER_Y_I = 1;
    public static final double PATH_PLANNER_Y_P = 0;
    public static final double PATH_PLANNER_Y_D = 0;

    public static final double PATH_PLANNER_R_P = 6;
    public static final double PATH_PANNER_R_I = 0;
    public static final double PATH_PLANNER_R_D = 0;

    // Physical
    public static final double HEIGHT_OF_VISION_STRIP_METERS = 2.6416;
    public static final double HEIGHT_TO_LIMELIGHT_METERS = 31.9694 * INCH_TO_FOOT * FOOT_TO_METER;
    public static final double LIMELIGHT_ANGEL_DEG = 42.5;

}
