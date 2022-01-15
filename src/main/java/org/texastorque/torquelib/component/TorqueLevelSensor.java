package org.texastorque.torquelib.component;

import edu.wpi.first.wpilibj.DigitalInput;

public class TorqueLevelSensor {

	private DigitalInput button;

	public TorqueLevelSensor(int port) {
		button = new DigitalInput(port);
	}

	public boolean get() {
		return !button.get();
	}
}
