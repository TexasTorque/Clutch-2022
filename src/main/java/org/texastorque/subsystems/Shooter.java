package org.texastorque.subsystems;

import com.revrobotics.CANSparkMax.ControlType;
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
    private TorqueSparkMax hood;

    // setpoints grabbed from input
    private double hoodPosition;
    private double flywheelSetpoint;

    public Shooter() {
        flywheel = new TorqueSparkMax(Ports.SHOOTER_FLYWHEEL_LEFT);
        flywheel.addFollower(Ports.SHOOTER_FLYWHEEL_RIGHT);
        flywheel.invertFollower();
        flywheel.setSupplyLimit(40);

        flywheel.configurePID(
                new KPID(Constants.FLYWHEEL_Kp, Constants.FLYWHEEL_Ki,
                        Constants.FLYWHEEL_Kd, Constants.FLYWHEEL_Kf, 0, 1));
        flywheel.configureIZone(Constants.FLYWHEEL_Iz);

        hood = new TorqueSparkMax(Ports.SHOOTER_HOOD);
        hood.invertPolarity(true);
        hood.configurePID(new KPID(Constants.HOOD_Kp, Constants.HOOD_Ki, Constants.HOOD_kd, 0, -1, 1));
        hood.configureIZone(Constants.HOOD_Iz);

        SmartDashboard.putNumber("RPMSET", 0);
        SmartDashboard.putNumber("HOODSET", 0);
    }

    @Override
    public void updateTeleop() {

        flywheelSetpoint = Input.getInstance().getShooterInput().getFlywheel();
        // flywheelSetpoint = SmartDashboard.getNumber("RPMSET", 0);
        // hoodPosition = SmartDashboard.getNumber("HOODSET", 0);
        hoodPosition = TorqueMathUtil.constrain(
                Input.getInstance().getShooterInput().getHood(), Constants.HOOD_MIN,
                Constants.HOOD_MAX);
    }

    @Override
    public void updateAuto() {
        flywheelSetpoint = AutoInput.getInstance().getFlywheelSpeed();
        hoodPosition = AutoInput.getInstance().getHoodPosition();
    }

    @Override
    public void updateFeedbackTeleop() {
        Feedback.getInstance().getShooterFeedback().setRPM(
                flywheel.getVelocity());
        Feedback.getInstance().getShooterFeedback().setHoodPosition(
                hood.getPosition());
    }

    @Override
    public void updateFeedbackAuto() {
        updateFeedbackTeleop();
    }

    @Override
    public void output() {
        hood.set(TorqueMathUtil.constrain(hoodPosition, Constants.HOOD_MIN, Constants.HOOD_MAX), ControlType.kPosition);
        flywheel.set(flywheelSetpoint, ControlType.kVelocity);
    }

    @Override
    public void updateSmartDashboard() {
        SmartDashboard.putNumber("[Shooter]Hood SetPoint", this.hoodPosition);
        SmartDashboard.putNumber("[Shooter]Flywheel SetPoint",
                this.flywheelSetpoint);
        SmartDashboard.putNumber("[Shooter]Flywheel Volt",
                flywheel.getOutputCurrent());
        SmartDashboard.putNumber("[Shooter] Hood Position", hood.getPosition());
    }

    public static synchronized Shooter getInstance() {
        return instance == null ? instance = new Shooter() : instance;
    }
}