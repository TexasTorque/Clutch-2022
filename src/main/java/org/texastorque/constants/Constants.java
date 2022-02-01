package org.texastorque.constants;

public class Constants {
    // Conversions

    // Drivebase
    public static final double DRIVE_WHEEL_RADIUS_METERS = -1;
    public static final double DRIVE_MAX_SPEED_METERS = -1;
    public static final double DRIVE_MAX_ANGUAR_SPEED_RADIANS = -1;

    public static final double ROTATE_MANAGER_PID_P = 0;
    public static final double ROTATE_MANAGER_PID_I = 0;
    public static final double ROTATE_MANAGER_PID_D = 0;

    // Magazine
    public static final double MAGAZINE_GATE_SPEED = 0.5;
    public static final double MAGAZINE_BELT_SPEED = 0.5;

    // Intake
    public static final double INTAKE_ROTARY_SPEED = .7;

    // Shooter
    public static final double FLYWHEEL_Kv = -1;
    public static final double FLYWHEEL_Ka = -1;
    public static final double FLYWHEEL_Ks = -1;
    public static final double FLYWHEEL_Kp = -1;
    public static final double FLYWHEEL_Ki = -1;
    public static final double FLYWHEEL_Kd = -1;
    public static final double HOOD_MIN = 0;
    public static final double HOOD_MAX = 0;

    // Climber
    public static final double CLIMBER_SPEED = .5;

    // Turret
    public static final double TURRET_Ks = -1;
    public static final double TURRET_Kv = -1;
    public static final double TURRET_Ka = -1;
    public static final double TURRET_Kp = -1;
    public static final double TURRET_Ki = -1;
    public static final double TURRET_Kd = -1;


    public static final double FOOT_TO_METER = 0.3048;
    public static final double TOP_SPEED_FEET = 16.52;
    public static final double TOP_SPEED_METERS = TOP_SPEED_FEET * FOOT_TO_METER;
    public static final double TOP_ACCELERATION_METERS = 1; 
    public static final double MAX_ANGULAR_SPEED = 2 * Math.PI;
    public static final double MAX_ANGULAR_ACCELERATION = Math.PI;

}
