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

    private boolean autoClimbFailed = false;
    private final TorqueClick autoClimbFailedClick = new TorqueClick();

    private Input() {
        driver = new GenericController(0, .01);
        operator = new GenericController(1, .01);
        // driver = new TorqueController(ControllerPort.DRIVER);
        // operator = new TorqueController(ControllerPort.OPERATOR);
    }

    @Override
    public final void update() {
        updateClimber();
        if (autoClimbFailed) return;

        updateDrivebase();
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
    public final void invertDrivebaseControls() { invertCoefficient = -1; }

    private static final TorquePID rotationPID =
            TorquePID.create(.02 / 4).addDerivative(.001).addContinuousInputRange(0, 360).build();

    private double lastRotation = drivebase.getGyro().getRotation2d().getDegrees();

    private final TorqueSlewLimiter xLimiter = new TorqueSlewLimiter(5, 10), yLimiter = new TorqueSlewLimiter(5, 10);

    private final static double DEADBAND = .1;

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

        if (noInput) {
            drivebase.setSpeeds(new ChassisSpeeds(0, 0, 0));
            return;
        }

        final double xVelo =
                xLimiter.calculate(driver.getLeftYAxis() * Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED * invertCoefficient);
        final double yVelo = yLimiter.calculate(-driver.getLeftXAxis() * Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED *
                                                invertCoefficient);
        final double rVelo = rotationRequested * Drivebase.DRIVE_MAX_ROTATIONAL_SPEED * invertCoefficient;

        drivebase.setSpeeds(new ChassisSpeeds(xVelo, yVelo, rVelo));
    }

    private final void updateIntake() {
        if (driver.getRightTrigger())
            intake.setState(IntakeState.INTAKE);
        else if (driver.getAButton())
            intake.setState(IntakeState.OUTAKE);
        else
            intake.setState(IntakeState.PRIMED);
    }

    private final void updateMagazine() { magazine.setManualState(false, false); }

    private final TorqueTraversableRange flywheelRPM = new TorqueTraversableRange(1000, 1000, 3000, 100);
    private final TorqueTraversableRange hoodSetpoint =
            new TorqueTraversableRange(Shooter.HOOD_MIN, Shooter.HOOD_MIN, Shooter.HOOD_MAX, 5);

    private boolean useTurret = true;
    private final TorqueClick useTurretClick = new TorqueClick();

    private final void updateShooter() {
        flywheelRPM.update(driver.getDPADRight(), driver.getDPADLeft(), false, false);
        hoodSetpoint.update(driver.getDPADUp(), driver.getDPADDown(), false, false);

        SmartDashboard.putNumber("IRPM", flywheelRPM.getSpeed());
        SmartDashboard.putNumber("IHOOD", hoodSetpoint.getSpeed());

        if (useTurretClick.calculate(operator.getAButton()))
            useTurret = !useTurret;

        SmartDashboard.putBoolean("Using Turret", useTurret);


        final double opInitAngle = Math.atan2(operator.getRightYAxis(),  operator.getRightXAxis());
        SmartDashboard.putNumber("opInitAngle", opInitAngle);
        turret.setAngleToHub(opInitAngle);

        if (driver.getLeftTrigger()) {
            shooter.setState(ShooterState.REGRESSION);
            turret.setState(useTurret ? TurretState.TRACK : TurretState.POSITIONAL);
            if (useTurret)
                turret.setPosition(180);
        } else if (driver.getXButton()) {
            shooter.setState(ShooterState.SETPOINT);
            shooter.setFlywheelSpeed(flywheelRPM.getSpeed());
            shooter.setHoodPosition(hoodSetpoint.getSpeed());
            turret.setState(TurretState.CENTER);
        } else {
            shooter.setState(ShooterState.OFF);
            turret.setState(TurretState.OFF);
        }
    }

    private final TorqueClick toggleClimberHooks = new TorqueClick();
    private boolean servoEnabled = true;

    private final void updateClimber() {
        if (autoClimbFailedClick.calculate(driver.getRightCenterButton()))
            autoClimbFailed = !autoClimbFailed;

        if (driver.getLeftCenterButton()) climber.reset();

        if (autoClimbFailed) {
            updateManualArmControls(driver);
            updateManualWinchControls(driver);
        } else {
            updateManualArmControls(operator);
            updateManualWinchControls(operator);
        }

        climber.setAuto(driver.getYButton());

        if (toggleClimberHooks.calculate(driver.getBButton() || operator.getBButton()))
            climber.setServos(servoEnabled = !servoEnabled);

        SmartDashboard.putBoolean("Servos", servoEnabled);
    }

    private final void updateManualArmControls(final GenericController ctrl) {
        if (ctrl.getRightTrigger())
            climber.setManualRight(TorqueDirection.FORWARD);
        else if (ctrl.getRightBumper())
            climber.setManualRight(TorqueDirection.REVERSE);
        else
            climber.setManualRight(TorqueDirection.OFF);

        if (ctrl.getLeftTrigger())
            climber.setManualLeft(TorqueDirection.FORWARD);
        else if (ctrl.getLeftBumper())
            climber.setManualLeft(TorqueDirection.REVERSE);
        else
            climber.setManualLeft(TorqueDirection.OFF);
    }

    private final void updateManualWinchControls(final GenericController ctrl) {
        // private final void updateManualWinchControls(final TorqueController controller) {
        // if (ctrl.getDPADUp())
            // climber.setManualWinch(TorqueDirection.FORWARD);
        // else if (ctrl.getDPADDown())
            // climber.setManualWinch(TorqueDirection.REVERSE);
        // else
            // climber.setManualWinch(TorqueDirection.OFF);
    }

    public static final synchronized Input getInstance() {
        return instance == null ? instance = new Input() : instance;
    }
}
