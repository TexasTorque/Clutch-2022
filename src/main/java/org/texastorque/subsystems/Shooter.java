package org.texastorque.subsystems;

import com.revrobotics.CANSparkMax.ControlType;

import org.texastorque.constants.Constants;
import org.texastorque.constants.Ports;
import org.texastorque.inputs.AutoInput;
import org.texastorque.inputs.Feedback;
import org.texastorque.inputs.Input;
import org.texastorque.inputs.State;
import org.texastorque.inputs.State.TurretState;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.component.TorqueLinearServo;
import org.texastorque.torquelib.component.TorqueSparkMax;
import org.texastorque.torquelib.util.TorqueMathUtil;
import org.texastorque.util.KPID;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Shooter extends TorqueSubsystem {
    private static volatile Shooter instance;

    private TorqueSparkMax flywheel;
    private TorqueSparkMax hood;

    // setpoints grabbed from input
    private double hoodPosition;
    private double flywheelSetpoint;

    public Shooter() {
        flywheel = new TorqueSparkMax(Ports.SHOOTER_FLYWHEEL_RIGHT);
        flywheel.addFollower(Ports.SHOOTER_FLYWHEEL_LEFT);
        flywheel.invertFollower();

        flywheel.configurePID(new KPID(Constants.FLYWHEEL_Kp, Constants.FLYWHEEL_Ki, Constants.FLYWHEEL_Kd,
                Constants.FLYWHEEL_Kf, 0, 1));
        flywheel.configureIZone(Constants.FLYWHEEL_Iz);

        hood = new TorqueSparkMax(Ports.SHOOTER_HOOD);

        // SmartDashboard.putNumber("RPMSET", 0);
        // SmartDashboard.putNumber("HOODSET", 0);
    }

    @Override
    public void updateTeleop() {

        flywheelSetpoint = Input.getInstance().getShooterInput().getFlywheel();
        // flywheelSetpoint = SmartDashboard.getNumber("RPMSET", 0);
        // hoodPosition = SmartDashboard.getNumber("HOODSET", 50);
        hoodPosition = TorqueMathUtil.constrain(Input.getInstance().getShooterInput().getHood(),
                Constants.HOOD_MIN, Constants.HOOD_MAX);
        updateTurretState();
    }

    @Override
    public void updateAuto() {
        flywheelSetpoint = AutoInput.getInstance().getFlywheelSpeed();
        hoodPosition = AutoInput.getInstance().getHoodPosition();
        updateTurretState();
    }

    /**
     * Enable turret if shooting
     */
    private void updateTurretState() {
        if (flywheelSetpoint != 0) {
            State.getInstance().setTurretState(TurretState.ON);
        } else {
            State.getInstance().setTurretState(TurretState.OFF);
        }
    }

    @Override
    public void updateFeedbackTeleop() {
        Feedback.getInstance().getShooterFeedback().setRPM(flywheel.getVelocity());
        Feedback.getInstance().getShooterFeedback().setHoodPosition(hood.getPosition());
    }

    @Override
    public void updateFeedbackAuto() {
        updateFeedbackTeleop();
    }

    @Override
    public void output() {
        hood.set(hoodPosition, ControlType.kPosition);
        flywheel.set(flywheelSetpoint, ControlType.kVelocity);
    }

    @Override
    public void updateSmartDashboard() {
        SmartDashboard.putNumber("[Shooter]Hood SetPoint", this.hoodPosition);
        SmartDashboard.putNumber("[Shooter]Flywheel SetPoint", this.flywheelSetpoint);
        SmartDashboard.putNumber("[Shooter]Flywheel Volt", flywheel.getOutputCurrent());
    }

    public static synchronized Shooter getInstance() {
        return instance == null ? instance = new Shooter() : instance;
    }
}