// to do all vector maths

package org.texastorque.util;

public class VectorUtils {

    private static volatile double magX;
    private static volatile double magY;
    private static volatile double bearing;
    private static volatile double mag;

    public static double vectorAddition2DBearing(double magX1, double magY1, double magX2, double magY2){
        magX = magX1 + magX2;
        magY = magY1 + magY2;
        bearing = toBearing(Math.toDegrees(Math.atan2(magY, magX)));
        return bearing;
    } // vector Addition 2D return bearing of final vector

    public static double vectorAddition2DMagnitude(double magX1, double magY1, double magX2, double magY2){
        magX = magX1 + magX2;
        magY = magY1 + magY2;
        mag = Math.hypot(magX, magY);
        return mag;
    } // vector addition 2d return magnitude of final vector

    //public static double gyroVectorAdd(double offset, )

    public double getMagX(){
        return magX;
    } // return magnitude

    public double getMagY(){
        return magY;
    } // return magnitude

    public double getMag(){
        return mag;
    } // return magnitude

    public double getBearing(){
        return bearing;
    } // return bearing

    public static double toBearing(double angle) { // came from input with some modifications
        double bearing = 0;
        bearing = 90 - angle;
        if (bearing < 0){
            bearing = 360 + bearing;
        } // change them all to positive bearings for ease of use
        if (bearing > 180) {
            bearing = bearing - 180;
        } // change so that it goes to 180 on either side
        return bearing;
    } // change value to a bearing
} // VectorUtils
