package org.texastorque.torquelib.base;

import java.util.Timer;
import java.util.TimerTask;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.hal.FRCNetComm.tInstances;
import edu.wpi.first.hal.FRCNetComm.tResourceType;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;

/**
 * A modified version of the WPILIBJ IterativeRobot template that uses two
 * threads.
 *
 * CPU usage on the roboRio may be higher than when using the regular
 * IterativeRobot base class, but should not be a problem.
 *
 * @author TexasTorque
 */
public abstract class TorqueIterative extends RobotBase {

	private volatile boolean m_disabledInitialized;
	private volatile boolean m_autoInitialized;
	private volatile boolean m_teleopInitialized;
	private volatile boolean m_testInitialized;

	Thread periodicThread;
	Timer continousTimer;
	// period is 1 / frequency
	double continuousPeriod = 1.0 / 80.0;

	/**
	 * Create a new iterative robot.
	 */
	public TorqueIterative() {
		m_disabledInitialized = false;
		m_autoInitialized = false;
		m_teleopInitialized = false;
		m_testInitialized = false;
	}

	/**
	 * Unused.
	 */
	protected void prestart() {
		// Don't immediately say that the robot's ready to be enabled.
		// See below.
	}

	/**
	 * Overrides method from RobotBase. This is called by main and runs the user
	 * robot code.
	 */
	@Override
	public void startCompetition() {
		HAL.report(tResourceType.kResourceType_Framework, tInstances.kFramework_Iterative);
		robotInit();
		HAL.observeUserProgramStarting();

		LiveWindow.setEnabled(false);

		// Start a new Thread to run the regular Periodic code on.
		periodicThread = new Thread(new Periodic());
		periodicThread.start();

		// Shcedule the second thread to run at the period specified above.
		continousTimer = new Timer();
		continousTimer.scheduleAtFixedRate(new Continuous(), 0, (long) (1000 * continuousPeriod));

		// Prevent return from startcompetition
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
			}
		}
	}

	/**
	 * This class provides the default IterativeRobot functionality.
	 *
	 * It is synchronized to inputs coming from the driver station, and runs at
	 * about 50 hz.
	 */
	private class Periodic implements Runnable {

		@Override
		public void run() {
			while (true) {

				if (isDisabled()) {
					if (!m_disabledInitialized) {
						LiveWindow.setEnabled(false);
						disabledInit();

						m_disabledInitialized = true;
						m_autoInitialized = false;
						m_teleopInitialized = false;
						m_testInitialized = false;
					}
					if (nextPeriodReady()) {
						HAL.observeUserProgramDisabled();
						disabledPeriodic();
					}
				} else if (isTest()) {
					if (!m_testInitialized) {
						LiveWindow.setEnabled(true);
						testInit();

						m_testInitialized = true;
						m_autoInitialized = false;
						m_teleopInitialized = false;
						m_disabledInitialized = false;
					}
					if (nextPeriodReady()) {
						HAL.observeUserProgramTest();
						testPeriodic();
					}
				} else if (isAutonomous()) {
					if (DriverStation.isEnabled() && !m_autoInitialized) {

						LiveWindow.setEnabled(false);
						autoInit();

						m_autoInitialized = true;
						m_testInitialized = false;
						m_teleopInitialized = false;
						m_disabledInitialized = false;
					}
					if (nextPeriodReady()) {
						HAL.observeUserProgramAutonomous();
						autoPeriodic();
					}
				} else {
					if (!m_teleopInitialized) {
						LiveWindow.setEnabled(false);
						teleopInit();

						m_teleopInitialized = true;
						m_testInitialized = false;
						m_autoInitialized = false;
						m_disabledInitialized = false;
					}
					if (nextPeriodReady()) {
						HAL.observeUserProgramTeleop();
						teleopPeriodic();
					}
				}
				DriverStation.waitForData();
			}
		}
	}

	/**
	 * This class provides an extra execution thread to take advantage of the two
	 * cores of the roboRIO.
	 *
	 * It is scheduled to run at 100hz.
	 */
	private class Continuous extends TimerTask {

		@Override
		public void run() {
			if (isAutonomous() && m_autoInitialized) {
				autoContinuous();
				alwaysContinuous();
			} else if (isOperatorControl() && m_teleopInitialized) {
				teleopContinuous();
				alwaysContinuous();
			} else if (isDisabled() && m_disabledInitialized) {
				disabledContinuous();
				alwaysContinuous();
			} else if (isTest() && m_testInitialized) {
				testContinuous();
				alwaysContinuous();
			}

		}
	}

	private boolean nextPeriodReady() {
		return DriverStation.isNewControlData();
	}

	/* ----------- Overridable continuous code ----------------- */
	private boolean tpcFirstRun = true;

	public void teleopContinuous() {
		if (tpcFirstRun) {
			System.out.println("Default TorqueIterative.teleopContinuous() method... Overload me!");
			tpcFirstRun = false;
		}
		try {
			Thread.sleep(1);
		} catch (InterruptedException ex) {
		}
	}

	private boolean apcFirstRun = true;

	public void autoContinuous() {
		if (apcFirstRun) {
			System.out.println("Default TorqueIterative.autonomousContinuous() method... Overload me!");
			apcFirstRun = false;
		}
		try {
			Thread.sleep(1);
		} catch (InterruptedException ex) {
		}
	}

	private boolean dcFirstRun = true;

	public void disabledContinuous() {
		if (dcFirstRun) {
			System.out.println("Default TorqueIterative.disabledContinuous() method... Overload me!");
			dcFirstRun = false;
		}
		try {
			Thread.sleep(1);
		} catch (InterruptedException ex) {
		}
	}

	private boolean tcFirstRun = true;

	public void testContinuous() {
		if (tcFirstRun) {
			System.out.println("Default TorqueIterative.testContinuous() method... Overload me!");
			tcFirstRun = false;
		}
		try {
			Thread.sleep(1);
		} catch (InterruptedException ex) {
		}
	}

	private boolean ecFirstRun = true;

	public void alwaysContinuous() {
		if (ecFirstRun) {
			System.out.println("Default TorqueIterative.alwaysContinuous() method... Overload me!");
			ecFirstRun = false;
		}
		try {
			Thread.sleep(1);
		} catch (InterruptedException ex) {
		}
	}

	/* ----------- Overridable initialization code ----------------- */
	public void robotInit() {
		System.out.println("Default TorqueIterative.robotInit() method... Overload me!");
	}

	public void disabledInit() {
		System.out.println("Default TorqueIterative.disabledInit() method... Overload me!");
	}

	public void autoInit() {
		System.out.println("Default TorqueIterative.autoInit() method... Overload me!");
	}

	public void teleopInit() {
		System.out.println("Default TorqueIterative.teleopInit() method... Overload me!");
	}

	public void testInit() {
		System.out.println("Default TorqueIterative.testInit() method... Overload me!");
	}

	/* ----------- Overridable periodic code ----------------- */
	private boolean dpFirstRun = true;

	public void disabledPeriodic() {
		if (dpFirstRun) {
			System.out.println("Default TorqueIterative.disabledPeriodic() method... Overload me!");
			dpFirstRun = false;
		}
		try {
			Thread.sleep(1);
		} catch (InterruptedException ex) {
		}
	}

	private boolean apFirstRun = true;

	public void autoPeriodic() {
		if (apFirstRun) {
			System.out.println("Default TorqueIterative.autonomousPeriodic() method... Overload me!");
			apFirstRun = false;
		}
		try {
			Thread.sleep(1);
		} catch (InterruptedException ex) {
		}
	}

	private boolean tpFirstRun = true;

	public void teleopPeriodic() {
		if (tpFirstRun) {
			System.out.println("Default TorqueIterative.teleopPeriodic() method... Overload me!");
			tpFirstRun = false;
		}
		try {
			Thread.sleep(1);
		} catch (InterruptedException ex) {
		}
	}

	private boolean tmpFirstRun = true;

	public void testPeriodic() {
		if (tmpFirstRun) {
			System.out.println("Default TorqueIterative.testPeriodic() method... Overload me!");
			tmpFirstRun = false;
		}
	}
}
