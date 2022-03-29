package org.texastorque.constants;

public class Ports {

    // Drivebase
    public static final int DRIVE_TRANS_RIGHT_FRONT = 1; // CAN
    public static final int DRIVE_TRANS_RIGHT_BACK = 2; // CAN
    public static final int DRIVE_TRANS_LEFT_BACK = 3; // CAN
    public static final int DRIVE_TRANS_LEFT_FRONT = 4; // CAN

    public static final int DRIVE_ROT_RIGHT_FRONT = 5; // CAN
    public static final int DRIVE_ROT_RIGHT_BACK = 6; // CAN
    public static final int DRIVE_ROT_LEFT_BACK = 7; // CAN
    public static final int DRIVE_ROT_LEFT_FRONT = 8; // CAN

    // Intake
    public static final int INTAKE_ROTARY = 9; // CAN
    public static final int INTAKE_ROLLER = 10; // CAN
    public static final int INTAKE_ROLLER_FOLLOWER = 19; // CAN
    public static final int ROTARY_LIMIT_SWITCH = 3; // DIO

    // Magazine
    public static final int MAGAZINE_BELT = 11; // CAN
    public static final int MAGAZINE_GATE = 12; // CAN

    // Shooter
    public static final int SHOOTER_HOOD = 13; // CAN
    public static final int SHOOTER_FLYWHEEL_LEFT = 14; // CAN
    public static final int SHOOTER_FLYWHEEL_RIGHT = 15; // CAN

    public static final int SHOOTER_HOOD_LEFT = 1; // PWM
    public static final int SHOOTER_HOOD_RIGHT = 2; // PWM

    // Climber
    public static final int CLIMBER_LEFT = 16; // CAN
    public static final int CLIMBER_RIGHT = 17; // CAN
    public static final int CLIMBER_LEFT_SERVO = 5; // PWM
    public static final int CLIMBER_RIGHT_SERVO = 6; // PWM
    public static final int CLIMBER_LIMIT_SWITCH_LEFT = 1; // DIO
    public static final int CLIMBER_LIMIT_SWITCH_RIGHT = 2; // DIO

    public static final int CLIMBER_LEFT_DIO = 1; // DIO
    public static final int CLIMBER_RIGHT_DIO = 2; // DIO

    // Turret
    public static final int TURRET = 18; // CAN

    // Lights
    public static final int LIGHTS = 3; // PWM
}
