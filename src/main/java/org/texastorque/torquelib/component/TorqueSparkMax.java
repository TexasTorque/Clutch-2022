package org.texastorque.torquelib.component;

import java.util.ArrayList;

import com.revrobotics.CANAnalog;
import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANAnalog.AnalogMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import org.texastorque.util.KPID;

/**
 * TorqueMotor using SparkMax for CAN motor management.
 */
public class TorqueSparkMax extends TorqueMotor {

    private CANSparkMax sparkMax;
    private CANEncoder sparkMaxEncoder;
    private CANEncoder alternateEncoder;
    private CANPIDController pidController;
    private CANAnalog analogEncoder;
    private ArrayList<CANSparkMax> sparkMaxFollowers = new ArrayList<>();

    private double encoderZero = 0;

    public TorqueSparkMax(int port) {
        this.port = port;
        sparkMax = new CANSparkMax(port, MotorType.kBrushless);
        sparkMaxEncoder = sparkMax.getEncoder();
        analogEncoder = sparkMax.getAnalog(AnalogMode.kAbsolute);
        pidController = sparkMax.getPIDController();
    }

    @Override
    public void addFollower(int port) {
        sparkMaxFollowers.add(new CANSparkMax(port, MotorType.kBrushless));
        System.out.println("Added spark max follower");
    }

    public void restoreFactoryDefaults() {
        sparkMax.restoreFactoryDefaults();
    }

    // ===================== Set Methods ========================
    @Override
    public void set(double output) {
        sparkMax.set(output);
        for (CANSparkMax canSparkMax : sparkMaxFollowers) {
            canSparkMax.follow(sparkMax);
        } // takes care of followers
    }

    public void set(double output, ControlType ctrlType) {
        try {
            pidController.setReference(output, ctrlType);
            for (CANSparkMax follower : sparkMaxFollowers) {
                follower.follow(sparkMax, invert);
            } // takes care of followers
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("You need to configure the PID");
        } // try catch
    } // set method for use with PID, position or velocity

    /**
     * UNSAFE
     */
    public void enableVoltageCompensation() {
        sparkMax.enableVoltageCompensation(2);
        for (CANSparkMax follower : sparkMaxFollowers) {
            follower.enableVoltageCompensation(2);
        }
    }

    /**
     * UNSAFE
     */
    public void disableVoltageCompensation() {
        sparkMax.disableVoltageCompensation();
        for (CANSparkMax follower : sparkMaxFollowers) {
            follower.disableVoltageCompensation();
        }

    }

    /**
     * Directly set voltage output. Be careful!
     */
    public void setVoltage(double volts) {
        sparkMax.setVoltage(volts);
    }

    // ===================== PID stuff ========================

    @Override
    public void configurePID(KPID kPID) {
        pidController.setP(kPID.p());
        pidController.setI(kPID.i());
        pidController.setD(kPID.d());
        pidController.setFF(kPID.f());
        pidController.setOutputRange(kPID.min(), kPID.max());
    } // configure PID

    @Override
    public void updatePID(KPID kPID) {
        pidController.setP(kPID.p());
        pidController.setI(kPID.i());
        pidController.setD(kPID.d());
        pidController.setFF(kPID.f());
        pidController.setOutputRange(kPID.min(), kPID.max());
    } // update PID

    /**
     * Configure needed variables for smart motion.
     * 
     * - setSmartMotionMaxVelocity() will limit the velocity in RPM of the pid
     * controller in Smart Motion mode - setSmartMotionMinOutputVelocity() will put
     * a lower bound in RPM of the pid controller in Smart Motion mode -
     * setSmartMotionMaxAccel() will limit the acceleration in RPM^2 of the pid
     * controller in Smart Motion mode - setSmartMotionAllowedClosedLoopError() will
     * set the max allowed error for the pid controller in Smart Motion mode
     * 
     * @param maxVelocity     the max velocity
     * @param minVelocity     the min velocity
     * @param maxAcceleration the maxAcceleration
     * @param allowedError    the allowed amount of error
     * @param id              the id for the pid (usually 0)
     * 
     */
    public void configureSmartMotion(double maxVelocity, double minVelocity, double maxAcceleration,
            double allowedError, int id) {
        pidController.setSmartMotionMaxVelocity(maxVelocity, id);
        pidController.setSmartMotionMinOutputVelocity(minVelocity, id);
        pidController.setSmartMotionMaxAccel(maxAcceleration, id);
        pidController.setSmartMotionAllowedClosedLoopError(allowedError, id);
    }

    public void setPosFactor(double factor) {
        sparkMaxEncoder.setPositionConversionFactor(factor);
    }

    @Override
    public double getVelocity() {
        return sparkMaxEncoder.getVelocity() * sparkMaxEncoder.getVelocityConversionFactor();
    } // returns velocity of motor

    /**
     * Get the velocity of the motor in meters
     * 
     * @param radius The radius (in meters) of the drive wheel
     * @return The velocity of the motor
     */
    public double getVelocityMeters(double radius) {
        return (2 * Math.PI * radius * getVelocity() / 60.0) / 4.0;
    }

    public void tareEncoder() {
        encoderZero = sparkMaxEncoder.getPosition();
    }

    public double getZero() {
        return encoderZero;
    }

    public double getAnalogValue() {
        // return (sparkMax.getAnalog(AnalogMode.kRelative).getPosition());
        return analogEncoder.getPosition() * analogEncoder.getPositionConversionFactor();

    }

    @Override
    public double getPosition() {
        return ((sparkMaxEncoder.getPosition() - encoderZero));
    } // returns position of motor

    public double getPositionConverted() {
        return ((sparkMaxEncoder.getPosition() - encoderZero) * sparkMaxEncoder.getPositionConversionFactor());
    } // returns motor position but converted by some factor

    public double getDegrees() {
        return getPosition() / sparkMaxEncoder.getCountsPerRevolution() * 360.0;
    }

    public double getCurrent() {
        return sparkMax.getOutputCurrent();
    }

    public void setAlternateEncoder() {
        // No params deprecated, default to 0
        alternateEncoder = sparkMax.getAlternateEncoder(0);
    }

    public void setAlternateEncoder(int n) {
        alternateEncoder = sparkMax.getAlternateEncoder(n);
    }

    public double getAlternateVelocity() {
        return alternateEncoder.getVelocity();
    }

    public double getAlternatePosition() {
        return alternateEncoder.getPosition();
    }

}
