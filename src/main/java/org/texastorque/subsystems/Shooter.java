package org.texastorque.subsystems;

import org.texastorque.constants.Constants;
import org.texastorque.constants.Ports;
import org.texastorque.inputs.Feedback;
import org.texastorque.inputs.Input;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.component.TorqueSparkMax;
import org.texastorque.torquelib.util.TorqueMathUtil;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Shooter extends TorqueSubsystem {
    private static volatile Shooter instance;

    private TorqueSparkMax flywheel;
    private Servo hood;

    // setpoints grabbed from input
    private double flywheelSpeed;
    private double hoodPosition;

    // control system
    private SimpleMotorFeedforward flywheelFeedforward = new SimpleMotorFeedforward(Constants.FLYWHEEL_Ks,
            Constants.FLYWHEEL_Kv, Constants.FLYWHEEL_Ka);
    private PIDController flywheelPIDController = new PIDController(Constants.FLYWHEEL_Kp, Constants.FLYWHEEL_Ki,
            Constants.FLYWHEEL_Kd);

    public Shooter() {
        flywheel = new TorqueSparkMax(Ports.SHOOTER_FLYWHEEL_LEFT);
        flywheel.addFollower(Ports.SHOOTER_FLYWHEEL_RIGHT);

        hood = new Servo(Ports.SHOOTER_HOOD);
    }

    @Override
    public void updateTeleop() {
        double flywheelSetpoint = Input.getInstance().getShooterInput().getFlywheel();
        hoodPosition = TorqueMathUtil.constrain(Input.getInstance().getShooterInput().getHood(),
                Constants.HOOD_MIN, Constants.HOOD_MAX);

        // TODO: confirm getVelocity is in RPM before running!!
        flywheelSpeed = flywheelFeedforward.calculate(flywheelSetpoint)
                + flywheelPIDController.calculate(flywheel.getVelocity(), flywheelSetpoint);

    }

    @Override
    public void updateFeedbackTeleop() {
        Feedback.getInstance().getShooterFeedback().setRPM(flywheel.getVelocity());
        Feedback.getInstance().getShooterFeedback().setHoodPosition(hood.get());
    }

    @Override
    public void output() {
        hood.set(hoodPosition);
        // TODO: check initial voltage before output
        flywheel.setVoltage(flywheelSpeed);
    }

    @Override
    public void updateSmartDashboard() {
        SmartDashboard.putNumber("[Shooter]Hood SetPoint", this.hoodPosition);
        SmartDashboard.putNumber("[Shooter]Flywheel SetPoint", this.flywheelSpeed);
    }

    public static synchronized Shooter getInstance() {
        return instance == null ? instance = new Shooter() : instance;
    }
}