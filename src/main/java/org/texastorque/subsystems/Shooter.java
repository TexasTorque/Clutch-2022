package org.texastorque.subsystems;

import com.revrobotics.CANSparkMax.ControlType;
import com.revrobotics.SparkMaxPIDController.ArbFFUnits;

import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.constants.Constants;
import org.texastorque.constants.Ports;
import org.texastorque.inputs.AutoInput;
import org.texastorque.inputs.Feedback;
import org.texastorque.inputs.Input;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.component.TorqueLinearServo;
import org.texastorque.torquelib.component.TorqueSparkMax;
import org.texastorque.torquelib.util.TorqueMathUtil;
import org.texastorque.util.KPID;

public class Shooter extends TorqueSubsystem {
    private static volatile Shooter instance;

    private TorqueSparkMax flywheel;
    private TorqueLinearServo hoodLeft, hoodRight;

    // setpoints grabbed from input
    private double hoodPosition;
    private double flywheelSetpoint;

    private final SimpleMotorFeedforward feedforward = new SimpleMotorFeedforward(Constants.FLYWHEEL_Ks,
            Constants.FLYWHEEL_Kv);

    public Shooter() {
        flywheel = new TorqueSparkMax(Ports.SHOOTER_FLYWHEEL_LEFT);
        flywheel.addFollower(Ports.SHOOTER_FLYWHEEL_RIGHT);
        flywheel.invertFollower();
        flywheel.setSupplyLimit(40);

        flywheel.configurePID(
                new KPID(Constants.FLYWHEEL_Kp, Constants.FLYWHEEL_Ki,
                        Constants.FLYWHEEL_Kd, Constants.FLYWHEEL_Kf, 0, 1));
        flywheel.configureIZone(Constants.FLYWHEEL_Iz);
        flywheel.configureSmartMotion(Constants.FLYWHEEEL_MAX_SPEED, 0, Constants.FLYWHEEL_MAX_ACCELERATION,
                Constants.SHOOTER_ERROR, 0);

        hoodLeft = new TorqueLinearServo(Ports.SHOOTER_HOOD_LEFT, 50, 5);
        hoodRight = new TorqueLinearServo(Ports.SHOOTER_HOOD_RIGHT, 50, 5);

        SmartDashboard.putNumber("RPMSET", 0);
        SmartDashboard.putNumber("HOODSET", 0);
    }

    @Override
    public void updateTeleop() {
        if (Input.getInstance().getClimberInput().hasClimbStarted()) {
            hoodPosition = Constants.HOOD_MIN;
            flywheelSetpoint = 0;
            return;
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
        hoodPosition = TorqueMathUtil.constrain(AutoInput.getInstance().getHoodPosition(), Constants.HOOD_MIN,
                Constants.HOOD_MAX);
    }

    @Override
    public void updateFeedbackTeleop() {
        Feedback.getInstance().getShooterFeedback().setRPM(
                flywheel.getVelocity());
        Feedback.getInstance().getShooterFeedback().setHoodPosition(hoodRight.get());
    }

    @Override
    public void updateFeedbackAuto() {
        updateFeedbackTeleop();
    }

    @Override
    public void output() {
        hoodRight.setPosition(hoodPosition);
        hoodLeft.setPosition(hoodPosition);
        // flywheel.setWithFF(flywheelSetpoint, ControlType.kVelocity, 0,
        // feedforward.calculate(flywheelSetpoint / 60),
        // ArbFFUnits.kVoltage);
        flywheel.set(flywheelSetpoint, ControlType.kSmartVelocity);
    }

    @Override
    public void updateSmartDashboard() {
        SmartDashboard.putNumber("[Shooter]Hood SetPoint", this.hoodPosition);
        SmartDashboard.putNumber("[Shooter]Flywheel SetPoint", this.flywheelSetpoint);
        SmartDashboard.putNumber("[Shooter]Flywheel Volt",
                flywheel.getOutputCurrent());
        // SmartDashboard.putNumber("[Shooter] Hood Position", hood.getPosition());
    }

    public static synchronized Shooter getInstance() {
        return instance == null ? instance = new Shooter() : instance;
    }
}