package org.texastorque.torquelib.util;

import edu.wpi.first.wpilibj.Joystick;

/**
 * @author TexasTorque 2021
 * 
 * https://imgur.com/gallery/YPm3x5Z
 */
public class TorqueLogiPro extends Joystick {
    protected final int port;
    private double deadband;
    private static final double DEADBAND_DEF = .001;

    public TorqueLogiPro(int port) {
        super(port);
        this.port = port;
        deadband = DEADBAND_DEF;
    }

    public TorqueLogiPro(int port, double deadband) {
        super(port);
        this.port = port;
        this.deadband = deadband;
    }

    public boolean getTrigger() {
        return getRawButton(1);
    }

    private double deadbanded(double value) {
        return (value < deadband && value > -deadband) ? 0 : value;
    }

    public boolean getButtonByIndex(int index) {
        return getRawButton(index);
    }

    public PovState getPovState() { 
        switch (getPOV()) {
            case -1: return PovState.CENTER;
            case 0: return PovState.NORTH;
            case 45: return PovState.NORTH_EAST;
            case 90: return PovState.EAST;
            case 135: return PovState.SOUTH_EAST;
            case 180: return PovState.SOUTH;
            case 225: return PovState.SOUTH_WEST;
            case 270: return PovState.WEST;
            case 315: return PovState.NORTH_WEST;
            default: return PovState.CENTER;
        }
    }

    public boolean atPovState(PovState state) {
        return getPovState() == state;
    }

    public boolean atPovState(int angle) {
        return getPOV() == angle;
    }

    public double getRoll() { 
        return deadbanded(getX());
    }

    public double getPitch() { 
        return deadbanded(-getY());
    }

    public double getYaw() { 
        return deadbanded(getZ());
    }

    @Override
    public double getThrottle() {
        return (-super.getThrottle() + 1) / 2;
    }

    public static enum PovState {
        CENTER("CENTER"),
        NORTH("NORTH"), NORTH_EAST("NORTH EAST"), EAST("EAST"), SOUTH_EAST("SOUTH EAST"), 
        SOUTH("SOUTH"), SOUTH_WEST("SOUTH WEST"), WEST("WEST"), NORTH_WEST("SOUTH WEST"); 

        private String value;

        PovState(String value) {
            this.value = value;
        }

        public String asString() { 
            return value; 
        }
    }
}
