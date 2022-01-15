package org.texastorque.torquelib.util;

import edu.wpi.first.wpilibj.Joystick;

/**
 * A class that reads input from either a Logitech or Xbox controller.
 *
 * @author TexasTorque
 */
public final class GenericController extends Joystick {

	public static final int TYPE_LOGITECH = 1;
	public static final int TYPE_XBOX = 2;

	public int[] controllerMap;
	private int controllerType;
	private double deadband;

	public GenericController(int port, double dband) {
		this(port, TYPE_XBOX, dband);
	}

	/**
	 * Create a new controller.
	 *
	 * @param port
	 *            Which Driver Station port the controller is in (use constant).
	 * @param type
	 *            Type of controller.
	 * @param dband
	 *            The size of the deadband.
	 */
	public GenericController(int port, int type, double dband) {
		super(port);
		controllerType = type;
		deadband = Math.min(1, Math.abs(dband));

		setType(controllerType);
	}

	/**
	 * Scale the input value 0 to 1 so that inputs less than the deadband are
	 * possible. We need this for terrible controller with huge deadzones.
	 *
	 * @param input
	 *            The raw joystick value.
	 * @return The scaled joystick value.
	 */
	private double scaleInput(double input) {
		if(deadband >= 1) {
			deadband = .99;
		}
		if (Math.abs(input) > deadband) {
			if (input > 0) {
				return (input - deadband) / (1 - deadband);
			} else {
				return (input + deadband) / (1 - deadband);
			}
		} else {
			return 0.0;
		}
	}

	/**
	 * Set the deadband of the controller.
	 *
	 * @param dband
	 *            The new deadband to use.
	 */
	public void setDeadband(double dband) {
		deadband = Math.min(1, Math.abs(dband));
	}

	/**
	 * Change controller type. Will default to Xbox if an incorrect type is
	 * given.
	 *
	 * @param type
	 *            New controller type.
	 */
	public synchronized void setType(int type) {
		controllerType = type;
		switch (type) {
		case TYPE_LOGITECH:
			controllerMap = new int[] { 1, 0, 3, 2, 5, 5, 11, 12, 5, 6, 7, 8, 9, 10, 1, 4, 3, 2 };
			break;
		case TYPE_XBOX:
			controllerMap = new int[] { 1, 0, 5, 4, 6, 6, 9, 10, 5, 6, 2, 3, 7, 8, 3, 4, 2, 1 };
			break;
		default:
			// default to xbox
			controllerMap = new int[] { 2, 1, 5, 4, 6, 6, 9, 10, 5, 6, 3, 3, 7, 8, 3, 4, 2, 1 };
			controllerType = TYPE_XBOX;
		}
	}
//	deprecated by FRC update 2017; was originally known as getType(); replace with getType() which returns an HIDType
	@Deprecated
	public synchronized int getControllerType() {
		return controllerType;
	}

	public synchronized double getLeftYAxis() {
		return scaleInput(getRawAxis(controllerMap[0]));
	}

	public synchronized double getLeftXAxis() {
		return scaleInput(getRawAxis(controllerMap[1]));
	}

	public synchronized double getRightYAxis() {
		return scaleInput(getRawAxis(controllerMap[2]));
	}

	public synchronized double getRightXAxis() {
		return scaleInput(getRawAxis(controllerMap[3]));
	}

	public synchronized boolean getLeftStickClick() {
		return getRawButton(controllerMap[6]);
	}

	public synchronized boolean getRightStickClick() {
		return getRawButton(controllerMap[7]);
	}

	public synchronized boolean getLeftBumper() {
		return getRawButton(controllerMap[8]);
	}

	public synchronized boolean getRightBumper() {
		return getRawButton(controllerMap[9]);
	}

	public synchronized boolean getLeftTrigger() {
		if (controllerType == TYPE_LOGITECH) {
			return getRawButton(controllerMap[10]);
		} else if (controllerType == TYPE_XBOX) {
			return (getRawAxis(controllerMap[10]) > 0.2);
		} else {
			return false;
		}
	}

	public synchronized double getLeftZAxis(){
		if (controllerType == TYPE_LOGITECH) {
			return getRawAxis(controllerMap[10]);
		} else if (controllerType == TYPE_XBOX) {
			return (getRawAxis(controllerMap[10]));
		} else {
			return 0;
		}
	}

	public synchronized boolean getRightTrigger() {
		if (controllerType == TYPE_LOGITECH) {
			return getRawButton(controllerMap[11]);
		} else if (controllerType == TYPE_XBOX) {
			return (getRawAxis(controllerMap[11]) > 0.2);
		} else {
			return false;
		}
	}

	public synchronized double getRightZAxis(){
		if (controllerType == TYPE_LOGITECH) {
			return getRawAxis(controllerMap[11]);
		} else if (controllerType == TYPE_XBOX) {
			return (getRawAxis(controllerMap[11]));
		} else {
			return 0;
		}
	}

	public synchronized boolean getLeftCenterButton() {
		return getRawButton(controllerMap[12]);
	}

	public synchronized boolean getRightCenterButton() {
		return getRawButton(controllerMap[13]);
	}

	public synchronized boolean getXButton() {
		return getRawButton(controllerMap[14]);
	}

	public synchronized boolean getYButton() {
		return getRawButton(controllerMap[15]);
	}

	public synchronized boolean getBButton() {
		return getRawButton(controllerMap[16]);
	}

	public synchronized boolean getAButton() {
		return getRawButton(controllerMap[17]);
	}
	
	public synchronized boolean getAButtonReleased() {
		return getRawButtonReleased(controllerMap[17]);
	}
	public synchronized boolean getBButtonReleased() {
		return getRawButtonReleased(controllerMap[16]);
	}
	public synchronized boolean getXButtonReleased() {
		return getRawButtonReleased(controllerMap[14]);
	}
	public synchronized boolean getYButtonReleased() {
		return getRawButtonReleased(controllerMap[15]);
	}
	public synchronized boolean getAButtonPressed() {
		return getRawButtonPressed(controllerMap[17]);
	}
	public synchronized boolean getBButtonPressed() {
		return getRawButtonPressed(controllerMap[16]);
	}
	public synchronized boolean getXButtonPressed() {
		return getRawButtonPressed(controllerMap[14]);
	}
	public synchronized boolean getYButtonPressed() {
		return getRawButtonPressed(controllerMap[15]);
	}
		
	public synchronized boolean getDPADUp() {
		return getPOV() == 0;
	}
	
	public synchronized boolean getDPADUpLeft() {
		return getPOV() == 315;
	}
	
	public synchronized boolean getDPADUpRight() {
		return getPOV() == 45;
	}
	
	public synchronized boolean getDPADRight() {
		return getPOV() == 90;
	}
	
	public synchronized boolean getDPADDown() {
		return getPOV() == 180;
	}
	
	public synchronized boolean getDPADLeft() {
		return getPOV() == 270;
	}

	public synchronized void setLeftRumble(boolean on) {
		setRumble(Joystick.RumbleType.kLeftRumble, on ? 1 : 0);
	}

	public synchronized void setRightRumble(boolean on) {
		setRumble(Joystick.RumbleType.kRightRumble, on ? 1 : 0);
	}
	
	public synchronized void setRumble(boolean on) {
		setLeftRumble(on);
		setRightRumble(on);
	}
}
