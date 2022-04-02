package org.texastorque.inputs;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import java.util.ArrayList;
import java.util.List;
import org.texastorque.auto.sequences.assists.*;
import org.texastorque.auto.sequences.assists.AutoClimb;
import org.texastorque.constants.Constants;
import org.texastorque.inputs.State.*;
import org.texastorque.modules.MagazineBallManager;
import org.texastorque.subsystems.Climber.ClimberDirection;
import org.texastorque.subsystems.Climber.ServoDirection;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Drivebase;
import org.texastorque.subsystems.Lights;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.subsystems.Turret.HomingDirection;
import org.texastorque.subsystems.Turret;
import org.texastorque.torquelib.auto.TorqueAssist;
import org.texastorque.torquelib.auto.TorqueAssist.AssistMode;
import org.texastorque.torquelib.base.TorqueInput;
import org.texastorque.torquelib.base.TorqueInputManager;
import org.texastorque.torquelib.component.TorqueSpeedSettings;
import org.texastorque.torquelib.controlLoop.TimedTruthy;
import org.texastorque.torquelib.controlLoop.TorqueSlewLimiter;
import org.texastorque.torquelib.util.GenericController;
import org.texastorque.torquelib.util.TorqueClick;
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
    private TorqueAssist rotateToBall;
    private TorqueAssist climbAssist;

    // Etc.
    private TimedTruthy driverRumble = new TimedTruthy();
    private TimedTruthy operatorRumble = new TimedTruthy();
    private TorqueToggle rotateToBallToggle = new TorqueToggle(false);
    private TorqueToggle climberToggle = new TorqueToggle(false);

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

        rotateToBall = new TorqueAssist(new RotateToBall(), driveBaseRotationInput);
        climbAssist = new TorqueAssist(new AutoClimb(),
                driveBaseTranslationInput, driveBaseRotationInput,
                intakeInput, magazineInput, shooterInput, climberInput);
    }

    @Override
    public void update() {
        rotateToBallToggle.calc(operator.getBButton());
        rotateToBall.run(
                intakeInput.getPosition() == IntakePosition.DOWN
                        && rotateToBallToggle.get() && !climberInput.hasClimbStarted());

        climberToggle.calc(operator.getXButton());
        if (climberToggle.get()) {
            climbAssist.run(true);
        }

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

        // private TorqueSlewLimiter xLimiter = new TorqueSlewLimiter(3, 4);
        // private TorqueSlewLimiter yLimiter = new TorqueSlewLimiter(3, 4);

        private DriveBaseTranslationInput() {
        }

        @Override
        public void update() {
            xSpeeds.update(driver.getRightBumper(), driver.getLeftBumper(),
                    false, false);

            ySpeeds.update(driver.getRightBumper(), driver.getLeftBumper(),
                    false, false);

            xSpeed = xSpeeds.getSpeed() * driver.getLeftYAxis() *
                    Constants.DRIVE_MAX_SPEED_METERS;

            // Negated to get positive values when going left
            ySpeed = -ySpeeds.getSpeed() * driver.getLeftXAxis() *
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

        public void setXSpeed(double xSpeed) {
            this.xSpeed = xSpeed;
        }

        public void setYSpeed(double ySpeed) {
            this.ySpeed = ySpeed;
        }

        @Override
        public void smartDashboard() {
            SmartDashboard.putNumber("Speed", xSpeeds.getSpeed());
        }
    }

    public class DriveBaseRotationInput extends TorqueInput {
        private TorqueSpeedSettings rotSpeeds = new TorqueSpeedSettings(1, 0.5, 1, .25); // two speeds, 1 and .5

        private double rot = 0;

        // private TorqueSlewLimiter rotLimiter = new TorqueSlewLimiter(50,
        // 1000);

        private TorqueLock<Double> rotLock = new TorqueLock<Double>(false);
        private TorqueToggle rotLockToggle = new TorqueToggle();

        private DriveBaseRotationInput() {
        }

        @Override
        public void update() {
            rotSpeeds.update(driver.getRightBumper(), driver.getLeftBumper(),
                    false, false);
            rotLockToggle.calc(driver.getRightStickClick());
            rotLock.setLocked(rotLockToggle.get());
            // rot =
            // -rotLock.calculate(rotLimiter.calculate(rotSpeeds.getSpeed() *
            // driver.getRightXAxis())
            // *Constants.DRIVE_MAX_ANGUAR_SPEED_RADIANS);
            rot = -rotLock.calculate(
                    rotSpeeds.getSpeed() * driver.getRightXAxis() *
                            Constants.DRIVE_MAX_ANGUAR_SPEED_RADIANS_DRIVER);
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
            liftedPosition = toggleLifted.get() ? IntakePosition.PRIME : IntakePosition.UP;

            if (driver.getRightTrigger())
                direction = IntakeDirection.INTAKE;
            else if (driver.getLeftTrigger())
                direction = IntakeDirection.OUTAKE;
            else
                direction = IntakeDirection.STOPPED;

            if (driver.getRightTrigger() || driver.getLeftTrigger())
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
            autoMag.calc(operator.getAButton());

            if (operator.getLeftTrigger())
                gateDirection = GateSpeeds.OPEN;
            else if (operator.getLeftBumper())
                gateDirection = GateSpeeds.CLOSED;
            // If we are asking to shoot and the flywheel is reved and the
            // turret is locked
            else if (shooterReady())
                gateDirection = GateSpeeds.OPEN;
            else
                gateDirection = GateSpeeds.OFF;

            if (operator.getRightTrigger())
                beltDirection = BeltDirections.OUTTAKE;
            else if (operator.getRightBumper() || shooterReady())
                beltDirection = BeltDirections.INTAKE;
            else
                beltDirection = BeltDirections.OFF;
        }

        private boolean shooterReady() {
            return shooterInput.getFlywheel() != 0 &&
                    shooterInput.isFlywheelReady() &&
                    (!shooterInput.isUsingRegression() ||
                            Feedback.getInstance().isTurretAlligned());
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
        private boolean usingRegression = false;
        private double flywheel; // rpm
        private double hood;
        private TorqueToggle xFactorToggle;
        private int readyCounter = 0;
        private final int readyCounterNeeded = 10;
        private HomingDirection homingDirection = HomingDirection.NONE;
        private TorqueSpeedSettings rpmAdjust = new TorqueSpeedSettings(0, -400, 400, 10);
        private TorqueClick startShoot = new TorqueClick();

        public ShooterInput() {
            xFactorToggle = new TorqueToggle(true);
            setRawValues(0, Constants.HOOD_MAX);
        }

        public boolean isUsingRegression() {
            return usingRegression;
        }

        private void setRawValues(double flywheel, double hood) {
            this.flywheel = flywheel;
            this.hood = hood;
            usingRegression = false;
        }

        private void setFromDist(double dist) {
            flywheel = regressionRPM(dist);
            hood = regressionHood(dist);
            usingRegression = true;
        }

        public boolean isFlywheelReady() {
            if (Math.abs(flywheel - Feedback.getInstance().getShooterFeedback().getRPM()) < Constants.SHOOTER_ERROR) {
                readyCounter++;
                if (readyCounter > readyCounterNeeded) {
                    return true;
                }
            } else {
                readyCounter = 0;
            }
            return false;
        }

        public boolean xFactor() {
            return xFactorToggle.get() && flywheel != 0;
        }

        @Override
        public void update() {
            xFactorToggle.calc(operator.getDPADUp()); // TEMP CONTROL?
            rpmAdjust.update(operator.getRightCenterButton(), operator.getLeftCenterButton(), false, false);

            // Regression
            if (driver.getXButton()) {
                if (Feedback.getInstance()
                        .getLimelightFeedback()
                        .getTaOffset() > 0) {
                    setFromDist(Feedback.getInstance()
                            .getLimelightFeedback()
                            .getDistance());
                } else
                    setFromDist(Constants.HUB_CENTER_POSITION
                            .getDistance(Drivebase.getInstance().odometry.getPoseMeters().getTranslation()));
            }

            // Layup
            else if (driver.getYButton()) {
                setRawValues(1550, Constants.HOOD_MIN);
                State.getInstance().setTurretState(TurretState.CENTER);
            } else
                reset();

            if (driver.getAButton())
                State.getInstance().setTurretState(TurretState.CENTER);

            if (startShoot.calc(driver.getXButton() || startShoot.calc(operator.getYButton())))
                updateToPositon();

            if (operator.getDPADLeft())
                homingDirection = HomingDirection.LEFT;
            else if (operator.getDPADRight())
                homingDirection = HomingDirection.RIGHT;
            else
                homingDirection = HomingDirection.NONE;
        }

        private void updateToPositon() {

            // Pose2d robotPosition = Drivebase.getInstance().odometry.getPoseMeters();
            // double xDist = Constants.HUB_CENTER_POSITION.getX() - robotPosition.getX();
            // double yDist = Constants.HUB_CENTER_POSITION.getY() - robotPosition.getY();
            // double robotAngleFromGoal = Math.atan2(yDist, xDist);

            // if (robotAngleFromGoal < Constants.TURRET_MAX_ROTATION_LEFT
            // && robotAngleFromGoal > Constants.TURRET_MAX_ROTATION_RIGHT) {
            // Rotation2d rotation = (robotPosition.getRotation().minus(new
            // Rotation2d(robotAngleFromGoal)))
            // .times(-1);
            // State.getInstance().setTurretState(TurretState.TO_POSITION);
            // State.getInstance()
            // .setTurretToPosition(rotation);
            // } else {
            State.getInstance().setTurretState(TurretState.ON);
            // }
        }

        @Override
        public void smartDashboard() {
            SmartDashboard.putString("HomingDirection", homingDirection.toString());
            SmartDashboard.putNumber("RPM Adjust", rpmAdjust.getSpeed());
        }

        @Override
        public void reset() {
            flywheel = 0;
            State.getInstance().setTurretState(TurretState.OFF);
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
            this.flywheel = Math.min(speed, Constants.FLYWHEEEL_MAX_SPEED);
        }

        /**
         * Set the hood for the flywheel
         *
         * @param hood Hood position
         */
        public void setHood(double hood) {
            this.hood = TorqueMathUtil.constrain(hood, Constants.HOOD_MIN, Constants.HOOD_MAX);
        }

        /**
         * @param distance Distance (m)
         * @return RPM the shooter should go at
         */
        public double regressionRPM(double distance) {
            return TorqueMathUtil.constrain((177.5 * distance) + 1316 + rpmAdjust.getSpeed(), 0,
                    Constants.FLYWHEEEL_MAX_SPEED);
        }

        /**
         * @param distance Distance (m)
         * @return Hood the shooter should go at
         */

        public double regressionHood(double distance) {
            if (distance > 3.5)
                return Constants.HOOD_MAX;
            return TorqueMathUtil.constrain(-72.22 * Math.exp(-0.5019 * distance) + 51.42,
                    Constants.HOOD_MIN,
                    Constants.HOOD_MAX);
        }

        public HomingDirection getHomingDirection() {
            return homingDirection;
        }
    }

    public class ClimberInput extends TorqueInput {
        private ClimberDirection direction = ClimberDirection.STOP;
        private ServoDirection servoDirection = ServoDirection.ATTACH;

        // ! DEBUG ONLY, PUBLIC SHOULD BE ENCAPSULATED IF PERMANENT
        public boolean runLeft = false;
        public boolean runRight = false;

        private boolean climbHasStarted = false;
        private boolean hookOverride = false;
        private boolean shreyasApproval = false; // (:

        public ClimberInput() {
        }

        @Override
        public void update() {
            if (driver.getDPADUp() || driver.getDPADUpLeft() || driver.getDPADUpRight())
                direction = ClimberDirection.PUSH;
            else if (driver.getDPADDown() || driver.getDPADDownRight() || driver.getDPADDownLeft())
                direction = ClimberDirection.PULL;
            else
                direction = ClimberDirection.STOP;

            // ^ If up or down is pressed, set the LEDs to ENDGAME
            if (direction != ClimberDirection.STOP)
                climbHasStarted = true;

            // The operator can cancel the ENGAME sequence
            if (operator.getLeftCenterButton())
                climbHasStarted = false;

            if (driver.getLeftCenterButton()) {
                servoDirection = ServoDirection.ATTACH;
            } else if (driver.getRightCenterButton()) {
                servoDirection = ServoDirection.DETACH;
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

            shreyasApproval = driver.getYButton();

            hookOverride = operator.getRightCenterButton();
        }

        public ClimberDirection getDirection() {
            return direction;
        }

        public boolean hasClimbStarted() {
            return climbHasStarted;
        }

        /**
         * @return the servoDirection
         */
        public ServoDirection getServoDirection() {
            return servoDirection;
        }

        public boolean getHookOverride() {
            return hookOverride;
        }

        public boolean getShreyasApproval() {
            return driver.getYButton();
        }

        @Override
        public void smartDashboard() {
            SmartDashboard.putBoolean("Climb Started", climbHasStarted);
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
