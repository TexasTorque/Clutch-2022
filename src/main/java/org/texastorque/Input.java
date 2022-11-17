/**
 * Copyright 2022 Texas Torque.
 *
 * This file is part of Clutch-2022, which is not licensed for distribution.
 * For more details, see ./license.txt or write <jus@gtsbr.org>.
 */
package org.texastorque;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.subsystems.Drivebase;
import org.texastorque.subsystems.Intake.IntakeState;
import org.texastorque.subsystems.Magazine;
import org.texastorque.subsystems.Shooter;
import org.texastorque.subsystems.Shooter.ShooterState;
import org.texastorque.subsystems.Turret.TurretState;
import org.texastorque.torquelib.base.TorqueDirection;
import org.texastorque.torquelib.base.TorqueInput;
import org.texastorque.torquelib.control.TorqueClick;
import org.texastorque.torquelib.control.TorquePID;
import org.texastorque.torquelib.control.TorqueSlewLimiter;
import org.texastorque.torquelib.control.TorqueTraversableRange;
import org.texastorque.torquelib.control.TorqueTraversableSelection;
import org.texastorque.torquelib.sensors.TorqueController;
import org.texastorque.torquelib.sensors.TorqueController.ControllerPort;
import org.texastorque.torquelib.util.GenericController;
import org.texastorque.torquelib.util.TorqueMath;

@SuppressWarnings("deprecation")
public final class Input extends TorqueInput<GenericController> implements Subsystems {
    // public final class Input extends TorqueInput<TorqueController> implements Subsystems {
    private static volatile Input instance;
    private final static double DEADBAND = .01;

    private boolean autoClimbFailed = false;
    private final TorqueClick autoClimbFailedClick = new TorqueClick();

    private Input() {
        driver = new GenericController(0, DEADBAND);
        operator = new GenericController(1, DEADBAND);
        // driver = new TorqueController(ControllerPort.DRIVER);
        // operator = new TorqueController(ControllerPort.OPERATOR);
    }

    @Override
    public final void update() {
        updateDrivebase();
        updateClimber();
        if (autoClimbFailed)
            return;

        updateIntake();
        updateMagazine();
        updateShooter();
    }

    private final TorqueTraversableSelection<Double>
    // translationalSpeeds = new TorqueTraversableSelection<Double>(1, .35, .45, .55),
    translationalSpeeds = new TorqueTraversableSelection<Double>(1, .5, .6, .7),
            rotationalSpeeds = new TorqueTraversableSelection<Double>(1, .5, .75, 1.);

    // Incredibly basic solution for inverting the driver controls after an auto routine.
    private double invertCoefficient = 1;

    public final void invertDrivebaseControls() {
        invertCoefficient = -1;
    }

    private static final TorquePID rotationPID = TorquePID.create(.02 / 4).addDerivative(.001)
            .addContinuousInputRange(0, 360).build();

    private double lastRotation = drivebase.getGyro().getRotation2d().getDegrees();

    private final TorqueSlewLimiter xLimiter = new TorqueSlewLimiter(5, 10), yLimiter = new TorqueSlewLimiter(5, 10);

    private final void updateDrivebase() {
        drivebase.setShouldTarget(!useTurret);

        SmartDashboard.putNumber("Speed Shifter", (rotationalSpeeds.get() - .5) * 2.);

        final double rotationReal = drivebase.getGyro().getRotation2d().getDegrees();
        double rotationRequested = -driver.getRightXAxis();

        // if (rotationRequested == 0)
        //     rotationRequested = -rotationPID.calculate(rotationReal, lastRotation);
        // else
        //     lastRotation = rotationReal;

        SmartDashboard.putNumber("PID O", rotationRequested);
        SmartDashboard.putNumber("Rot Delta", rotationReal - lastRotation);

        drivebase.setSpeedCoefs(translationalSpeeds.calculate(driver.getLeftBumper(), driver.getRightBumper()),
                rotationalSpeeds.calculate(driver.getLeftBumper(), driver.getRightBumper()));

        final boolean noInput = TorqueMath.toleranced(driver.getLeftYAxis(), DEADBAND) &&
                TorqueMath.toleranced(driver.getLeftXAxis(), DEADBAND) &&
                TorqueMath.toleranced(driver.getRightXAxis(), DEADBAND);

        drivebase.setZeroWheels(driver.getLeftCenterButton());

        if (noInput && !driver.getLeftCenterButton()) {
            drivebase.setSpeeds(new ChassisSpeeds(0, 0, 0));
            return;
        }

        final double xVelo = xLimiter
                .calculate(driver.getLeftYAxis() * Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED * invertCoefficient);
        final double yVelo = yLimiter.calculate(-driver.getLeftXAxis() * Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED *
                invertCoefficient);
        final double rVelo = .75 * rotationRequested * Drivebase.DRIVE_MAX_ROTATIONAL_SPEED * invertCoefficient;

        drivebase.setSpeeds(new ChassisSpeeds(xVelo, yVelo, rVelo));
    }

    private final void updateIntake() {
        if (operator.getAButton())
            intake.setState(IntakeState.INTAKE);
        else if (operator.getBButton())
            intake.setState(IntakeState.OUTAKE);
        else
            intake.setState(IntakeState.PRIMED);
    }

    private final void updateMagazine() {
        //magazine.setManualState(operator.getRightTrigger(), operator.getRightBumper());
        magazine.setManualBeltDirection(operator.getRightTrigger(), operator.getRightBumper());
        magazine.setManualGateDirection(operator.getLeftTrigger(), operator.getLeftBumper());

    }

    private final TorqueTraversableRange flywheelRPM = new TorqueTraversableRange(1000, 1000, 3000, 100);
    private final TorqueTraversableRange hoodSetpoint = new TorqueTraversableRange(Shooter.HOOD_MIN, Shooter.HOOD_MIN,
            Shooter.HOOD_MAX, 5);

    private boolean useTurret = true;
    private final TorqueClick useTurretClick = new TorqueClick();

    private final void updateShooter() {
        flywheelRPM.update(operator.getDPADRight(), operator.getDPADLeft(), false, false);
        hoodSetpoint.update(operator.getDPADUp(), operator.getDPADDown(), false, false);

        SmartDashboard.putBoolean("Using Turret", useTurret);

        final double opInitAngle = Math.atan2(operator.getRightYAxis(), operator.getRightXAxis());
        SmartDashboard.putNumber("opInitAngle", opInitAngle);
        turret.setAngleToHub(opInitAngle);

        if (driver.getLeftTrigger()) {
            shooter.setState(ShooterState.REGRESSION);
            turret.setState(useTurret ? TurretState.TRACK : TurretState.POSITIONAL);
            if (useTurret)
                turret.setPosition(180);
        } else if (driver.getXButton()) {
            shooter.setState(ShooterState.SETPOINT);
            System.out.println("Shooting");
            shooter.setFlywheelSpeed(1600);
            shooter.setHoodPosition(30);
            turret.setState(TurretState.CENTER);
        } else {
            shooter.setState(ShooterState.OFF);
            turret.setState(TurretState.OFF);
        }
    }

    private final TorqueClick toggleClimberHooks = new TorqueClick();
    private boolean servoEnabled = true;

    private final void updateClimber() {
        // if (autoClimbFailedClick.calculate(driver.getRightCenterButton()))
        //     autoClimbFailed = !autoClimbFailed;

        //if (driver.getLeftCenterButton()) climber.reset();

        if (autoClimbFailed) {
            updateManualArmControls(driver);
            updateManualWinchControls(driver);
        } else {
        }

        climber.coef = 5;
        climber.setAuto(driver.getYButton());

        //if (!driver.getBButton())
        if (driver.getDPADUp())
            climber.setManualRight(TorqueDirection.FORWARD);
        else if (driver.getDPADDown())
            climber.setManualRight(TorqueDirection.REVERSE);
        else
            climber.setManualRight(TorqueDirection.OFF);

        //if (!driver.getBButton())
        if (driver.getDPADUp())
            climber.setManualLeft(TorqueDirection.FORWARD);
        else if (driver.getDPADDown())
            climber.setManualLeft(TorqueDirection.REVERSE);
        else
            climber.setManualLeft(TorqueDirection.OFF);

        if (driver.getBButton())
            if (driver.getDPADDown())
                climber.setManualWinch(TorqueDirection.FORWARD);
            else if (driver.getDPADUp())
                climber.setManualWinch(TorqueDirection.REVERSE);
            else if (driver.getDPADRight())
                climber.setManualWinch(TorqueDirection.FORWARD);
            else if (driver.getDPADLeft())
                climber.setManualWinch(TorqueDirection.REVERSE);
            else
                climber.setManualWinch(TorqueDirection.OFF);
        else
            climber.setManualWinch(TorqueDirection.OFF);

        if (toggleClimberHooks.calculate(driver.getAButton() || operator.getBButton()))
            climber.setServos(servoEnabled = !servoEnabled);

        SmartDashboard.putBoolean("Servos", servoEnabled);
    }

    // Im sorry, shreyas has a gun to my head making change do the controls -justus
    private final void updateManualArmControls(final GenericController ctrl) {
        if (ctrl.getDPADRight()) {

            climber.coef = 1;
            climber.setManualRight(TorqueDirection.FORWARD);
        } else if (ctrl.getRightTrigger()) {

            climber.coef = 1;
            climber.setManualRight(TorqueDirection.FORWARD);
        } else if (ctrl.getRightBumper()) {

            climber.coef = 1;
            climber.setManualRight(TorqueDirection.REVERSE);
        } else
            ; // climber.setManualRight(TorqueDirection.OFF);

        if (ctrl.getDPADLeft()) {
            climber.coef = 1;
            climber.setManualLeft(TorqueDirection.FORWARD);
        } else if (ctrl.getLeftTrigger()) {

            climber.coef = 1;
            climber.setManualLeft(TorqueDirection.FORWARD);
        } else if (ctrl.getLeftBumper()) {

            climber.coef = 1;
            climber.setManualLeft(TorqueDirection.REVERSE);
        } else
            ; // climber.setManualLeft(TorqueDirection.OFF);
    }

    private final void updateManualWinchControls(final GenericController ctrl) {
        // private final void updateManualWinchControls(final TorqueController controller) {
        if (ctrl.getDPADUp())
            climber.setManualWinch(TorqueDirection.FORWARD);
        else if (ctrl.getDPADDown())
            climber.setManualWinch(TorqueDirection.REVERSE);
        else
            ; // climber.setManualWinch(TorqueDirection.OFF);
    }

    public static final synchronized Input getInstance() {
        return instance == null ? instance = new Input() : instance;
    }
}
