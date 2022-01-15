package org.texastorque.util;

public final class MathUtils {

	private MathUtils() {
	}

	public static double calcAreaTrapezoid(double base1, double base2, double height) {
		return 0.5 * (base1 + base2) * height;
	}

	public static double clamp(double value, double min, double max) {
		return Math.min(Math.max(min, value), max);
	}

	/**
	 * A signum, but 0 is positive.
	 * 
	 * @param val The value to signum.
	 * @return Positive if 0 or higher, negative otherwise.
	 */
	public static int nonZeroSignum(double val) {
		return (val >= 0) ? 1 : -1;
	}
}
