package org.texastorque.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.revrobotics.CANSparkMax.ControlType;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.constants.Constants;
import org.texastorque.constants.Ports;
import org.texastorque.inputs.AutoInput;
import org.texastorque.inputs.Feedback;
import org.texastorque.inputs.Input;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.component.TorqueFalcon;
import org.texastorque.torquelib.component.TorqueSparkMax;
import org.texastorque.torquelib.util.TorqueMathUtil;
import org.texastorque.util.KPID;

public class Shooter extends TorqueSubsystem {
    private static volatile Shooter instance;

    private TorqueSparkMax hood;
    private TorqueFalcon flywheel;

    // setpoints grabbed from input
    private double hoodPosition = Constants.HOOD_MAX;
    private double flywheelSetpoint;

    public Shooter() {
        flywheel = new TorqueFalcon(Ports.SHOOTER_FLYWHEEL_LEFT);
        flywheel.addFollower(Ports.SHOOTER_FLYWHEEL_RIGHT, true);

        // These numbers are going to suck
        flywheel.configurePID(
            new KPID(Constants.FLYWHEEL_Kp, Constants.FLYWHEEL_Ki,
                     Constants.FLYWHEEL_Kd, Constants.FLYWHEEL_Kf, -.1, 1));

        // flywheel.setIZone(Constants.FLYWHEEL_Iz);

        hood = new TorqueSparkMax(Ports.SHOOTER_HOOD);
        hood.invertPolarity(false);
        hood.configurePID(new KPID(Constants.HOOD_Kp, Constants.HOOD_Ki,
                                   Constants.HOOD_kd, 0, -.70, .70));
        hood.configureIZone(Constants.HOOD_Iz);
        hood.configurePositionalCANFrame();
        hood.burnFlash();

        SmartDashboard.putNumber("RPMSET", 0);
        SmartDashboard.putNumber("HOODSET", 0);
        SmartDashboard.putNumber("s kf", Constants.FLYWHEEL_Kf);
        SmartDashboard.putNumber("s kp", Constants.FLYWHEEL_Kp);
        SmartDashboard.putNumber("s ki", Constants.FLYWHEEL_Ki);
        SmartDashboard.putNumber("s kd", Constants.FLYWHEEL_Kd);
    }

    double kf = Constants.FLYWHEEL_Kf;
    double kp = Constants.FLYWHEEL_Kp;
    double ki = Constants.FLYWHEEL_Ki;
    double kd = Constants.FLYWHEEL_Kd;

    @Override
    public void updateTeleop() {
        if (Input.getInstance().getClimberInput().hasClimbStarted()) {
            hoodPosition = Constants.HOOD_MIN;
            flywheelSetpoint = 0;
            return;
        }

        if (SmartDashboard.getNumber("s kf", kf) != kf) {
            kf = SmartDashboard.getNumber("s kf", kf);
            flywheel.configurePID(new KPID(kp, ki, kd, kf, -.3, 1));
        }
        if (SmartDashboard.getNumber("s kp", kp) != kp) {
            kp = SmartDashboard.getNumber("s kp", kp);
            flywheel.configurePID(new KPID(kp, ki, kd, kf, -.3, 1));
        }
        if (SmartDashboard.getNumber("s ki", ki) != ki) {
            ki = SmartDashboard.getNumber("s ki", ki);
            flywheel.configurePID(new KPID(kp, ki, kd, kf, -.3, 1));
        }

        if (SmartDashboard.getNumber("s kf", kd) != kd) {
            kd = SmartDashboard.getNumber("s kd", kd);
            flywheel.configurePID(new KPID(kp, ki, kd, kf, -.3, 1));
        }
        flywheelSetpoint = Input.getInstance().getShooterInput().getFlywheel();
        hoodPosition = TorqueMathUtil.constrain(
            Input.getInstance().getShooterInput().getHood(), Constants.HOOD_MIN,
            Constants.HOOD_MAX);
        // flywheelSetpoint = SmartDashboard.getNumber("RPMSET", 0);
        // hoodPosition = SmartDashboard.getNumber("HOODSET", 0);
    }

    @Override
    public void updateAuto() {
        flywheelSetpoint = AutoInput.getInstance().getFlywheelSpeed();
        hoodPosition =
            TorqueMathUtil.constrain(AutoInput.getInstance().getHoodPosition(),
                                     Constants.HOOD_MIN, Constants.HOOD_MAX);
    }

    @Override
    public void updateFeedbackTeleop() {
        Feedback.getInstance().getShooterFeedback().setRPM(
            flywheel.getVelocity());
        Feedback.getInstance().getShooterFeedback().setHoodPosition(
            hood.getPosition());
        SmartDashboard.putNumber("flywheel resid",
                                 flywheel.getVelocity() - flywheelSetpoint);
    }

    @Override
    public void updateFeedbackAuto() {
        updateFeedbackTeleop();
    }

    @Override
    public void output() {
        hood.set(hoodPosition, ControlType.kPosition);

        if (flywheelSetpoint == 0 &&
            !Input.getInstance().getClimberInput().hasClimbStarted()) {
            flywheel.set(Constants.IDLE_SHOOTER_PERCENT);
            return;
        }

        flywheel.set(flywheelSetpoint, ControlMode.Velocity);
    }

    @Override
    public void updateSmartDashboard() {
        SmartDashboard.putNumber("[Shooter]Hood SetPoint", this.hoodPosition);
        SmartDashboard.putNumber("[Shooter]Flywheel SetPoint",
                                 this.flywheelSetpoint);
        // SmartDashboard.putNumber("[Shooter]Flywheel Volt",
        //         flywheel.getOutputCurrent());
        SmartDashboard.putNumber("[Shooter]Hood Position", hood.getPosition());
    }

    public static synchronized Shooter getInstance() {
        return instance == null ? instance = new Shooter() : instance;
    }
}