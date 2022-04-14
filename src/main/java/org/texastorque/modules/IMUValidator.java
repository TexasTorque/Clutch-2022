package org.texastorque.modules;

// Validate the encoder movements of the swerve drive w/ imu readings to prevent over-movement in freespin
public class IMUValidator {

    private static double allowedMagnitudeDifference = 1. / 10;

    private IMUValidator() {
    }

    public static boolean calc(double worldXVelocity, double worldYVelocity, double recordedVelocity) {
        double worldVelocity = Math.sqrt(worldXVelocity * worldXVelocity + worldYVelocity * worldYVelocity);
        if (recordedVelocity * allowedMagnitudeDifference < worldVelocity) {
            System.out.println("IMU Validator failed check!");
            return false;
        }
        return true;
    }
}
