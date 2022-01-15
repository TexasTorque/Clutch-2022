package org.texastorque.torquelib.controlLoop;

import org.texastorque.torquelib.util.TorqueMathUtil;

import edu.wpi.first.wpilibj.Timer;

/**
 * A PID implementation.
 *
 * @author TexasTorque
 */
public class TorquePID extends ControlLoop {

	// Settings
	private double kP;
	private double kI;
	private double kD;
	private double epsilon;
	private double errorSum;
	private double maxOutput;
	private boolean speedController;

	private boolean initialLimited;
	private double initialLimitedTime;
	private double initialLimitedOutput;

	// Variables
	private boolean firstCycle;
	private double setpoint;
	private double error;
	private double prevError;
	private double output;
	private double prevOutput;
	private double dt;
	private double lastTime;

	private double lastLimitedTime;

	/**
	 * Create a new PID with all constants 0.
	 */
	public TorquePID() {
		this(0, 0, 0);
	}

	/**
	 * Create a new PID.
	 *
	 * @param p
	 *            The proportionality constant.
	 * @param i
	 *            The integral constant.
	 * @param d
	 *            The derivative constant.
	 */
	public TorquePID(double p, double i, double d) {
		super();
		kP = p;
		kI = i;
		kD = d;
		epsilon = 0.0;
		errorSum = 0.0;
		firstCycle = true;
		maxOutput = 1.0;

		initialLimited = false;
	}

	/**
	 * Create a new PID.
	 *
	 * @param p
	 *            The proportionality constant.
	 * @param i
	 *            The integral constant.
	 * @param d
	 *            The derivative constant.
	 * @param limitedTime
	 *            How long PID should output the limitedOutput.
	 * @param limitedOutput
	 *            The output PID will send for a limited time;
	 */
	public TorquePID(double p, double i, double d, double limitedTime, double limitedOutput) {
		super();
		kP = p;
		kI = i;
		kD = d;
		epsilon = 0.0;
		errorSum = 0.0;
		firstCycle = true;
		maxOutput = 1.0;

		initialLimited = true;
		initialLimitedTime = limitedTime;
		initialLimitedOutput = limitedOutput;
	}

	/**
	 * Change the PID constants.
	 *
	 * @param p
	 *            The proportionality constant.
	 * @param i
	 *            The integral constant.
	 * @param d
	 *            The derivative constant.
	 */
	public void setPIDGains(double p, double i, double d) {
		kP = p;
		kI = i;
		kD = d;
	}

	/*
	 * Change this controller to a speed control, which integrates output and
	 * never goes negative.
	 * 
	 * @param speed True for speed/velocity control, false for position control.
	 */
	public void setControllingSpeed(boolean speedControl) {
		speedController = speedControl;
	}

	/*
	 * Set the setpoint of the PID loop.
	 */
	@Override
	public void setSetpoint(double set) {
		setpoint = set;
	}

	/**
	 * Set the epsilon value.
	 *
	 * @param e
	 *            The new epsilon value.
	 */
	public void setEpsilon(double e) {
		epsilon = e;
	}

	/**
	 * Set the limit of the output.
	 *
	 * @param max
	 *            The maximum value that the value can be.
	 */
	public void setMaxOutput(double max) {
		if (max < 0.0) {
			maxOutput = 0.0;
		} else if (max > 1.0) {
			maxOutput = 1.0;
		} else {
			maxOutput = max;
		}
	}

	/**
	 * Reset the PID controller.
	 */
	public void reset() {
		firstCycle = true;
	}

	/**
	 * Calculate output based off of the current sensor value.
	 *
	 * @param currentValue
	 *            the current sensor feedback.
	 * @return Motor output to the system.
	 */
	public double calculate(double currentValue) {
		if (firstCycle) {
			lastTime = Timer.getFPGATimestamp();
			lastLimitedTime = Timer.getFPGATimestamp();
			errorSum = 0.0;
			firstCycle = false;
		}

		dt = Timer.getFPGATimestamp() - lastTime;
		prevOutput = output;
		output = 0;

		// ----- Error -----
		prevError = error;
		error = setpoint - currentValue;

		// ----- P Calculation -----
		output += kP * error;

		// ----- I Calculation -----
		if (error > epsilon) {
			if (errorSum < 0.0) {
				errorSum = 0.0;
			}
			errorSum += error * dt;
		} else {
			errorSum = 0.0;
		}

		output += kI * errorSum;

		// ----- D Calculation -----
		output += kD * (2*error - prevError) * dt;
//		output *= voltageAdjustment;
//		System.out.println(voltageAdjustment);
//		 ----- Limit Output ------
		if (speedController) {
			output += prevOutput;
		}
		if (output > maxOutput) {
			output = maxOutput;
		} else if (output < -maxOutput) {
			output = -maxOutput;
		}
		if (speedController && output < 0) {
			output = 0;
		}
		
		if (initialLimited && Timer.getFPGATimestamp() - lastLimitedTime < initialLimitedTime) {
			output = TorqueMathUtil.constrain(output, initialLimitedOutput);
		}

		// ----- Save Time -----
		lastTime = Timer.getFPGATimestamp();
		
		return output;
	}

}
