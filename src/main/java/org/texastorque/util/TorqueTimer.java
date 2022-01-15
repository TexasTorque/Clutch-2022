package org.texastorque.util;

import org.texastorque.util.interfaces.Stopwatch;

import edu.wpi.first.wpilibj.Timer;

public class TorqueTimer implements Stopwatch {

	private double startTime = -1;
	private double lastTime = -1;
	private boolean started = false;
	
	public TorqueTimer() { }
	
	@Override
	public double elapsed() {
		startIfNeeded();
		
		return Timer.getFPGATimestamp() - this.startTime;
	}
	
	@Override
	public double lapTime() {
		startIfNeeded();
		
		return Timer.getFPGATimestamp() - this.lastTime;
	}
	
	@Override
	public void startLap() {
		this.lastTime = Timer.getFPGATimestamp();
	}
	
	@Override
	public double timeSince(double lastTime) {
		startIfNeeded();
		
		return Timer.getFPGATimestamp() - lastTime;
	}
	
	@Override
	public double start() {
		if (this.started) {
			return this.startTime;
		}
		
		this.startTime = Timer.getFPGATimestamp();
		this.lastTime = this.startTime;
		
		return this.startTime;
	}
	
	@Override
	public boolean isRunning() {
		return this.started;
	}
	
	@Override
	public void reset() {
		this.started = false;
	}
	
	private void startIfNeeded() {
		if (this.startTime <= 0 || this.lastTime <= 0) {
			start();
			this.started = true;
		}
	}
}
