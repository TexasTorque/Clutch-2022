package org.texastorque;

import org.texastorque.subsystems.Drivebase;
import org.texastorque.subsystems.Intake;
import org.texastorque.subsystems.Magazine;
import org.texastorque.subsystems.Shooter;
import org.texastorque.subsystems.Drivebase.DrivebaseState;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirection;
import org.texastorque.subsystems.Magazine.GateDirection;
import org.texastorque.subsystems.Shooter.ShooterState;
import org.texastorque.torquelib.base.TorqueInputManager;
import org.texastorque.torquelib.util.GenericController;
import org.texastorque.torquelib.util.TorqueSpeedSettings;

import edu.wpi.first.math.kinematics.ChassisSpeeds;

public class Input extends TorqueInputManager {
    private static volatile Input instance;

    private TorqueSpeedSettings xSpeeds = new TorqueSpeedSettings(1, 0.6, 1, .2); // 1, .8, .6
    private TorqueSpeedSettings ySpeeds = new TorqueSpeedSettings(1, 0.6, 1, .2); // 1, .8, .6
    private TorqueSpeedSettings rSpeeds = new TorqueSpeedSettings(1, 0.5, 1, .25); // 1, .75, .5

    private Input() {
        driver = new GenericController(0, 0.1);
        operator = new GenericController(1, 0.1);
    }

    @Override
    public void update() {
        updateDrivebase();
        updateIntake();
        updateMagazine();
        updateShooter();
    }

    private void updateDrivebase() {
        Drivebase.getInstance().setState(DrivebaseState.FIELD_RELATIVE);
        Drivebase.getInstance().setSpeeds(new ChassisSpeeds(
                driver.getLeftYAxis() * Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED
                        * xSpeeds.update(driver.getRightBumper(), driver.getLeftBumper(),false, false),
                -driver.getLeftXAxis() * Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED
                        * ySpeeds.update(driver.getRightBumper(), driver.getLeftBumper(),false, false),
                -driver.getRightXAxis() * Drivebase.DRIVE_MAX_ROTATIONAL_SPEED
                        * rSpeeds.update(driver.getRightBumper(), driver.getLeftBumper(),false, false)
        ));
    }

    private void updateIntake() {
        if (driver.getRightTrigger())
            Intake.getInstance().setState(IntakeDirection.INTAKE, IntakePosition.DOWN);
        else if (driver.getLeftTrigger())
            Intake.getInstance().setState(IntakeDirection.OUTAKE, IntakePosition.DOWN);
        else
            Intake.getInstance().setState(IntakeDirection.STOPPED, IntakePosition.UP);
    }

    private void updateMagazine() {
        if (operator.getRightBumper())
            Magazine.getInstance().setBeltDirection(BeltDirection.UP);
        else if (operator.getRightTrigger())
            Magazine.getInstance().setBeltDirection(BeltDirection.DOWN);
        else
            Magazine.getInstance().setBeltDirection(BeltDirection.OFF);

        if (operator.getLeftBumper())
            Magazine.getInstance().setGateDirection(GateDirection.FORWARD);
        else if (operator.getLeftTrigger())
            Magazine.getInstance().setGateDirection(GateDirection.REVERSE);
        else
            Magazine.getInstance().setGateDirection(GateDirection.OFF);
    }

    private void updateShooter() {
        if (driver.getXButton())
            Shooter.getInstance().setState(ShooterState.REGRESSION);
        else if (driver.getAButton()) {
            Shooter.getInstance().setState(ShooterState.SETPOINT);
            Shooter.getInstance().setFlywheelSpeed(1500);
            Shooter.getInstance().setHoodPosition(Shooter.HOOD_MAX);
        } else if (driver.getYButton()) {
            Shooter.getInstance().setState(ShooterState.DISTANCE);
            Shooter.getInstance().setDistance(3);
        } else
            Shooter.getInstance().setState(ShooterState.OFF);
    }



    public static synchronized Input getInstance() {
        return instance == null ? instance = new Input() : instance;
    }
}
