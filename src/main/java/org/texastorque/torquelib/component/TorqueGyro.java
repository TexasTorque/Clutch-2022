package org.texastorque.torquelib.component;

import edu.wpi.first.hal.AccumulatorResult;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Timer;

/**
 * Am going to deprecate this to make a new TorqueGyro with the AHRS or wtv.
 * 
 * Create a new gyro.
 *
 * @author TexasTorque
 */
public class TorqueGyro {

	// Analog Input Parameters
	static final int kOversampleBits = 10;
	static final int kAverageBits = 0;
	static final double kSamplesPerSecond = 50.0;
	static final double kCalibrationSampleTime = 5.0;

	// Volts to rate of change conversion
	double m_voltsPerDegreePerSecond;
	static final double kDefaultVoltsPerDegreePerSecond = 0.007;

	// Gyro 1
	double m1_offset;
	int m1_center;
	boolean m1_channelAllocated = false;
	private AnalogInput m1_analog;
	AccumulatorResult result1;

	// Gyro 2
	double m2_offset;
	int m2_center;
	boolean m2_channelAllocated = false;
	private AnalogInput m2_analog;
	AccumulatorResult result2;

	/**
	 * Create a new double gyro.
	 *
	 * @param port1
	 *            Port for the first gyroscope.
	 * @param port2
	 *            Port for the second, upside down gyroscope.
	 */
	public TorqueGyro(int port1, int port2) {
		m1_analog = new AnalogInput(port1);
		m1_channelAllocated = true;

		m2_analog = new AnalogInput(port2);
		m2_channelAllocated = true;

		initGyro();
	}

	/**
	 * Initialize the gyroscope.
	 */
	private void initGyro() {
		m_voltsPerDegreePerSecond = kDefaultVoltsPerDegreePerSecond;

		// Gyro 1
		result1 = new AccumulatorResult();

		m1_analog.setAverageBits(kAverageBits);
		m1_analog.setOversampleBits(kOversampleBits);
		double sampleRate = kSamplesPerSecond * (1 << (kAverageBits + kOversampleBits));
		AnalogInput.setGlobalSampleRate(sampleRate);

		// Gyro 2
		result2 = new AccumulatorResult();

		m2_analog.setAverageBits(kAverageBits);
		m2_analog.setOversampleBits(kOversampleBits);
		sampleRate = kSamplesPerSecond * (1 << (kAverageBits + kOversampleBits));
		AnalogInput.setGlobalSampleRate(sampleRate);

		Timer.delay(1.0);

		// Calibrate
		m1_analog.initAccumulator();
		m1_analog.resetAccumulator();
		m2_analog.initAccumulator();
		m2_analog.resetAccumulator();

		Timer.delay(kCalibrationSampleTime);

		// Gyro 1
		m1_analog.getAccumulatorOutput(result1);

		m1_center = (int) ((double) result1.value / (double) result1.count + .5);

		m1_offset = ((double) result1.value / (double) result1.count) - m1_center;

		m1_analog.setAccumulatorCenter(m1_center);
		m1_analog.resetAccumulator();

		// Gyro 2
		m2_analog.getAccumulatorOutput(result1);

		m2_center = (int) ((double) result1.value / (double) result1.count + .5);

		m2_offset = ((double) result1.value / (double) result1.count) - m1_center;

		m2_analog.setAccumulatorCenter(m1_center);
		m2_analog.resetAccumulator();

		setDeadband(0.1);
	}

	/**
	 * Free resources taken up by the Gyros
	 */
	public void free() {
		if (m1_analog != null && m1_channelAllocated) {
			m1_analog.close();
		}
		m1_analog = null;

		if (m2_analog != null && m2_channelAllocated) {
			m2_analog.close();
		}
		m2_analog = null;
	}

	/**
	 * Reset the accumulators to set the current angle as 0.0.
	 */
	public void reset() {
		if (m1_analog != null) {
			m1_analog.resetAccumulator();
		}
		if (m2_analog != null) {
			m2_analog.resetAccumulator();
		}
	}

	/**
	 * Set the deadband of the gyros. Any rate of change smaller than the
	 * deadband will be treated as sitting still. A large deadband will reduce
	 * drift but decrease accuracy. The default value is 0.1 degrees per second.
	 *
	 * @param degreesPerSecond
	 *            The deadband to be set in degrees/second.
	 */
	public void setDeadband(double degreesPerSecond) {
		double volts = degreesPerSecond * m_voltsPerDegreePerSecond;

		int deadband1 = (int) (volts * 1e9 / m1_analog.getLSBWeight() * (1 << m1_analog.getOversampleBits()));
		m1_analog.setAccumulatorDeadband(deadband1);

		int deadband2 = (int) (volts * 1e9 / m2_analog.getLSBWeight() * (1 << m2_analog.getOversampleBits()));
		m2_analog.setAccumulatorDeadband(deadband2);
	}

	/**
	 * Set the volts to angular rate of change scale factor of the gyros.
	 *
	 * @param voltsPerDegreePerSecond
	 *            The Sensitivity scalar.
	 */
	public void setSensitivity(double voltsPerDegreePerSecond) {
		m_voltsPerDegreePerSecond = voltsPerDegreePerSecond;
	}

	/**
	 * Get the angle of the gyro.
	 *
	 * @return The current angular postion of the robot in degrees.
	 */
	public double getAngle() {
		if (m1_analog == null || m2_analog == null) {
			return 0.0;
		} else {
			m1_analog.getAccumulatorOutput(result1);

			long value1 = result1.value - (long) (result1.count * m1_offset);

			double scaledValue1 = value1 * 1e-9 * m1_analog.getLSBWeight() * (1 << m1_analog.getAverageBits())
					/ (AnalogInput.getGlobalSampleRate() * m_voltsPerDegreePerSecond);

			long value2 = result2.value - (long) (result2.count * m2_offset);

			double scaledValue2 = -1 * value2 * 1e-9 * m2_analog.getLSBWeight() * (1 << m2_analog.getAverageBits())
					/ (AnalogInput.getGlobalSampleRate() * m_voltsPerDegreePerSecond);

			return (scaledValue1 + scaledValue2);
		}
	}

	/**
	 * Get the angular rate of change of the gyro.
	 *
	 * @return The angular rate of change of the robot in degrees/second.
	 */
	public double getRate() {
		if (m1_analog == null || m2_analog == null) {
			return 0.0;
		} else {
			double rate1 = (m1_analog.getAverageValue() - (m1_center + m1_offset)) * 1e-9 * m1_analog.getLSBWeight()
					/ ((1 << m1_analog.getOversampleBits()) * m_voltsPerDegreePerSecond);

			double rate2 = -1 * (m2_analog.getAverageValue() - (m2_center + m2_offset)) * 1e-9
					* m2_analog.getLSBWeight() / ((1 << m2_analog.getOversampleBits()) * m_voltsPerDegreePerSecond);

			return (rate1 + rate2) / 2;
		}
	}
}
