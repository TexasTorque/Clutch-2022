package org.texastorque;

import org.texastorque.subsystems.Drivebase;
import org.texastorque.subsystems.Drivebase.DrivebaseState;
import org.texastorque.torquelib.base.TorqueInputManager;
import org.texastorque.torquelib.util.GenericController;
import org.texastorque.torquelib.util.TorqueSpeedSettings;

import edu.wpi.first.math.kinematics.ChassisSpeeds;

public class Input extends TorqueInputManager {
    private static volatile Input instance;

    private TorqueSpeedSettings xSpeeds = new TorqueSpeedSettings(1, 0.6, 1, .2); // 1, .8, .6
    private TorqueSpeedSettings ySpeeds = new TorqueSpeedSettings(1, 0.6, 1, .2); // 1, .8, .6
    private TorqueSpeedSettings rSpeeds = new TorqueSpeedSettings(1, 0.5, 1, .25); // 1, .75, .5

    private Input() {
        driver = new GenericController(0, 0.1);
        operator = new GenericController(1, 0.1);
    }

    @Override
    public void update() {
        Drivebase.getInstance().setState(DrivebaseState.FIELD_RELATIVE);
        Drivebase.getInstance().setSpeeds(new ChassisSpeeds(
                driver.getLeftYAxis() * Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED
                        * xSpeeds.update(driver.getRightBumper(), driver.getLeftBumper(),false, false),
                driver.getLeftXAxis() * Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED
                        * ySpeeds.update(driver.getRightBumper(), driver.getLeftBumper(),false, false),
                driver.getRightXAxis() * Drivebase.DRIVE_MAX_ROTATIONAL_SPEED
                        * rSpeeds.update(driver.getRightBumper(), driver.getLeftBumper(),false, false)
        ));
    }

    public static synchronized Input getInstance() {
        return instance == null ? instance = new Input() : instance;
    }
}
