package org.texastorque.inputs;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import java.util.ArrayList;
import java.util.List;

import org.texastorque.auto.sequences.AutoLaunch;
import org.texastorque.auto.sequences.AutoReflect;
import org.texastorque.constants.Constants;
import org.texastorque.inputs.State.AutomaticMagazineState;
import org.texastorque.modules.MagazineBallManager;
import org.texastorque.subsystems.Climber.ClimberDirection;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateDirections;
import org.texastorque.torquelib.auto.TorqueAssist;
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
    private ClimberInput climberInput;

    private List<TorqueInput> modules = new ArrayList<>();

    // Assists
    private TorqueAssist autoLaunch = new TorqueAssist(new AutoLaunch(), magazineInput, shooterInput);
    private TorqueAssist autoReflect = new TorqueAssist(new AutoReflect(), magazineInput, shooterInput);

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

        climberInput = new ClimberInput();
        modules.add(climberInput);
    }

    @Override
    public void update() {

        // If detect our alliance, shoot.
        // If detect enemy, shoot badly
        // Otherwise, do nothin
        if (MagazineBallManager.getInstance().isOurAlliance()) {
            State.getInstance().setAutomaticMagazineState(AutomaticMagazineState.SHOOTING);
        } else if (MagazineBallManager.getInstance().isEnemyAlliance()) {
            State.getInstance().setAutomaticMagazineState(AutomaticMagazineState.REFLECTING);
        } else {
            State.getInstance().setAutomaticMagazineState(AutomaticMagazineState.OFF);
        }

        autoLaunch.run(State.getInstance().getAutomaticMagazineState() == AutomaticMagazineState.SHOOTING);
        autoReflect.run(State.getInstance().getAutomaticMagazineState() == AutomaticMagazineState.REFLECTING);

        modules.forEach(TorqueInput::run);
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

        /**
         * @param beltDirection the beltDirection to set
         */
        public void setBeltDirection(BeltDirections beltDirection) {
            this.beltDirection = beltDirection;
        }

        /**
         * @param gateDirection the gateDirection to set
         */
        public void setGateDirection(GateDirections gateDirection) {
            this.gateDirection = gateDirection;
        }
    }

    public class ShooterInput extends TorqueInput {
        private double flywheel; // rpm
        private double hood; // degrees

        public ShooterInput() {
        }

        @Override
        public void update() {
            // Launchpad shoot in case of failure
            // TODO: tune values
            if (MagazineBallManager.getInstance().getMagazineState() == MagazineBallManager.MagazineState.NONE) {
                flywheel = 5000;
                hood = 10;
            }
        }

        @Override
        public void reset() {
        }

        /**
         * 
         * @return flywheel RPM
         */
        public double getFlywheel() {
            return flywheel;
        }

        /**
         * 
         * @return hood degrees
         */
        public double getHood() {
            return hood;
        }

        /**
         * Set the speed of the flywheel
         * 
         * @param speed RPM
         */
        public void setFlywheelSpeed(double speed) {
            this.flywheel = speed;
        }
    }

    public class ClimberInput extends TorqueInput {
        private ClimberDirection direction = ClimberDirection.STOP;

        public ClimberInput() {
        }

        @Override
        public void update() {
            if (driver.getDPADUp())
                direction = ClimberDirection.PUSH;
            else if (driver.getDPADDown())
                direction = ClimberDirection.PULL;
            else
                direction = ClimberDirection.STOP;
        }

        public ClimberDirection getDirection() {
            return direction;
        }

        @Override
        public void reset() {
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

    public ClimberInput getClimberInput() {
        return climberInput;
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
