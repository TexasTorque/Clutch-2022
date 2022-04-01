package org.texastorque.inputs;

import edu.wpi.first.math.kinematics.SwerveModuleState;

import org.texastorque.constants.Constants;
import org.texastorque.subsystems.Intake;
import org.texastorque.subsystems.Climber.ClimberDirection;
import org.texastorque.subsystems.Climber.ServoDirection;
import org.texastorque.subsystems.Intake.IntakeDirection;
import org.texastorque.subsystems.Intake.IntakePosition;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;

public class AutoInput {
    private static volatile AutoInput instance;

    // Drive
    private SwerveModuleState[] driveStates = new SwerveModuleState[] {
            new SwerveModuleState(), new SwerveModuleState(),
            new SwerveModuleState(), new SwerveModuleState() };

    public SwerveModuleState[] getDriveStates() {
        return driveStates;
    }

    public void setDriveStates(SwerveModuleState[] states) {
        driveStates = states;
    }

    // Shooter
    private double flywheelSpeed;
    private double hoodPosition = Constants.HOOD_MAX;

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
    private GateSpeeds gateDirection = GateSpeeds.CLOSED;
    private BeltDirections beltDirection = BeltDirections.OFF;

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

    private double climberRightSetpoint = 0;
    private double climberLeftSetpoint = 0;
    private ClimberDirection climberDirection = ClimberDirection.STOP;
    private ServoDirection servoDirection = ServoDirection.ATTACH;

    /**
     * @return the climberLeftSetpoint
     */
    public double getClimberLeftSetpoint() {
        return climberLeftSetpoint;
    }

    /**
     * @return the climberRightSetpoint
     */
    public double getClimberRightSetpoint() {
        return climberRightSetpoint;
    }

    /**
     * @param climberLeftSetpoint the climberLeftSetpoint to set
     */
    public void setClimberLeftSetpoint(double climberLeftSetpoint) {
        this.climberLeftSetpoint = climberLeftSetpoint;
    }

    /**
     * @param climberRightSetpoint the climberRightSetpoint to set
     */
    public void setClimberRightSetpoint(double climberRightSetpoint) {
        this.climberRightSetpoint = climberRightSetpoint;
    }

    /**
     * @return the climberDirection
     */
    public ClimberDirection getClimberDirection() {
        return climberDirection;
    }

    /**
     * @return the servoDirection
     */
    public ServoDirection getServoDirection() {
        return servoDirection;
    }

    /**
     * @param climberDirection the climberDirection to set
     */
    public void setClimberDirection(ClimberDirection climberDirection) {
        this.climberDirection = climberDirection;
    }

    /**
     * @param servoDirection the servoDirection to set
     */
    public void setServoDirection(ServoDirection servoDirection) {
        this.servoDirection = servoDirection;
    }

    private boolean setTurretPosition = false;
    private double turretPosition = 0;

    /**
     * @return are we setting the turret position?
     */
    public boolean getSettingTurretPosition() {
        return setTurretPosition;
    }

    /**
     * @param setTurretPosition the setTurretPosition to set
     */
    public void setSetTurretPosition(boolean setTurretPosition) {
        this.setTurretPosition = setTurretPosition;
    }

    /**
     * @return the turretPosition
     */
    public double getTurretPosition() {
        return turretPosition;
    }

    /**
     * @param turretPosition the turretPosition to set
     */
    public void setTurretPosition(double turretPosition) {
        this.turretPosition = turretPosition;
    }

    public static AutoInput getInstance() {
        return instance == null ? instance = new AutoInput() : instance;
    }
}