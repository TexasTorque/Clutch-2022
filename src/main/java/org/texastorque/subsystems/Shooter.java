package org.texastorque.subsystems;
 
import com.revrobotics.CANSparkMax.ControlType;

import org.texastorque.constants.Ports;
import org.texastorque.inputs.Input;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.component.TorqueSparkMax;
import org.texastorque.torquelib.controlLoop.ScheduledPID;
import org.texastorque.util.KPID;

import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Shooter extends TorqueSubsystem {
    public static volatile Shooter instance;

    private TorqueSparkMax flywheel;
    private Servo hood;

    // setpoints grabbed from input
    private double flywheelSpeed;
    private double hoodPosition;

    // grabbed from encoders
    private double flywheelActual;
    private double hoodActual;

    public static enum FlywheelSetpoints {
        OFF(0), LAYUP(0), TARMAC(0), LAUNCHPAD(0), AUTO(-1);

        private final int rpm;

        FlywheelSetpoints(int rpm) {
            this.rpm = rpm;
        }

        public double getRPM() {
            return rpm;
        }
    }

    public static enum HoodPosition {
        OFF(0), LAYUP(0), TARMAC(0), LAUNCHPAD(0), AUTO(-1);

        private final double position;

        HoodPosition(double position) {
            this.position = position;
        }

        public double getPosition() {
            return position;
        }
    }
    
    public Shooter() {
        flywheel = new TorqueSparkMax(Ports.SHOOTER_FLYWHEEL_LEFT);
        flywheel.addFollower(Ports.SHOOTER_FLYWHEEL_RIGHT);
        
        hood = new Servo(Ports.SHOOTER_HOOD);
    }

    @Override
    public void updateTeleop() {
        FlywheelSetpoints flywheelSetpoint = Input.getInstance().getShooterInput().getFlywheel();
        if (flywheelSetpoint != FlywheelSetpoints.AUTO)
            this.flywheelSpeed = flywheelSetpoint.getRPM(); 
        else {
            // Auto range finding interface
        }

        HoodPosition hoodPosition = Input.getInstance().getShooterInput().getHood();
        if (hoodPosition != HoodPosition.AUTO)
            this.hoodPosition = hoodPosition.getPosition();
        else {
            // Auto range finding interface
        }
    }

    @Override
    public void updateFeedbackTeleop() {
        flywheelActual = flywheel.getPosition();
        hoodActual = hood.get();
    }

    @Override
    public void output() {
        // TODO: set the flywheel speed
        
        hood.set(hoodPosition);
    }

    @Override
    public void updateSmartDashboard() {
        SmartDashboard.putNumber("[Shooter]Hood SetPoint", this.hoodPosition);
        SmartDashboard.putNumber("[Shooter]Hood Position", this.hoodActual);
        SmartDashboard.putNumber("[Shooter]Flywheel SetPoint", this.flywheelSpeed);
        SmartDashboard.putNumber("[Shooter]Flywheel Speed", this.flywheelActual);
    }

    public static synchronized Shooter getInstance() {
        return instance == null ? instance = new Shooter() : instance;
    }
}