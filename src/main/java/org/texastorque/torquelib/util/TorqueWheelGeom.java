package org.texastorque.torquelib.util;

/**
 * Simple utility class for wheel geometry and conversions.
 */
public class TorqueWheelGeom {
    private double diameter;
    private double gearRatio;

    public TorqueWheelGeom(double diameter, double gearRatio) {
        this.diameter = diameter;
        this.gearRatio = gearRatio;
    }

    public double getDiameter() {
        return diameter;
    }

    public double setDiameter(double diameter) {
        return this.diameter = diameter;
    }

    public double getRadius() {
        return diameter / 2.0;
    }

    public double setRadius(double radius) {
        return diameter = 2.0 * radius;
    }

    public double getCircumference() {
        return Math.PI * diameter;
    }

    public double metersToRotations(double meters) {
        return 2048. * meters * getCircumference() * gearRatio;
    }

    public double rotationsToMeters(double rotations) {
        return rotations / (2048. * getCircumference() * gearRatio);
    }
}
