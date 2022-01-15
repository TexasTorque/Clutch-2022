package org.texastorque.torquelib.controlLoop;

import org.texastorque.torquelib.util.TorqueMathUtil;

import edu.wpi.first.wpilibj.Timer;

public class TorqueRIMP extends ControlLoop {

	private double kP;
	private double kV;
	private double kFFV;
	private double kFFA;

	private double actualPosition;
	private double actualVelocity;
	private double positionDoneRange;

	private double maxVelocity;
	private double maxAcceleration;
	private double previousTime = 0;
	private double dt;
	private double kCoasting;
	
	public TorqueRIMP(double maxVelocity, double maxAcceleration, double kCoasting) {
		super();

		kP = 0.0;
		kV = 0.0;
		kFFV = 0.0;
		kFFA = 0.0;
		
		this.maxVelocity = maxVelocity;
		this.maxAcceleration = maxAcceleration;
		this.kCoasting = kCoasting;
	}
	
	public TorqueRIMP(double maxVelocity, double maxAcceleration) {
			super();
			
			kP = 0.0;
			kV = 0.0;
			kFFV = 0.0;
			kFFA = 0.0;
			
			this.maxVelocity = maxVelocity;
			this.maxAcceleration = maxAcceleration;
			kCoasting = -999;
	}

	public double calculate(double currentError, double currentVelocity) {
		double voltageAdjustment = 1; // tunedVoltage / ds.getBatteryVoltage();

		calculateDT();
		
		double output = 0.0;

		// Position P
		output += (currentError * kP);

		// Velocity P
		double velocityError = maxVelocity - currentVelocity;
		output += (velocityError * kV);

		// Velocity FeedForward
		output += (maxVelocity * kFFV * voltageAdjustment);

		// Acceleration FeedForward
		output += (maxAcceleration * kFFA * voltageAdjustment);
		
		if(kCoasting != -999 && TorqueMathUtil.near(currentVelocity, Math.sqrt(currentError*6)+kCoasting, .5)) {
			output = 0;
		}
		
		return output;
	}
	
	public void calculateDT() {
		double currentTime = Timer.getFPGATimestamp();
		dt = currentTime - previousTime;
		previousTime = currentTime;
	}

	public void setGains(double p, double v, double ffV, double ffA) {
		kP = p;
		kV = v;
		kFFV = ffV;
		kFFA = ffA;
	}

	public void reset() {
	}

	public void setPositionDoneRange(double range) {
		positionDoneRange = range;
	}

//	public boolean isDone() {
//		if ((Math.abs(profile.getCurrentPosition() - actualPosition) < positionDoneRange)
//				&& Math.abs(profile.getCurrentVelocity() - actualVelocity) < doneRange) {
//			doneCyclesCount++;
//		} else {
//			doneCyclesCount = 0;
//		}
//
//		return (doneCyclesCount > minDoneCycles);
//	}
//
//	public boolean onTrack() {
//		return Math.abs(profile.getCurrentVelocity() - actualVelocity) < doneRange;
//	}
	
}
