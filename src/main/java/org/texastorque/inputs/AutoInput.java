package org.texastorque.inputs;

import org.texastorque.subsystems.Intake;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;

import edu.wpi.first.math.kinematics.SwerveModuleState;

public class AutoInput {
    private static volatile AutoInput instance;

    // Drive
    private SwerveModuleState[] driveStates;

    public SwerveModuleState[] getDriveStates() {
        return driveStates;
    }

    public void setDriveStates(SwerveModuleState[] states) {
        driveStates = states;
    }

    // Shooter
    private double flywheelSpeed;
    private double hoodPosition;

    /**
     * @param flywheelSpeed the flywheelSpeed to set
     */
    public void setFlywheelSpeed(double flywheelSpeed) {
        this.flywheelSpeed = flywheelSpeed;
    }

    /**
     * @param hoodPosition the hoodPosition to set
     */
    public void setHoodPosition(double hoodPosition) {
        this.hoodPosition = hoodPosition;
    }

    /**
     * @return the flywheelSpeed
     */
    public double getFlywheelSpeed() {
        return flywheelSpeed;
    }

    /**
     * @return the hoodPosition
     */
    public double getHoodPosition() {
        return hoodPosition;
    }

    // Intake
    private IntakePosition intakePosition = IntakePosition.PRIME;
    private IntakeDirection intakeSpeed = IntakeDirection.STOPPED;

    /**
     * Set the intake position
     * 
     * @param position
     */
    public void setIntakePosition(IntakePosition position) {
        intakePosition = position;
    }

    /**
     * @param direction Intake direction
     */
    public void setIntakeSpeed(IntakeDirection direction) {
        intakeSpeed = direction;
    }

    /**
     * @return the intakePosition
     */
    public IntakePosition getIntakePosition() {
        return intakePosition;
    }

    /**
     * @return the intakeSpeed
     */
    public IntakeDirection getIntakeSpeed() {
        return intakeSpeed;
    }

    // Magazine
    private GateSpeeds gateDirection;
    private BeltDirections beltDirection;

    /**
     * @param gateDirection the gateDirection to set
     */
    public void setGateDirection(GateSpeeds gateDirection) {
        this.gateDirection = gateDirection;
    }

    /**
     * @param beltDirection the beltDirection to set
     */
    public void setBeltDirection(BeltDirections beltDirection) {
        this.beltDirection = beltDirection;
    }

    /**
     * @return the gateDirection
     */
    public GateSpeeds getGateDirection() {
        return gateDirection;
    }

    /**
     * @return the beltDirection
     */
    public BeltDirections getBeltDirection() {
        return beltDirection;
    }

    public static AutoInput getInstance() {
        if (instance == null)
            instance = new AutoInput();
        return instance;
    }
}
