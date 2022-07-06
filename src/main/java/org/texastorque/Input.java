/**
 * Copyright 2022 Texas Torque.
 * 
 * This file is part of Clutch-2022, which is not licensed for distribution.
 * For more details, see ./license.txt or write <jus@gtsbr.org>.
 */
package org.texastorque;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.texastorque.subsystems.Climber.ManualClimbState;
import org.texastorque.subsystems.Climber.ManualWinchState;
import org.texastorque.subsystems.Drivebase;
import org.texastorque.subsystems.Drivebase.DrivebaseState;
import org.texastorque.subsystems.Faller.FallerState;
import org.texastorque.subsystems.Faller.WinchState;
import org.texastorque.subsystems.Intake.IntakeState;
import org.texastorque.subsystems.Magazine.BeltDirection;
import org.texastorque.subsystems.Magazine.GateDirection;
import org.texastorque.subsystems.Shooter.ShooterState;
import org.texastorque.subsystems.Turret.TurretState;
import org.texastorque.torquelib.base.TorqueInput;
import org.texastorque.torquelib.control.TorqueClick;
import org.texastorque.torquelib.control.TorqueTraversableSelection;
import org.texastorque.torquelib.util.GenericController;

@SuppressWarnings("deprecation")
public final class Input extends TorqueInput implements Subsystems {
    private static volatile Input instance;

    private Input() {
        driver = new GenericController(0, 0.1);
        operator = new GenericController(1, 0.1);
    }

    @Override
    public final void update() {
        updateDrivebase();
        updateIntake();
        updateMagazine();
        updateShooter();

        if (Robot.USE_CLIMBER)
            updateClimber();
        else
            updateFaller();
    }

    private final TorqueTraversableSelection<Double> translationalSpeeds =
            new TorqueTraversableSelection<Double>(1, .35, .55, .75);
            // new TorqueTraversableSelection<Double>(1, .4, .6, .8);

    private final TorqueTraversableSelection<Double> rotationalSpeeds =
            new TorqueTraversableSelection<Double>(1, .5, .75, 1.);

    // Incredibly basic solution for inverting the driver controls after an auto routine.
    private double invertCoefficient = 1;
    public final void invertDrivebaseControls() { invertCoefficient = -1; }

    private final void updateDrivebase() {
        SmartDashboard.putNumber("Secret", translationalSpeeds.get());

        drivebase.setState(driver.getRightCenterButton() ? DrivebaseState.X_FACTOR : DrivebaseState.FIELD_RELATIVE);
        drivebase.setSpeeds(new ChassisSpeeds(
                driver.getLeftYAxis() * Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED * invertCoefficient,
                -driver.getLeftXAxis() * Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED * invertCoefficient,
                -driver.getRightXAxis() * Drivebase.DRIVE_MAX_ROTATIONAL_SPEED * invertCoefficient));
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

    private final void updateManualMagazineBeltControls(final GenericController controller) {
        if (controller.getRightBumper())
            magazine.setBeltDirection(BeltDirection.UP);
        else if (controller.getRightTrigger())
            magazine.setBeltDirection(BeltDirection.DOWN);
        else 
            magazine.setBeltDirection(BeltDirection.OFF);
    }

    private final void updateManualMagazineGateControls(final GenericController controller) {
        if (controller.getLeftBumper())
            magazine.setGateDirection(GateDirection.FORWARD);
        else if (controller.getLeftTrigger())
            magazine.setGateDirection(GateDirection.REVERSE);
        else 
            magazine.setGateDirection(GateDirection.OFF);
    }

    private final void updateShooter() {
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
    }

    private final void updateManualArmControls(final GenericController controller) {
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

    private final void updateManualWinchControls(final GenericController controller) {
        if (controller.getBButton() && controller.getDPADUp())
            climber.setWinch(ManualWinchState.OUT);
        else if (controller.getBButton() && controller.getDPADDown())
            climber.setWinch(ManualWinchState.IN);
        else
            climber.setWinch(ManualWinchState.OFF);
    }

    private final TorqueClick toggleFallerHooks = new TorqueClick();
    private boolean fallerServosEnabled = true;

    private final void updateFaller() {
        if (driver.getLeftCenterButton()) climber.reset();

        updateFallerArms(driver);
        updateFallerWinch(driver);

        if (toggleFallerHooks.calculate(driver.getYButton())) 
            climber.setServos(fallerServosEnabled = !fallerServosEnabled);
    }

    private final void updateFallerArms(final GenericController controller) {
        if (controller.getDPADDown())
            faller.setState(FallerState.BOTH_DOWN);
        else if (controller.getDPADUp())
            faller.setState(FallerState.BOTH_UP);
        else if (controller.getDPADRight())
            faller.setState(FallerState.ZERO_RIGHT);
        else if (controller.getDPADLeft())
            faller.setState(FallerState.ZERO_LEFT);
        else
            faller.setState(FallerState.OFF);
    }

    private final void updateFallerWinch(final GenericController controller) {
        if (controller.getBButton() && controller.getDPADUp())
            faller.setWinch(WinchState.OUT);
        else if (controller.getBButton() && controller.getDPADDown())
        faller.setWinch(WinchState.IN);
        else
        faller.setWinch(WinchState.OFF);
    }


    public static final synchronized Input getInstance() {
        return instance == null ? instance = new Input() : instance;
    }
}
