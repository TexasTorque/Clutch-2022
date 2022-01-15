package org.texastorque.torquelib.util;

public class TorqueToggle {

	private boolean toggle;
	private boolean lastValue;

	public TorqueToggle() {
		toggle = false;
		lastValue = false;
	}
	
	public TorqueToggle(boolean override) {
		toggle = override;
	}

	public void calc(boolean currentValue) {
		// Checks for an edge in boolean state. We only want to perform an action once when we go from False to True
		if (currentValue != lastValue) {
			// If the value is true now, it is the first time it is true. Flip the toggle.
			if (currentValue) {
				toggle = !toggle;
			}
			
			// Keep track of the previous value. Does not need to be updated iflastCheck is already equal to current.
			lastValue = currentValue;
		}
	}

	public void set(boolean override) {
		toggle = override;
	}

	public boolean get() {
		return toggle;
	}

}
