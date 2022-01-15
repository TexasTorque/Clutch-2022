package org.texastorque.torquelib.component;

import edu.wpi.first.wpilibj.AnalogTrigger;
import edu.wpi.first.wpilibj.Counter;
import edu.wpi.first.wpilibj.CounterBase;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.SensorUtil;

/**
 * Class for a FRC counter.
 *
 * @author TexasTorque
 */
public class TorqueCounter extends Counter {

	private double averageRate;
	private double acceleration;
	private double previousTime;
	private double previousPosition;
	private double previousRate;

	/**
	 * Create a new counter.
	 *
	 * @param port
	 *            Which port the counter is on.
	 */
	public TorqueCounter(int port) {
		super(SensorUtil.kPwmChannels);
	}

	/**
	 * Create a new counter with a specific digital input source.
	 *
	 * @param source
	 *            The digital input source.
	 */
	public TorqueCounter(DigitalInput source) {
		super(source);
	}

	/**
	 * Create a new counter with specific parameters.
	 *
	 * @param upPort
	 *            Port connected to the up counter.
	 * @param downPort
	 *            Port connected to the down counter.
	 * @param reverse
	 *            Whether or not the counter is reversed.
	 * @param encodingype
	 *            What type encoding the counter is using.
	 */
	public TorqueCounter(int upPort, int downPort, boolean reverse, CounterBase.EncodingType encodingype) {
		super(encodingype, new DigitalInput(upPort), new DigitalInput(downPort), reverse);

	}

	/**
	 * Create a new counter with an analog trigger.
	 *
	 * @param trigger
	 *            The analog trigger.
	 */
	public TorqueCounter(AnalogTrigger trigger) {
		super(trigger);
	}

	/**
	 * Calculate the values for the counter.
	 */
	public void calc() {
		double currentTime = Timer.getFPGATimestamp();
		double currentPosition = super.get();

		averageRate = (currentPosition - previousPosition) / (currentTime - previousTime);
		acceleration = (averageRate - previousRate) / (currentTime - previousTime);

		previousTime = currentTime;
		previousPosition = currentPosition;
		previousRate = averageRate;
	}

	/**
	 * Get the average rate at which counter position changes over time. This
	 * rate is calculated in the dx/dt method rather than 1 / period method.
	 *
	 * @return The rate.
	 */
	public double getAverageRate() {
		return averageRate;
	}

	/**
	 * Get average rate at which rate changes over time.
	 *
	 * @return The rate.
	 */
	public double getAcceleration() {
		return acceleration;
	}
}
