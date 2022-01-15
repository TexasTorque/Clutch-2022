package org.texastorque.torquelib.controlLoop;

import edu.wpi.first.wpilibj.DriverStation;

public abstract class ControlLoop {

	protected double setPoint;
	protected double currentValue;

	protected double doneRange;
	protected int minDoneCycles;
	protected int doneCyclesCount;

	protected DriverStation ds;
	protected double tunedVoltage;

	public ControlLoop() {
		setPoint = 0;
		doneRange = 0;
		ds = DriverStation.getInstance();
	}

	public void setSetpoint(double set) {
		setPoint = set;
	}

	public void setDoneRange(double range) {
		doneRange = range;
	}

	public void setDoneCycles(int cycles) {
		minDoneCycles = cycles;
	}

	public void setTunedVoltage(double volts) {
		tunedVoltage = volts;
	}

	public boolean isDone() {
		double currError = Math.abs(setPoint - currentValue);

		if (currError <= this.doneRange) {
			doneCyclesCount++;
		} else {
			doneCyclesCount = 0;
		}

		return doneCyclesCount > minDoneCycles;
	}
}
