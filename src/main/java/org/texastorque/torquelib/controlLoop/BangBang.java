package org.texastorque.torquelib.controlLoop;

import edu.wpi.first.wpilibj.Timer;

/**
 * A controller that is either on or off depending on if the setpoint is
 * reached.
 *
 * @author TexasTorque
 */
public class BangBang extends ControlLoop {

	private final boolean limited;
	private final double resetTime;
	private final double firstOutput;
	private final double overshoot;

	private boolean reset;
	private double lastTime;

	/**
	 * Create a new BangBang controller (will always run when below setpoint,
	 * and will not run when above setpoint).
	 */
	public BangBang() {
		this(0.0, 1.0);
	}

	/**
	 * Create a new BangBang controller with a limited output for a specific
	 * time interval. <code>firstOut</code> will run for <code>time</code>
	 * seconds, then full output will be returned until controller is reset.
	 * 
	 * @param time
	 *            The time the controller should wait until full output is sent.
	 * @param firstOut
	 *            The output sent until the time interval has ended.
	 */
	public BangBang(double time, double firstOut) {
		this(time, firstOut, 0.0);
	}

	/**
	 * Create a new BangBang controller with a limited output for a specific
	 * time interval.
	 * 
	 * @param time
	 *            The time the controller should wait until full output is sent.
	 * @param firstOut
	 *            The output sent until the time interval has ended.
	 * @param over
	 *            How far past the setpoint BangBang will overshoot. This is
	 *            usually half the amplitude of the oscillation, or how far the
	 *            value drops before BangBang kicks in again.
	 */
	public BangBang(double time, double firstOut, double over) {
		limited = true;
		resetTime = time;
		firstOutput = firstOut;
		overshoot = over;
	}

	/**
	 * Reset the time interval.
	 */
	public void reset() {
		reset = true;
	}

	/**
	 * Calculate output based off of the current sensor value.
	 *
	 * @param current
	 *            the current sensor feedback.
	 * @return Motor ouput to the system.
	 */
	public double calculate(double current) {
		if (reset) {
			reset = false;
			lastTime = Timer.getFPGATimestamp();
		}

		if (current < setPoint) {
			if (!limited || lastTime - Timer.getFPGATimestamp() > resetTime) {
				return 1.0;
			}
			return firstOutput;
		} else {
			return 0.0;
		}
	}
}
