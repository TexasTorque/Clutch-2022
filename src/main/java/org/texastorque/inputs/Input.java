package org.texastorque.inputs;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import java.util.ArrayList;
import java.util.List;
import org.texastorque.constants.Constants;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateDirections;
import org.texastorque.subsystems.Shooter.FlywheelSetpoints;
import org.texastorque.subsystems.Shooter.HoodPosition;
import org.texastorque.torquelib.base.TorqueInput;
import org.texastorque.torquelib.base.TorqueInputManager;
import org.texastorque.torquelib.component.TorqueSpeedSettings;
import org.texastorque.torquelib.controlLoop.TorqueSlewLimiter;
import org.texastorque.torquelib.util.GenericController;
import org.texastorque.torquelib.util.TorqueLock;
import org.texastorque.torquelib.util.TorqueToggle;

public class Input extends TorqueInputManager {
    private static volatile Input instance;

    private GenericController driver;
    private GenericController operator;

    // Modules
    private DriveBaseTranslationInput driveBaseTranslationInput;
    private DriveBaseRotationInput driveBaseRotationInput;
    private IntakeInput intakeInput;
    private MagazineInput magazineInput;
    private ShooterInput shooterInput;

    private List<TorqueInput> modules = new ArrayList<>();

    private Input() {
        driver = new GenericController(0, 0.1);
        operator = new GenericController(1, 0.1);

        driveBaseTranslationInput = new DriveBaseTranslationInput();
        modules.add(driveBaseTranslationInput);

        driveBaseRotationInput = new DriveBaseRotationInput();
        modules.add(driveBaseRotationInput);

        intakeInput = new IntakeInput();
        modules.add(intakeInput);

        magazineInput = new MagazineInput();
        modules.add(magazineInput);

        shooterInput = new ShooterInput();
        modules.add(shooterInput);
    }

    @Override
    public void update() {
        modules.forEach(TorqueInput::run); // dont ask!
    }

    @Override
    public void smartDashboard() {
        modules.forEach(TorqueInput::smartDashboard);
    }

    public class DriveBaseTranslationInput extends TorqueInput {
        private TorqueSpeedSettings xSpeeds = new TorqueSpeedSettings(1, 0.2, 1, .4); // two speeds, 1 and .5
        private TorqueSpeedSettings ySpeeds = new TorqueSpeedSettings(1, 0.2, 1, .4); // two speeds, 1 and .5

        private double xSpeed = 0;
        private double ySpeed = 0;

        private TorqueSlewLimiter xLimiter = new TorqueSlewLimiter(50, 1000);
        private TorqueSlewLimiter yLimiter = new TorqueSlewLimiter(50, 1000);

        private DriveBaseTranslationInput() {
        }

        @Override
        public void update() {
            xSpeeds.update(driver.getRightBumper(), driver.getLeftBumper(), false, false);

            ySpeeds.update(driver.getRightBumper(), driver.getLeftBumper(), false, false);

            xSpeed = xLimiter.calculate(xSpeeds.getSpeed() * driver.getLeftYAxis()) *
                    Constants.DRIVE_MAX_SPEED_METERS;

            // Negated to get positive values when going left
            ySpeed = -yLimiter.calculate(ySpeeds.getSpeed() * driver.getLeftXAxis()) *
                    Constants.DRIVE_MAX_SPEED_METERS;
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
            SmartDashboard.putNumber("[Input]X Speed", xSpeed);
            SmartDashboard.putNumber("[Input]Y Speed", ySpeed);
        }
    }

    public class DriveBaseRotationInput extends TorqueInput {
        private TorqueSpeedSettings rotSpeeds = new TorqueSpeedSettings(1, 0.5, 1, .25); // two speeds, 1 and .5

        private double rot = 0;

        private TorqueSlewLimiter rotLimiter = new TorqueSlewLimiter(50, 1000);

        private TorqueLock<Double> rotLock = new TorqueLock<Double>(false);
        private TorqueToggle rotLockToggle = new TorqueToggle();

        private DriveBaseRotationInput() {
        }

        @Override
        public void update() {
            rotSpeeds.update(driver.getRightBumper(), driver.getLeftBumper(), false, false);
            rotLockToggle.calc(driver.getRightStickClick());
            rotLock.setLocked(rotLockToggle.get());
            rot = -rotLock.calculate(
                    rotLimiter.calculate(rotSpeeds.getSpeed() * driver.getRightXAxis()) *
                            Constants.DRIVE_MAX_ANGUAR_SPEED_RADIANS);
        }

        @Override
        public void reset() {
            rot = 0;
        }

        public double setRot(double rot) {
            return this.rot = rot;
        }

        public double getRot() {
            return this.rot;
        }

        @Override
        public void smartDashboard() {
            SmartDashboard.putNumber("[Input]Rotation", rot);
        }
    }

    public class IntakeInput extends TorqueInput {
        private IntakeDirection direction = IntakeDirection.STOPPED;
        private IntakePosition intakePosition = IntakePosition.UP;

        public IntakeInput() {
        }

        @Override
        public void update() {
            if (driver.getRightTrigger()) {
                direction = IntakeDirection.INTAKE;
                if (driver.getLeftTrigger())
                    direction = IntakeDirection.OUTAKE;
            } else
                direction = IntakeDirection.STOPPED;

            if (driver.getRightTrigger())
                intakePosition = IntakePosition.DOWN;
            else
                intakePosition = IntakePosition.UP;
        }

        public IntakeDirection getDirection() {
            return direction;
        }

        public IntakePosition getPosition() {
            return intakePosition;
        }

        @Override
        public void reset() {
        }
    }

    public class MagazineInput extends TorqueInput {
        private GateDirections gateDirection;
        private BeltDirections beltDirection;

        public MagazineInput() {
        }

        @Override
        public void update() {
            if (operator.getLeftTrigger())
                gateDirection = GateDirections.OPEN;
            else
                gateDirection = GateDirections.CLOSED;

            if (operator.getRightBumper())
                beltDirection = BeltDirections.BACKWARDS;
            else if (operator.getRightTrigger())
                beltDirection = BeltDirections.FORWARDS;
            else
                beltDirection = BeltDirections.OFF;
        }

        public BeltDirections getBeltDirection() {
            return beltDirection;
        }

        public GateDirections getGateDirection() {
            return gateDirection;
        }
    }

    public class ShooterInput extends TorqueInput {
        private FlywheelSetpoints flywheel;
        private HoodPosition hood;

        public ShooterInput() {
        }

        @Override
        public void update() {
            if (operator.getYButton()) {
                flywheel = FlywheelSetpoints.LAYUP;
                hood = HoodPosition.LAYUP;
            } else if (operator.getBButton()) {
                flywheel = FlywheelSetpoints.TARMAC;
                hood = HoodPosition.TARMAC;
            } else if (operator.getXButton()) {
                flywheel = FlywheelSetpoints.LAUNCHPAD;
                hood = HoodPosition.LAUNCHPAD;
            } else if (operator.getAButton()) {
                flywheel = FlywheelSetpoints.AUTO;
                hood = HoodPosition.AUTO;
            } else {
                flywheel = FlywheelSetpoints.OFF;
                hood = HoodPosition.OFF;
            }
        }

        @Override
        public void reset() {
        }

        public FlywheelSetpoints getFlywheel() {
            return flywheel;
        }

        public HoodPosition getHood() {
            return hood;
        }
    }


    public DriveBaseTranslationInput getDrivebaseTranslationInput() {
        return driveBaseTranslationInput;
    }

    public DriveBaseRotationInput getDrivebaseRotationInput() {
        return driveBaseRotationInput;
    }

    public IntakeInput getIntakeInput() {
        return intakeInput;
    }

    public MagazineInput getMagazineInput() {
        return magazineInput;
    }

    public ShooterInput getShooterInput() {
        return shooterInput;
    }

    @Override
    public void requestRumble(double forTime) {
        // ignore rn
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
