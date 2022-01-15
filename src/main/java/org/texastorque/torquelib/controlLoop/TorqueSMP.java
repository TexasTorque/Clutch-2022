package org.texastorque.torquelib.controlLoop;

import static java.lang.Math.*;

/**
 * Created by Texas Torque (Matthew Webb)
 */
public class TorqueSMP {

	// inputs
	private double maxV, maxA;

	// variables
	private double currentTime;

	// constants
	private double t1, t2, t3;
	private double c, k;
	private double sigma;

	/**
	 * Create a new spline/sinusoidal motion profile. Units do not matter as
	 * long as they are constant.
	 *
	 * @param mV
	 *            Maximum velocity
	 * @param mA
	 *            Maximum acceleration
	 */
	public TorqueSMP(double mV, double mA) {
		maxV = mV;
		maxA = mA;
		currentTime = 0.0;
	}

	private double func(double x) {
		return (sin(k * x - PI / 2.0) + 1.0) / c;
	}

	/**
	 * Generate the profile based a given distance.
	 *
	 * @param distance
	 *            Distance
	 */
	public void generate(double distance) {
		t1 = PI * maxV / (2.0 * maxA);
		c = 2.0 / maxV;
		k = PI / t1;

		sigma = -((t1 * maxV) / (2.0 * PI)) * cos((PI / t1) - (PI / 2.0)) + (1.0 / c) * t1;

		t2 = sigma - t1;

		if (t2 < 0) {

		}

		t3 = t2 + t1;
	}

	/**
	 * Get the velocity of the profile at a specific time.
	 *
	 * @param dt Change in time
	 * @return Velocity
	 */
	public double getVelocity(double dt) {
		currentTime += dt;
		if (currentTime < t1) {
			return func(currentTime);
		} else if (currentTime < t2) {
			return maxV;
		} else if (currentTime < t3) {
			return -func(currentTime - t2) + maxV;
		}
		return 0.0;
	}

	/**
	 * Get the time the profile will take.
	 *
	 * @return Time
	 */
	public double getMaxTime() {
		return t3;
	}
}
