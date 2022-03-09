package org.texastorque.inputs;

import org.texastorque.constants.Constants;

public class Test {
    public static void main(String[] args) {

        System.out.println(2 * (60. / 1.) * (1 / (2 * Math.PI * Constants.DRIVE_WHEEL_RADIUS_METERS))
                * (1. / Constants.DRIVE_GEARING));
        System.out.println(2242.84300 * (1. / 60.) * (2 * Math.PI * Constants.DRIVE_WHEEL_RADIUS_METERS / 1.)
                * (Constants.DRIVE_GEARING / 1.));
    }
}
