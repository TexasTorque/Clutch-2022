package org.texastorque.inputs;

import java.util.ArrayList;
import java.util.List;

import org.texastorque.constants.Constants;
import org.texastorque.torquelib.base.TorqueInput;
import org.texastorque.torquelib.base.TorqueInputModule;
import org.texastorque.torquelib.component.TorqueSpeedSettings;
import org.texastorque.torquelib.controlLoop.TorqueSlewLimiter;
import org.texastorque.torquelib.util.GenericController;
import org.texastorque.torquelib.util.TorqueLock;
import org.texastorque.torquelib.util.TorqueToggle;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Input {
    private static volatile Input instance;

    private GenericController driver;

    // Modules
    private DriveBaseTranslationInput driveBaseTranslationInput;
    private DriveBaseRotationInput driveBaseRotationInput;
    private List<TorqueInputModule> modules = new ArrayList<>();

    private Input() {
        driver = new GenericController(0, 0.1);

        driveBaseTranslationInput = new DriveBaseTranslationInput();
        modules.add(driveBaseTranslationInput);

        driveBaseRotationInput = new DriveBaseRotationInput();
        modules.add(driveBaseRotationInput);
    }

    public void update() {
        modules.forEach(TorqueInputModule::update);
    }

    public void smartDashboard() {
        modules.forEach(TorqueInputModule::smartDashboard);
    }

    public class DriveBaseTranslationInput implements TorqueInputModule {
        private TorqueSpeedSettings xSpeeds = new TorqueSpeedSettings(1, 0.5, 1, .25); // two speeds, 1 and .5
        private TorqueSpeedSettings ySpeeds = new TorqueSpeedSettings(1, 0.5, 1, .25); // two speeds, 1 and .5

        private double xSpeed = 0;
        private double ySpeed = 0;

        private TorqueSlewLimiter xLimiter = new TorqueSlewLimiter(1.2, 50);
        private TorqueSlewLimiter yLimiter = new TorqueSlewLimiter(1.2, 51);

        private DriveBaseTranslationInput() {
        }

        @Override
        public void update() {
            xSpeed = xLimiter.calculate(
                    driver.getLeftYAxis()) * Constants.DRIVE_MAX_SPEED_METERS;

            // Negated to get positive values when going left
            ySpeed = -yLimiter.calculate(
                    driver.getLeftXAxis()) * Constants.DRIVE_MAX_SPEED_METERS;

        }

        @Override
        public void reset() {
            xSpeed = 0;
            ySpeed = 0;
        }

        public double getXSpeed() {
            return this.xSpeed;
        }

        public double getYSpeed() {
            return this.ySpeed;
        }

        @Override
        public void smartDashboard() {
            SmartDashboard.putNumber("[Input]xSpeed", xSpeed);
            SmartDashboard.putNumber("[Input]ySpeed", ySpeed);
        }

    }

    public class DriveBaseRotationInput implements TorqueInputModule {
        private TorqueSpeedSettings rotSpeeds = new TorqueSpeedSettings(1, 0.5, 1, .25); // two speeds, 1 and .5

        private double rot = 0;

        private TorqueSlewLimiter rotLimiter = new TorqueSlewLimiter(2.2, 55);

        private TorqueLock<Double> rotLock = new TorqueLock<Double>(false);
        private TorqueToggle rotLockToggle = new TorqueToggle();

        private DriveBaseRotationInput() {
        }

        @Override
        public void update() {
            rotLockToggle.calc(driver.getRightStickClick());
            rotLock.setLocked(rotLockToggle.get());
            rot = -rotLock.calculate(rotLimiter.calculate(
                    driver.getRightXAxis()) * Constants.DRIVE_MAX_ANGUAR_SPEED_RADIANS);

        }

        @Override
        public void reset() {
            rot = 0;
        }

        public double getRot() {
            return this.rot;
        }

        @Override
        public void smartDashboard() {
            SmartDashboard.putNumber("[Input]rot", rot);
        }

    }

    // ====
    // Getters
    // ====
    public DriveBaseTranslationInput getDrivebaseTranslationInput() {
        return driveBaseTranslationInput;
    }

    public DriveBaseRotationInput getDrivebaseRotationInput() {
        return driveBaseRotationInput;
    }

    /**
     * Get the Input instance
     * 
     * @return Input
     */
    public static synchronized Input getInstance() {
        return instance == null ? instance = new Input() : instance;
    }
}
