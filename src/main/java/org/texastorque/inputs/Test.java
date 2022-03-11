package org.texastorque.inputs;

import org.texastorque.constants.Constants;

public class Test {
    private static final double turnCountPerRevolution = 4096. * 2;

    public static void main(String[] args) {

        double current = 45;
        double reqDegrees = -45;
        int desired = (int) Math.round(reqDegrees * turnCountPerRevolution / 360.);
        double outputPosition = Math.IEEEremainder(desired - current, turnCountPerRevolution / 2) + current;
        double dist = Math.abs(Math.IEEEremainder(desired - current, turnCountPerRevolution));
        System.out.println("Reverse direction? " + dist);
        System.out.println("Output position (encoder): " + outputPosition);
    }
}
