package org.texastorque.constants;

public class Ports {

    // Drivebase
    public static final int DRIVE_TRANS_RIGHT_FRONT = 1;    // CAN
    public static final int DRIVE_TRANS_RIGHT_BACK = 2;     // CAN
    public static final int DRIVE_TRANS_LEFT_BACK = 3;      // CAN
    public static final int DRIVE_TRANS_LEFT_FRONT = 4;     // CAN

    public static final int DRIVE_ROT_RIGHT_FRONT = 5;      // CAN
    public static final int DRIVE_ROT_RIGHT_BACK = 6;       // CAN
    public static final int DRIVE_ROT_LEFT_BACK = 7;        // CAN
    public static final int DRIVE_ROT_LEFT_FRONT = 8;       // CAN

    // Intake
    public static final int INTAKE_ROTARY = 9;              // CAN
    public static final int INTAKE_ROLLER = 10;             // CAN

    // Magazine
    public static final int MAGAZINE_BELT = 11;             // CAN
    public static final int MAGAZINE_GATE = 12;             // CAN

    // Shooter
    public static final int SHOOTER_FLYWHEEL_LEFT = 14;     // CAN
    public static final int SHOOTER_FLYWHEEL_RIGHT = 15;    // CAN
    public static final int SHOOTER_HOOD = 0;               // PWM
}