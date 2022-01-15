package org.texastorque.util;

public class Integrator {
	
	private double lastValue;
	private double sum;
	
	public Integrator() { }
	
	public double calculate(double value, double delta) {
		this.sum += MathUtils.calcAreaTrapezoid(lastValue, value, delta);
		this.lastValue = value;
		
		return this.sum;
	}
	
	public void reset() {
		this.sum = 0;
	}
}
