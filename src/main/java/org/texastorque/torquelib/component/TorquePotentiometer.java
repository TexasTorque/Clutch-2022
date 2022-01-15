package org.texastorque.torquelib.component;

import edu.wpi.first.wpilibj.AnalogInput;

/**
 * Create a new FRC potentiometer.
 *
 * @author TexasTorque
 */
public class TorquePotentiometer {

	private AnalogInput pot;

	private double maxValue;
	private double minValue;

	private double maxPosition;
	private double minPosition;

	/**
	 * Create a new potentiometer.
	 *
	 * @param port
	 *            Port of the potentiometer.
	 */
	public TorquePotentiometer(int port) {
		pot = new AnalogInput(port);
	}

	/**
	 * Set the range that the potentiometer receives input between.
	 *
	 * @param max
	 *            Maximum value.
	 * @param min
	 *            Minimum value.
	 */
	public void setInputRange(double max, double min) {
		maxValue = max;
		minValue = min;
	}

	/**
	 * Set the range that the potentiometer runs between.
	 *
	 * @param max
	 *            Maximum value.
	 * @param min
	 *            Minimum value.
	 */
	public void setPositionRange(double max, double min) {
		maxPosition = max;
		minPosition = min;
	}

	/**
	 * Get the fraction of the maximum turn.
	 *
	 * @return A value from 0 to 1.
	 */
	public double get() {
		return (getRaw() - minValue) / (maxValue - minValue);
	}

	/**
	 * Get the position of the potentiometer in relation to the minimum and
	 * maximum position. If you define min and max position in real units this
	 * will be in real units.
	 *
	 * @return The position.
	 */
	public double getPosition() {
		return get() * (maxPosition - minPosition) + minPosition;
	}

	/**
	 * Get the rate value of the potentiometer as a digital value between 0 and
	 * 4096.
	 *
	 * @return The value.
	 */
	public double getRaw() {
		return pot.getValue();
	}

	/**
	 * Get the voltage of the potentiometer between 0v and 5v.
	 *
	 * @return The voltage.
	 */
	public double getRawVoltage() {
		return pot.getVoltage();
	}
}
