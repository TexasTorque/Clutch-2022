package org.texastorque.subsystems;

import org.texastorque.constants.Constants;
import org.texastorque.constants.Ports;
import org.texastorque.inputs.AutoInput;
import org.texastorque.inputs.Feedback;
import org.texastorque.inputs.Input;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.component.TorqueLinearServo;
import org.texastorque.torquelib.component.TorqueSparkMax;
import org.texastorque.torquelib.util.TorqueMathUtil;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Shooter extends TorqueSubsystem {
    private static volatile Shooter instance;

    private TorqueSparkMax flywheel;
    private TorqueLinearServo hoodLeft, hoodRight;

    // setpoints grabbed from input
    private double flywheelSpeed;
    private double hoodPosition;

    // control system
    private SimpleMotorFeedforward flywheelFeedforward = new SimpleMotorFeedforward(Constants.FLYWHEEL_Ks,
            Constants.FLYWHEEL_Kv, Constants.FLYWHEEL_Ka);
    private PIDController flywheelPIDController = new PIDController(Constants.FLYWHEEL_Kp, Constants.FLYWHEEL_Ki,
            Constants.FLYWHEEL_Kd);

    public Shooter() {
        flywheel = new TorqueSparkMax(Ports.SHOOTER_FLYWHEEL_RIGHT);
        flywheel.addFollower(Ports.SHOOTER_FLYWHEEL_LEFT);
        flywheel.invertFollower();

        hoodLeft = new TorqueLinearServo(Ports.SHOOTER_HOOD_LEFT, 50, 1);
        hoodRight = new TorqueLinearServo(Ports.SHOOTER_HOOD_RIGHT, 50, 1);

        SmartDashboard.putNumber("RPMSET", 0);
        SmartDashboard.putNumber("HOODSET", 0);
    }

    @Override
    public void updateTeleop() {

        double flywheelSetpoint = Input.getInstance().getShooterInput().getFlywheel();
        hoodPosition = TorqueMathUtil.constrain(Input.getInstance().getShooterInput().getHood(),
                Constants.HOOD_MIN, Constants.HOOD_MAX);

        // convert RPM to RPS
        flywheelSetpoint /= 60;

        flywheelSpeed = Math.min(flywheelFeedforward.calculate(flywheelSetpoint)
                + flywheelPIDController.calculate(flywheel.getVelocity() / 60,
                        flywheelSetpoint),
                12);
    }

    @Override
    public void updateAuto() {
        flywheelSpeed = AutoInput.getInstance().getFlywheelSpeed();
        hoodPosition = AutoInput.getInstance().getHoodPosition();
    }

    @Override
    public void updateFeedbackTeleop() {
        Feedback.getInstance().getShooterFeedback().setRPM(flywheel.getVelocity());
        Feedback.getInstance().getShooterFeedback().setHoodPosition(hoodRight.getPosition());
    }

    @Override
    public void output() {
        hoodRight.setPosition(hoodPosition);
        hoodLeft.setPosition(hoodPosition);
        // TODO: check initial voltage before output
        flywheel.setVoltage(flywheelSpeed);
    }

    @Override
    public void updateSmartDashboard() {
        SmartDashboard.putNumber("[Shooter]Hood SetPoint", this.hoodPosition);
        SmartDashboard.putNumber("[Shooter]Flywheel SetPoint", this.flywheelSpeed);
        SmartDashboard.putNumber("[Shooter]Flywheel Volt", flywheel.getOutputCurrent());
    }

    public static synchronized Shooter getInstance() {
        return instance == null ? instance = new Shooter() : instance;
    }
}