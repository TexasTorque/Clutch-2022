package org.texastorque.torquelib.util;

import edu.wpi.first.wpilibj.Timer;

public class TorqueMathUtil {

	/**
	 * Return contrained value n between a and -a
	 * 
	 * @param n Value to be constrained
	 * @param a Value to constrain by
	 * @return The constrained value of n
	 */
	public static double constrain(double n, double a) {
        return Math.max(Math.min(n, a), -a);
	}

	/**
	 * Return contrained value n between a and b
	 * 
	 * @param n Value to be constrained
	 * @param a Value to constrain the value over, the minimum value
	 * @param b Value to constrain the value under, the maximum value
	 * @return The constrained value of n
	 */
	public static double constrain(double n, double a, double b) {
		return Math.max(Math.min(n, b), a);
	}


	/**
	 * Return contrained value n between a and -a
	 * 
	 * @deprecated
	 * 
	 * @param n Value to be constrained
	 * @param a Value to constrain by
	 * @return The constrained value of n
	 */
	@Deprecated
	public static double absConstrain(double n, double a) {
        return Math.max(Math.min(n, a), -a);
	}
	
	/**
	 * Return contrained value n between a and b
	 * 
	 * @deprecated 
	 * 
	 * @param n Value to be constrained
	 * @param a Value to constrain the value over, the minimum value
	 * @param b Value to constrain the value under, the maximum value
	 * @return The constrained value of n
	 */
	@Deprecated
	public static double biConstrain(double n, double a, double b) {
        return Math.max(Math.min(n, b), a);
    }

	public static double arrayClosest(double[] values, double value) {
		double closest = 0.0;
		for (int i = 0; i < values.length; i++) {
			if (Math.abs(values[i] - value) < Math.abs(closest - value)) {
				closest = value;
			}
		}
		return closest;
	}

	public static boolean near(double number, double value, double howClose) {
		return Math.abs(number - value) < howClose;
	}

	public static double addSign(double value, double add) {
		if (value < 0) {
			return value - add;
		} else {
			return value + add;
		}
	}
	
	public static void delay(double delay) {
		double startTime = Timer.getFPGATimestamp();
		while(startTime + delay >= Timer.getFPGATimestamp()) {
			if(Timer.getFPGATimestamp() - startTime > 10) {
				break;
			}
		}
	} //returns true once delayed
}
