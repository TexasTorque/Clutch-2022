package org.texastorque.inputs;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import java.util.ArrayList;
import java.util.List;

import org.texastorque.auto.sequences.AutoLaunch;
import org.texastorque.auto.sequences.AutoReflect;
import org.texastorque.constants.Constants;
import org.texastorque.inputs.State.*;
import org.texastorque.modules.ArduinoInterface;
import org.texastorque.modules.MagazineBallManager;
import org.texastorque.modules.ArduinoInterface.LightMode;
import org.texastorque.subsystems.Turret;
import org.texastorque.subsystems.Climber.ClimberDirection;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.TorqueAssist;
import org.texastorque.torquelib.base.TorqueInput;
import org.texastorque.torquelib.base.TorqueInputManager;
import org.texastorque.torquelib.component.TorqueSpeedSettings;
import org.texastorque.torquelib.controlLoop.TimedTruthy;
import org.texastorque.torquelib.controlLoop.TorqueSlewLimiter;
import org.texastorque.torquelib.util.GenericController;
import org.texastorque.torquelib.util.TorqueLock;
import org.texastorque.torquelib.util.TorqueMathUtil;
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

    // Etc.
    private TimedTruthy driverRumble = new TimedTruthy();
    private TimedTruthy operatorRumble = new TimedTruthy();

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
        driver.setRumble(driverRumble.calc());
        operator.setRumble(operatorRumble.calc());

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

        private TorqueSlewLimiter xLimiter = new TorqueSlewLimiter(3, 4);
        private TorqueSlewLimiter yLimiter = new TorqueSlewLimiter(3, 4);

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
        private TorqueToggle toggleLifted = new TorqueToggle();
        private IntakePosition liftedPosition = IntakePosition.PRIME;

        private IntakeDirection direction = IntakeDirection.STOPPED;
        private IntakePosition intakePosition = IntakePosition.PRIME;

        public IntakeInput() {
        }

        @Override
        public void update() {
            toggleLifted.calc(driver.getBButton());
            // liftedPosition = toggleLifted.get() ? IntakePosition.PRIME :
            // IntakePosition.UP;

            if (driver.getRightTrigger())
                direction = IntakeDirection.INTAKE;
            else if (driver.getLeftTrigger())
                direction = IntakeDirection.OUTAKE;
            else
                direction = IntakeDirection.STOPPED;

            if (driver.getRightTrigger())
                intakePosition = IntakePosition.DOWN;
            else
                intakePosition = liftedPosition;
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
        private GateSpeeds gateDirection;
        private BeltDirections beltDirection;

        private TorqueToggle autoMag = new TorqueToggle(true);

        public MagazineInput() {
        }

        @Override
        public void update() {
            // If we are asking to shoot and the turret is locked
            if (shooterInput.getFlywheel() != 0 && Turret.getInstance().checkOver()) {
                // We are "target locked"
                ArduinoInterface.getInstance().setLightMode(LightMode.TARGET_LOCK);

                // If the shooter is ready wee decide to shoot
                if (shooterInput.getFlywheel() - Constants.SHOOTER_ERROR 
                        < Feedback.getInstance().getShooterFeedback().getRPM()
                        && shooterInput.getFlywheel() + Constants.SHOOTER_ERROR 
                        > Feedback.getInstance().getShooterFeedback().getRPM()
                ) 
                    gateDirection = GateSpeeds.OPEN;
                // Otherwise we dont
                else 
                    gateDirection = GateSpeeds.CLOSED;
            } else {
                // We want to be in the normal setting
                ArduinoInterface.getInstance().setToAllianceColor();

                // Operator override
                if (operator.getLeftTrigger())
                    gateDirection = GateSpeeds.OPEN;
                else 
                    gateDirection = GateSpeeds.CLOSED;
            }

            autoMag.calc(driver.getYButton());

            if (operator.getRightTrigger())
                beltDirection = BeltDirections.FORWARDS;
            else if (autoMag.get() || operator.getRightBumper())
                beltDirection = BeltDirections.BACKWARDS;
            else
                beltDirection = BeltDirections.OFF;
        }

        public BeltDirections getBeltDirection() {
            return beltDirection;
        }

        public GateSpeeds getGateDirection() {
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
        public void setGateDirection(GateSpeeds gateDirection) {
            this.gateDirection = gateDirection;
        }
    }

    public class ShooterInput extends TorqueInput {
        private double flywheel; // rpm
        private double hood; // degrees

        public ShooterInput() {
        }

        private void setFromDist(double dist) {
            flywheel = regressionRPM(dist);
            hood = regressionHood(dist);
        }

        @Override
        public void update() {
            // Regression
            if (driver.getXButton() || Feedback.getInstance().getLimelightFeedback().getTaOffset() > 0)
                setFromDist(Feedback.getInstance().getLimelightFeedback().getDistance());
            // Layup
            else if (operator.getYButton())
                setFromDist(0); // distance at layup (tbd)
            // Launchpad
            else if (operator.getXButton())
                setFromDist(0); // distance at launchpad (tbd)
            // Tarmac
            else if (operator.getBButton())
                setFromDist(0); // distance at tarmac (tbd)
            // SmartDashboard
            else if (operator.getAButton())
                setFromDist(SmartDashboard.getNumber("[Input]Distance", 0));
            else reset();
        }

        @Override
        public void reset() {
            flywheel = 0;
            hood = 0;
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
            this.flywheel = Math.min(speed, 4000);
        }

        /**
         * Set the hood for the flywheel
         * 
         * @param hood Hood position
         */
        public void setHood(double hood) {
            this.hood = TorqueMathUtil.constrain(hood, 0, 50);
        }

        /**
         * @param distance Distance (m)
         * @return RPM the shooter should go at
         */
        public double regressionRPM(double distance) {
            return TorqueMathUtil.constrain((316.4 * distance) + 1240, 3000);
        }

        /**
         * @param distance Distance (m)
         * @return Hood the shooter should go at
         */

        public double regressionHood(double distance) {
            // past 1.9, just do max
            if (distance > 1.9)
                return 50;
            return TorqueMathUtil.constrain(22.87 * distance - 3.914, 0, 50);
        }

    }

    public class ClimberInput extends TorqueInput {
        private ClimberDirection direction = ClimberDirection.STOP;
        // ! DEBUG ONLY, PUBLIC SHOULD BE ENCAPSULATED IF PERMANENT
        public boolean runLeft = false; 
        public boolean runRight = false;

        private boolean climbHasStarted = false;

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
            
            // ^ If up or down is pressed, set the LEDs to ENDGAME
            if (direction != ClimberDirection.STOP) {
                if (!climbHasStarted)
                    ArduinoInterface.getInstance().setLightMode(LightMode.ENDGAME);
                climbHasStarted = true;
            }

            // The operator can cancel the ENGAME sequence
            if (operator.getRightCenterButton()) {
                if (climbHasStarted)
                    ArduinoInterface.getInstance().setToAllianceColor();
                climbHasStarted = false;
            }

            // ! DEBUG
            if (driver.getDPADLeft())
                runLeft = true;
            else
                runLeft = false;

            if (driver.getDPADRight())
                runRight = true;
            else
                runRight = false;
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

    /**
     * Rumble the driver's controller
     * 
     * @param forTime seconds
     */
    public void requestDriverRumble(double forTime) {
        driverRumble.setTime(forTime);
    }

    /**
     * Rumble the operator's controller
     * 
     * @param forTime seconds
     */
    public void requestOperatorRumble(double forTime) {
        operatorRumble.setTime(forTime);
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
