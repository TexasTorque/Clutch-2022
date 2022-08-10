/**
 * Copyright 2022 Texas Torque.
 * 
 * This file is part of Clutch-2022, which is not licensed for distribution.
 * For more details, see ./license.txt or write <jus@gtsbr.org>.
 */
package org.texastorque;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.texastorque.subsystems.Climber.ManualClimbState;
import org.texastorque.subsystems.Climber.ManualWinchState;
import org.texastorque.subsystems.Drivebase;
import org.texastorque.subsystems.Shooter;
import org.texastorque.subsystems.Drivebase.DrivebaseState;
import org.texastorque.subsystems.Intake.IntakeState;
import org.texastorque.subsystems.Magazine.BeltDirection;
import org.texastorque.subsystems.Magazine.GateDirection;
import org.texastorque.subsystems.Shooter.ShooterState;
import org.texastorque.subsystems.Turret.TurretState;
import org.texastorque.torquelib.base.TorqueInput;
import org.texastorque.torquelib.control.TorqueClick;
import org.texastorque.torquelib.control.TorquePID;
import org.texastorque.torquelib.control.TorqueSlewLimiter;
import org.texastorque.torquelib.control.TorqueTraversableRange;
import org.texastorque.torquelib.control.TorqueTraversableSelection;
import org.texastorque.torquelib.sensors.TorqueController;
import org.texastorque.torquelib.sensors.TorqueController.ControllerPort;

@SuppressWarnings("deprecation")
public final class Input extends TorqueInput<TorqueController> implements Subsystems {
    private static volatile Input instance;

    private Input() {
        driver = new TorqueController(ControllerPort.DRIVER);
        operator = new TorqueController(ControllerPort.OPERATOR);
    }

    @Override
    public final void update() {
        updateDrivebase();
        updateIntake();
        updateMagazine();
        updateShooter();
        updateClimber();
    }

    private final TorqueTraversableSelection<Double> 
            translationalSpeeds = new TorqueTraversableSelection<Double>(1, .35, .45, .55),
            rotationalSpeeds = new TorqueTraversableSelection<Double>(1, .5, .75, 1.);

    // Incredibly basic solution for inverting the driver controls after an auto routine.
    private double invertCoefficient = 1;
    public final void invertDrivebaseControls() { invertCoefficient = -1; }

    private final PIDController rotationPID = TorquePID.create(.02).addDerivative(.001)
            .build().createPIDController((pid) -> {
                pid.enableContinuousInput(0, 360);
                return pid;
            });

    private double lastRotation = drivebase.getGyro().getRotation2d().getDegrees();

    final TorqueSlewLimiter xLimiter = new TorqueSlewLimiter(5, 10),
                            yLimiter = new TorqueSlewLimiter(5, 10);

    private final void updateDrivebase() {
        SmartDashboard.putNumber("Speed Shifter", (rotationalSpeeds.get() - .5) *  2.); 

        // drivebase.setState(driver.getRightCenterButton() ? DrivebaseState.X_FACTOR : DrivebaseState.FIELD_RELATIVE);
        drivebase.setState(driver.getRightCenterButton() ? DrivebaseState.X_FACTOR : DrivebaseState.FIELD_RELATIVE);

        final double rotationReal = drivebase.getGyro().getRotation2d().getDegrees();
        double rotationRequested = -driver.getRightXAxis();

        if (rotationRequested == 0) 
            rotationRequested = -rotationPID.calculate(rotationReal, lastRotation);
        else
            lastRotation = rotationReal;


        SmartDashboard.putNumber("PID O", rotationRequested);
        SmartDashboard.putNumber("Rot Delta", rotationReal - lastRotation);

        drivebase.setSpeeds(new ChassisSpeeds(
                xLimiter.calculate(driver.getLeftYAxis() * Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED * invertCoefficient),
                yLimiter.calculate(-driver.getLeftXAxis() * Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED * invertCoefficient),
                rotationRequested * Drivebase.DRIVE_MAX_ROTATIONAL_SPEED * invertCoefficient));
        drivebase.setSpeedCoefs(translationalSpeeds.calculate(driver.getLeftBumper(), driver.getRightBumper()),
                                rotationalSpeeds.calculate(driver.getLeftBumper(), driver.getRightBumper()));
    }

    private final void updateIntake() {
        if (driver.getRightTrigger())
            intake.setState(IntakeState.INTAKE);
        else if (driver.getAButton())
            intake.setState(IntakeState.OUTAKE);
        else
            intake.setState(IntakeState.PRIMED);
    }

    private final void updateMagazine() { 
        updateManualMagazineBeltControls(operator);
        updateManualMagazineGateControls(operator);
        // magazine.setState(BeltDirection.OFF, GateDirection.OFF); 
    }

    private final void updateManualMagazineBeltControls(final TorqueController controller) {
        if (controller.getRightBumper())
            magazine.setBeltDirection(BeltDirection.UP);
        else if (controller.getRightTrigger())
            magazine.setBeltDirection(BeltDirection.DOWN);
        else 
            magazine.setBeltDirection(BeltDirection.OFF);
    }

    private final void updateManualMagazineGateControls(final TorqueController controller) {
        if (controller.getLeftBumper())
            magazine.setGateDirection(GateDirection.FORWARD);
        else if (controller.getLeftTrigger())
            magazine.setGateDirection(GateDirection.REVERSE);
        else 
            magazine.setGateDirection(GateDirection.OFF);
    }


    private final TorqueTraversableRange flywheelRPM = new TorqueTraversableRange(1000, 1000, 3000, 100);
    private final TorqueTraversableRange hoodSetpoint = new TorqueTraversableRange(Shooter.HOOD_MIN, Shooter.HOOD_MIN, Shooter.HOOD_MAX, 5);

    private final void updateShooter() {
        flywheelRPM.update(operator.getDPADRight(), operator.getDPADLeft(), false, false);
        hoodSetpoint.update(operator.getDPADUp(), operator.getDPADDown(), false, false);

        SmartDashboard.putNumber("IRPM", flywheelRPM.getSpeed());
        SmartDashboard.putNumber("IHOOD", hoodSetpoint.getSpeed());

        // This is debugging for the regression
        if (operator.getXButton()) {
            shooter.setState(ShooterState.SETPOINT);
            shooter.setFlywheelSpeed(flywheelRPM.getSpeed());
            shooter.setHoodPosition(hoodSetpoint.getSpeed());
            // turret.setState(TurretState.CENTER);
        } else 
        
        if (driver.getLeftTrigger()) {
            shooter.setState(ShooterState.REGRESSION);
            turret.setState(TurretState.TRACK);
        } else if (driver.getXButton()) {
            shooter.setState(ShooterState.SETPOINT);
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
        if (driver.getLeftCenterButton()) climber.reset();

        updateManualArmControls(driver);
        updateManualWinchControls(driver);

        climber.setAuto(driver.getRightCenterButton());

        if (toggleClimberHooks.calculate(driver.getYButton())) 
            climber.setServos(servoEnabled = !servoEnabled);

        SmartDashboard.putBoolean("Servos", servoEnabled);
    }

    private final void updateManualArmControls(final TorqueController controller) {
        if (controller.getDPADDown())
            climber.setManual(ManualClimbState.BOTH_DOWN);
        else if (controller.getDPADUp())
            climber.setManual(ManualClimbState.BOTH_UP);
        else if (controller.getDPADRight())
            climber.setManual(ManualClimbState.ZERO_RIGHT);
        else if (controller.getDPADLeft())
            climber.setManual(ManualClimbState.ZERO_LEFT);
        else
            climber.setManual(ManualClimbState.OFF);
    }

    private final void updateManualWinchControls(final TorqueController controller) {
        if (controller.getBButton() && controller.getDPADUp())
            climber.setWinch(ManualWinchState.OUT);
        else if (controller.getBButton() && controller.getDPADDown())
            climber.setWinch(ManualWinchState.IN);
        else
            climber.setWinch(ManualWinchState.OFF);
    }


    public static final synchronized Input getInstance() {
        return instance == null ? instance = new Input() : instance;
    }
}
