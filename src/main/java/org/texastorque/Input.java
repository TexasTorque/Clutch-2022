package org.texastorque;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import org.texastorque.subsystems.Drivebase;
import org.texastorque.subsystems.Drivebase.DrivebaseState;
import org.texastorque.subsystems.Intake;
import org.texastorque.subsystems.Intake.IntakeState;
import org.texastorque.subsystems.Magazine;
import org.texastorque.subsystems.Magazine.BeltDirection;
import org.texastorque.subsystems.Magazine.GateDirection;
import org.texastorque.subsystems.Shooter.ShooterState;
import org.texastorque.subsystems.Climber.ManualClimbState;
import org.texastorque.subsystems.Turret.TurretState;
import org.texastorque.torquelib.base.TorqueInput;
import org.texastorque.torquelib.control.TorqueClick;
import org.texastorque.torquelib.control.complex.TorqueTraversableSelection;
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
        updateClimber();
    }

    private final TorqueTraversableSelection<Double> translationalSpeeds 
            = new TorqueTraversableSelection<Double>(.4, .6, .8);

    private final TorqueTraversableSelection<Double> rotationalSpeeds
            = new TorqueTraversableSelection<Double>(.5, .75, 1.);

    private final void updateDrivebase() {
        drivebase.setState(driver.getRightCenterButton() ? DrivebaseState.X_FACTOR 
                : DrivebaseState.FIELD_RELATIVE);
        drivebase.setSpeeds(new ChassisSpeeds(
                driver.getLeftYAxis() * Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED,
                -driver.getLeftXAxis() * Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED,
                -driver.getRightXAxis() * Drivebase.DRIVE_MAX_ROTATIONAL_SPEED
                ));
        drivebase.setSpeedCoefs(translationalSpeeds.calculate(driver.getRightBumper(), driver.getLeftBumper()),
                rotationalSpeeds.calculate(driver.getRightBumper(), driver.getLeftBumper()));
    }

    private final void updateIntake() {
        intake.setState(driver.getRightTrigger() ? IntakeState.INTAKE : IntakeState.PRIMED);
    }

    private final void updateMagazine() {
        magazine.setState(BeltDirection.OFF, GateDirection.OFF);
    }

    private final void updateShooter() {
        if (driver.getLeftTrigger()) {
            shooter.setState(ShooterState.REGRESSION);
            turret.setState(TurretState.TRACK);
        } else {
            shooter.setState(ShooterState.OFF);
            turret.setState(TurretState.OFF);
        }
    }

    // The ugly code below will be cleaned later!
    private final TorqueClick toggleClimberHooks = new TorqueClick();
    private boolean servoEnabled = false;

    private final void updateClimber() {
        if (driver.getLeftCenterButton())
            climber.reset();

        if (driver.getDPADDown()) 
            climber.setManual(ManualClimbState.BOTH_DOWN);
        else if (driver.getDPADUp()) 
            climber.setManual(ManualClimbState.BOTH_UP);
        else if (driver.getDPADRight()) 
            climber.setManual(ManualClimbState.ZERO_RIGHT);
        else if (driver.getDPADLeft()) 
            climber.setManual(ManualClimbState.ZERO_LEFT);
        else 
            climber.setManual(ManualClimbState.OFF);

        climber.setAuto(driver.getXButton());

        if (toggleClimberHooks.calculate(driver.getYButton()))
            climber.setServos(servoEnabled = !servoEnabled);

        if (driver.getAButton()) climber._winch = .5;
        else if (driver.getBButton()) climber._winch = -.5;
        else climber._winch = 0;
    }

    public static final synchronized Input getInstance() {
        return instance == null ? instance = new Input() : instance;
    }
}
