package org.texastorque;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import org.texastorque.subsystems.Drivebase;
import org.texastorque.subsystems.Drivebase.DrivebaseState;
import org.texastorque.torquelib.base.TorqueInput;
import org.texastorque.torquelib.control.complex.TorqueSpeedSettings;
import org.texastorque.torquelib.util.GenericController;

@SuppressWarnings("deprecation")
public final class Input extends TorqueInput implements Subsystems {
    private static volatile Input instance;

    private final TorqueSpeedSettings xSpeeds = new TorqueSpeedSettings(1, 0.6, 1, .2);  // 1, .8, .6
    private final TorqueSpeedSettings ySpeeds = new TorqueSpeedSettings(1, 0.6, 1, .2);  // 1, .8, .6
    private final TorqueSpeedSettings rSpeeds = new TorqueSpeedSettings(1, 0.5, 1, .25); // 1, .75, .5


    private Input() {
        driver = new GenericController(0, 0.1);
        operator = new GenericController(1, 0.1);
    }

    @Override
    public final void update() {
        updateDrivebase();
    }

    private final void updateDrivebase() {
        drivebase.setState(DrivebaseState.FIELD_RELATIVE);
        drivebase.setSpeeds(new ChassisSpeeds(
                driver.getLeftYAxis() * Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED *
                        xSpeeds.update(driver.getRightBumper(), driver.getLeftBumper(), false, false),
                -driver.getLeftXAxis() * Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED *
                        ySpeeds.update(driver.getRightBumper(), driver.getLeftBumper(), false, false),
                -driver.getRightXAxis() * Drivebase.DRIVE_MAX_ROTATIONAL_SPEED *
                        rSpeeds.update(driver.getRightBumper(), driver.getLeftBumper(), false, false)));
    }

    public static final synchronized Input getInstance() {
        return instance == null ? instance = new Input() : instance;
    }
}
