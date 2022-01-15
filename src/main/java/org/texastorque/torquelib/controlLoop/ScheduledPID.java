package org.texastorque.torquelib.controlLoop;

import java.util.Arrays;

import org.texastorque.util.ArrayUtils;
import org.texastorque.util.Integrator;
import org.texastorque.util.KPID;
import org.texastorque.util.MathUtils;
import org.texastorque.util.TorqueTimer;
import org.texastorque.util.interfaces.Stopwatch;

import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.util.sendable.SendableBuilder;

public class ScheduledPID implements Sendable {

	private static boolean strictModeEnabled = false;

	private final double[] gainDivisions;
	private final double[] pGains;
	private final double[] iGains;
	private final double[] dGains;
	private final double[] fGains;

	private final double minOutput;
	private final double maxOutput;

	private double setpoint;
	private int currentGainIndex;
	private double lastError;

	private boolean firstCycle;

	private final Integrator integrator;
	private Stopwatch timer;
	private SafetyCheck safetyCheck;

	private ScheduledPID(double setpoint, double minOutput, double maxOutput, int count) {
		checkConstructorArguments(minOutput, maxOutput, count);

		this.gainDivisions = new double[count - 1];
		this.pGains = new double[count];
		this.iGains = new double[count];
		this.dGains = new double[count];
		this.fGains = new double[count];

		this.setpoint = setpoint;
		this.minOutput = minOutput;
		this.maxOutput = maxOutput;

		this.integrator = new Integrator();
		this.timer = new TorqueTimer();
	}

	private ScheduledPID(KPID kPID) {
		this.gainDivisions = new double[0];
		this.pGains = new double[1];
		this.pGains[0] = kPID.p();
		this.iGains = new double[1];
		this.iGains[0] = kPID.p();
		this.dGains = new double[1];
		this.dGains[0] = kPID.p();
		this.fGains = new double[1];
		this.fGains[0] = kPID.p();

		this.setpoint = 0;
		this.minOutput = kPID.min();
		this.maxOutput = kPID.max();

		this.integrator = new Integrator();
		this.timer = new TorqueTimer();
	}

	private ScheduledPID(double setpoint, double maxOutput, int count) {
		this(setpoint, -maxOutput, maxOutput, count);
	}

	private ScheduledPID(double setpoint, double maxOutput) {
		this(setpoint, -maxOutput, maxOutput, 1);
	}

	// == Private API ==

	private double proportionalTerm(double error) {
		double gain = this.pGains[this.currentGainIndex];

		return gain * error;
	}

	private double integralTerm(double error) {
		double gain = this.iGains[this.currentGainIndex];
		double dt = timer.lapTime();
		double integral = integrator.calculate(error, dt);

		return gain * integral;
	}

	private double derivativeTerm(double error) {
		double dt = timer.lapTime();

		if (dt == 0)
			return 0;

		double gain = this.dGains[this.currentGainIndex];
		double de = error - this.lastError;

		return gain * (de / dt);
	}

	// feedforward term. Multiplies fTerm by setpoint to add to PIDOutput
	// Use when output is needed to maintain positions
	private double feedForwardTerm(double setPoint) {
		double gain = this.fGains[this.currentGainIndex];
		return gain * setPoint;
	}

	private boolean isSafeToOutput() {
		return (this.safetyCheck == null || safetyCheck.isSafe());
	}

	private void startTimerIfNeeded() {
		if (!timer.isRunning())
			timer.start();
	}

	private double startUpdate(double processVar) {
		double error = this.setpoint - processVar;
		this.currentGainIndex = calculateGainIndex(error);

		return error;
	}

	public void finishUpdate(double error) {
		this.lastError = error;
		timer.startLap(); // Measure dt from the end of the last update.
	}

	public void setLastError(double error) {
		this.lastError = error;
	}

	public double getLastError() {
		return this.lastError;
	}

	/**
	 * Calculates the index for the current gain values.
	 * 
	 * @param error The current error in the process variable.
	 * @return The integer index for the gain values.
	 */
	private int calculateGainIndex(double error) {
		if (gainDivisions.length == 0 || error <= this.gainDivisions[0]) {
			return 0;
		}

		for (int i = 0; i < gainDivisions.length - 1; i++) {
			double leftBound = this.gainDivisions[i];
			double rightBound = this.gainDivisions[i + 1];

			if (leftBound <= error && error < rightBound) {
				return i + 1;
			}
		}

		return gainDivisions.length;
	}

	private static void checkConstructorArguments(double minOutput, double maxOutput, int count) {
		if (count <= 0) {
			String errorMessage = "PID Schedule must have at least 1 gain, count was: " + count;
			if (ScheduledPID.strictModeEnabled) {
				throw new IllegalArgumentException(errorMessage);
			} else {
				System.err.println(errorMessage);
			}
		}

		if (minOutput > maxOutput) {
			String errorMessage = "Min output (" + minOutput + ") >= max output (" + maxOutput + ").";
			if (ScheduledPID.strictModeEnabled) {
				throw new IllegalArgumentException(errorMessage);
			} else {
				System.err.println(errorMessage);
			}
		}
	}

	// == Public API ==

	public double calculate(double processVar) {

		if (firstCycle) {
			lastError = 0;
			timer.reset();
			firstCycle = false;
		}

		if (!isSafeToOutput()) {
			timer.reset();
			integrator.reset();

			if (this.safetyCheck == null) {
				return 0;
			}

			return safetyCheck.getSafetyModeOutput(this.setpoint, processVar);
		}

		startTimerIfNeeded();

		double error = startUpdate(processVar);
		double pTerm = proportionalTerm(error);
		double iTerm = integralTerm(error);
		double dTerm = derivativeTerm(error);
		double fTerm = feedForwardTerm(setpoint);
		double output = pTerm + iTerm + dTerm + fTerm; // Ideal PID form.

		finishUpdate(error);

		return MathUtils.clamp(output, minOutput, maxOutput);
	}

	public void reset() {
		integrator.reset();
		firstCycle = true;
		timer.reset();
	}

	public void changeSetpoint(double newSetpoint) {
		this.setpoint = newSetpoint;

		reset();
	}

	@Override
	public String toString() {
		return "ScheduledPID [\n\tgainDivisions=" + Arrays.toString(gainDivisions) + ", \n\tpGains="
				+ Arrays.toString(pGains) + ", \n\tiGains=" + Arrays.toString(iGains) + ", dGains="
				+ Arrays.toString(dGains) + ", \n\tcurrentGainIndex=" + currentGainIndex + ", \n\tsetpoint=" + setpoint
				+ ", \n\tmaxOutput=" + maxOutput + ", \n\tlastError=" + lastError + ", \n\tintegrator=" + integrator
				+ ", \n\ttimer=" + timer + "\n]";
	}

	/**
	 * Enables strict handling of invalid constructor arguments.
	 * 
	 * @param enabled True if invalid constructor arguments should throw errors.
	 */
	public static void enableStrictMode(boolean enabled) {
		ScheduledPID.strictModeEnabled = enabled;
	}

	// == PID Construction ==

	public static class Builder {

		private final ScheduledPID pid;

		public Builder(double setpoint, double maxOutput) {
			this.pid = new ScheduledPID(setpoint, maxOutput);
		}

		public Builder(double setpoint, double maxOutput, int count) {
			this.pid = new ScheduledPID(setpoint, maxOutput, count);
		}

		public Builder(double setpoint, double minOutput, double maxOutput, int count) {
			this.pid = new ScheduledPID(setpoint, minOutput, maxOutput, count);
		}

		public Builder setRegions(double... regions) {
			ArrayUtils.bufferAndFill(regions, pid.gainDivisions);

			// Divisions should always be specified in ascending order.
			if (!ArrayUtils.isSorted(regions, true)) {
				Arrays.sort(pid.gainDivisions);
				System.out.println("Gain schedule was not ordered correctly.");
			}

			return this;
		}

		public Builder setPGains(double... pGains) {
			ArrayUtils.bufferAndFill(pGains, pid.pGains);
			return this;
		}

		public Builder setIGains(double... iGains) {
			ArrayUtils.bufferAndFill(iGains, pid.iGains);
			return this;
		}

		public Builder setDGains(double... dGains) {
			ArrayUtils.bufferAndFill(dGains, pid.dGains);
			return this;
		}

		public Builder setFGains(double... fGains) {
			ArrayUtils.bufferAndFill(fGains, pid.fGains);
			return this;
		}

		public Builder overrideFPGATimer(Stopwatch timer) {
			this.pid.timer = timer;
			return this;
		}

		public Builder setSafetyMechanism(SafetyCheck safety) {
			this.pid.safetyCheck = safety;
			return this;
		}

		public ScheduledPID build() {
			return this.pid;
		}
	}

	public interface SafetyCheck {
		public boolean isSafe();

		public double getSafetyModeOutput(double setpoint, double process);
	}

	@Override
	public void initSendable(SendableBuilder builder) {
		builder.setSmartDashboardType("PIDController");
		builder.addDoubleProperty("p", () -> pGains[0], x -> pGains[0] = x);
		builder.addDoubleProperty("i", () -> iGains[0], x -> iGains[0] = x);
		builder.addDoubleProperty("d", () -> dGains[0], x -> dGains[0] = x);
		builder.addDoubleProperty("setpoint", () -> setpoint, x -> setpoint = x);

	}
}