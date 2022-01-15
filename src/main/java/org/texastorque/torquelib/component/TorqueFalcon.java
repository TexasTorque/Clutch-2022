package org.texastorque.torquelib.component;

import java.util.ArrayList;

import com.ctre.phoenix.ErrorCode;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonFXConfiguration;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatorCurrentLimitConfiguration;
import com.ctre.phoenix.motorcontrol.SupplyCurrentLimitConfiguration;
import com.ctre.phoenix.motorcontrol.TalonFXInvertType;

import org.texastorque.util.KPID;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * A class for controlling a Falcon 500. The Falcon 500 uses a TalonFX Motor
 * controller. I'm not extending TorqueMotor for a reason - it is over
 * abstraction that adds nothing. The Falcon 500 and TalonFX actually also add a
 * wealth of features that TorqueMotor is not equipped to handle without heavy
 * modification for standardization.
 * 
 * Resources:
 * https://github.com/CrossTheRoadElec/Phoenix-Examples-Languages/blob/master/Java%20Talon%20FX%20(Falcon%20500)/IntegratedSensor/src/main/java/frc/robot/Robot.java
 * http://www.ctr-electronics.com/downloads/pdf/Falcon%20500%20User%20Guide.pdf
 * 
 * 
 * @author Justus
 * @apiNote Created durring 2021 off-season
 */
public class TorqueFalcon {

    // TODO: Add TalonFXInvertType direction setting mode.
    // TODO: Discuss invert rules for followers and main motor.

    private final double kUnitsPerRev = 2048.;
    private final String encoderMissing = "[Falcon500] Encoder interface error!\n"
            + " - Encoder could be missing, but the\n" + "   Falcon 500 encoder is built in,\n"
            + "   so check that your Falcon 500 works.";
    private static final int timeoutMs = 30;
    private final int slotId = 0;

    private WPI_TalonFX falcon;
    private TalonFXConfiguration config;
    private ArrayList<WPI_TalonFX> followers = new ArrayList<>();
    private ArrayList<WPI_TalonFX> all = new ArrayList<>();
    private boolean invert = false;
    private int port;

    private NeutralMode neutralMode = NeutralMode.EEPROMSetting;

    /*
     * Constructors for main motor *
     */

    /**
     * Constructor for the main motor.
     * 
     * @param port The CAN ID port for the main motor.
     */
    public TorqueFalcon(int port) {
        this.port = port;
        falcon = new WPI_TalonFX(port);

        config = new TalonFXConfiguration();
        config.primaryPID.selectedFeedbackSensor = FeedbackDevice.IntegratedSensor;
        falcon.configAllSettings(config);
        falcon.configStatorCurrentLimit(new StatorCurrentLimitConfiguration(true, 30, 0, 0), timeoutMs);
        falcon.configSupplyCurrentLimit(new SupplyCurrentLimitConfiguration(true, 30, 0, 0), timeoutMs);
        falcon.setNeutralMode(neutralMode);
        all.add(falcon);
    }

    /**
     * Constructor for the main motor with neutral mode.
     * 
     * @param port        The CAN ID port for the main motor.
     * @param neutralMode The neutral mode setting for the main motor.
     */
    public TorqueFalcon(int port, NeutralMode neutralMode) {
        this.port = port;
        falcon = new WPI_TalonFX(port);

        config = new TalonFXConfiguration();
        config.primaryPID.selectedFeedbackSensor = FeedbackDevice.IntegratedSensor;
        falcon.configAllSettings(config);

        this.neutralMode = neutralMode;
        falcon.setNeutralMode(neutralMode);
    }

    /*
     * Handles addition of followers *
     */

    /**
     * Adds a follower to the main motor.
     * 
     * @param port The CAN ID port for the follower motor.
     */
    public void addFollower(int port) {
        WPI_TalonFX follower = new WPI_TalonFX(port);
        follower.setNeutralMode(neutralMode);
        followers.add(follower);
        all.add(follower);
    }

    /**
     * Adds a follower to the main motor and set it inverted.
     * 
     * @param port The CAN ID port for the follower motor.
     */
    public void addFollower(int port, boolean inverted) {
        WPI_TalonFX follower = new WPI_TalonFX(port);
        follower.setInverted(inverted);
        follower.setNeutralMode(neutralMode);
        followers.add(follower);
        all.add(follower);
    }

    /*
     * Handles inversions *
     */

    /**
     * Set main motor and followers to the same inversion.
     * 
     * @param invert True sets inverted, false sets not inverted.
     */
    public void setInverted(boolean invert) {
        falcon.setInverted(invert);
        for (WPI_TalonFX follower : followers) {
            follower.setInverted(TalonFXInvertType.FollowMaster);
        }
    }

    /**
     * Sets the invert of the main motor.
     * 
     * @param invert True sets inverted, false sets not inverted.
     */
    public void setMainInverted(boolean invert) {
        falcon.setInverted(invert);
    }

    /*
     * Handles neutral mode in brake etc. *
     */

    /**
     * Updates the neutral mode for main motor and followers to the current one
     * stored in the class.
     */
    private void updateNeutralMode() {
        falcon.setNeutralMode(neutralMode);
        for (WPI_TalonFX follower : followers) {
            follower.setNeutralMode(neutralMode);
        }
    }

    /**
     * Set the neutral mode for the class and apply changes to main motor and all
     * followers.
     * 
     * @param mode The neutral mode to set to.
     */
    public void setNeutralMode(NeutralMode mode) {
        neutralMode = mode;
        updateNeutralMode();
    }

    /**
     * Reset the neutral mode for class to default (read from motor controller
     * memory) (EEPROMSetting) and apply changes to main motor and all followers.
     */
    public void resetNeutralMode() {
        neutralMode = NeutralMode.EEPROMSetting;
        updateNeutralMode();
    }

    /**
     * Get the current number neutral mode stored in the class.
     * 
     * @return The current neutral mode
     */
    public NeutralMode getNeutralMode() {
        return neutralMode;
    }

    /*
     * Handles setting motor output *
     */

    /**
     * Sets the percent power output of the main motor and all followers. Percent
     * output is the default.
     * 
     * @param output Percent output from -1 (100% backwards) to 1 (100% forwards).
     *               Example: 50% = .5.
     */
    public void set(double output) {
        set(output, ControlMode.PercentOutput);
    }

    /**
     * Sets the output of the main motor and all followers with a specified control
     * mode.
     * 
     * @param output The value that the motor will be set to.
     * @param mode   The control mode to be used when setting motor output.
     */
    public void set(double output, ControlMode mode) {
        falcon.set(mode, output);
        for (WPI_TalonFX follower : followers) {
            follower.set(ControlMode.Follower, port);
            SmartDashboard.putNumber("FollowerVelocity", output);
        }
    }

    /*
     * Handles PID settings *
     */

    /**
     * Sets the main motor PID from a Torque KPID object.
     * 
     * @param kPID The Torque KPID object to set the main motor PID from.
     */
    public void configurePID(KPID kPID) {
        configurePIDHandler(falcon, kPID);
        for (WPI_TalonFX follower : followers) {
            configurePIDHandler(follower, kPID);
        }
    }

    private void configurePIDHandler(WPI_TalonFX falcon, KPID kPID) {
        falcon.config_kP(0, kPID.p());
        falcon.config_kI(0, kPID.i());
        falcon.config_kD(0, kPID.d());
        falcon.config_kF(0, kPID.f());
        falcon.configPeakOutputForward(kPID.max());
        falcon.configPeakOutputReverse(kPID.min());

    }

    /*
     * Feedback system *
     */

    /**
     * Get current motor output percent.
     * 
     * @return Motor output percent.
     */
    public double getOutput() {
        return falcon.getMotorOutputPercent();
    }

    /**
     * Get current motor position in kPosition.
     * 
     * @return Current motor position in kPosition.
     */
    public double getPosition() {
        try {
            return falcon.getSelectedSensorPosition();
        } catch (Exception e) {
            System.out.println(e);
            System.out.println(encoderMissing);
        }
        return 0;
    }

    /**
     * Set position to value
     * 
     * @param sensorPos Value
     */
    public void setPosition(double sensorPos) {
        try {
            falcon.setSelectedSensorPosition(sensorPos);
        } catch (Exception e) {
            System.out.println(e);
            System.out.println(encoderMissing);
        }
    }

    /**
     * Get current motor position in degrees.
     * 
     * @return Current motor position in degrees.
     */
    public double getPositionDegrees() {
        try {
            return falcon.getSelectedSensorPosition() / kUnitsPerRev * 360;
        } catch (Exception e) {
            System.out.println(e);
            System.out.println(encoderMissing);
        }
        return 0;
    }

    /**
     * Get current motor poition in rotation.
     * 
     * @return Current motor position in rotation.
     */
    public double getPositionRotations() {
        try {
            return falcon.getSelectedSensorPosition() / kUnitsPerRev;
        } catch (Exception e) {
            System.out.println(e);
            System.out.println(encoderMissing);
        }
        return 0;
    }

    /**
     * Get current motor velocity in 100 ticks per millisecond.
     * 
     * @return Current motor velocity in 100 ticks per millisecond.
     */
    public double getVelocity() {
        try {
            return falcon.getSelectedSensorVelocity();
        } catch (Exception e) {
            System.out.println(e);
            System.out.println(encoderMissing);
        }
        return 0;
    }

    /**
     * Get absolute value of the current motor velocity in 100 ticks per
     * millisecond.
     * 
     * @return Absolute value of the current motor velocity in 100 ticks per
     *         millisecond.
     */
    public double getAbsoluteVelocity() {
        try {
            return Math.abs(falcon.getSelectedSensorVelocity());
        } catch (Exception e) {
            System.out.println(e);
            System.out.println(encoderMissing);
        }
        return 0;
    }

    /**
     * Get current motor velocity in rotations per second.
     * 
     * @return Current motor velocity in rotations per second.
     */
    public double getVelocityRPS() {
        try {
            return falcon.getSelectedSensorVelocity() / kUnitsPerRev * 10.;
        } catch (Exception e) {
            System.out.println(e);
            System.out.println(encoderMissing);
        }
        return 0;
    }

    /**
     * Get absolute value of the current motor velocity in rotations per second.
     * 
     * @return Absolute value of the current motor velocity in rotations per second.
     */
    public double getAbsoluteVelocityRPS() {
        try {
            return Math.abs(falcon.getSelectedSensorVelocity() / kUnitsPerRev * 10.);
        } catch (Exception e) {
            System.out.println(e);
            System.out.println(encoderMissing);
        }
        return 0;
    }

    /**
     * Get current motor velocity in rotations per minuite.
     * 
     * @return Current motor velocity in rotations per minuite..
     */
    public double getVelocityRPM() {
        try {
            return falcon.getSelectedSensorVelocity() / kUnitsPerRev * 600.;
        } catch (Exception e) {
            System.out.println(e);
            System.out.println(encoderMissing);
            return 0;
        }
    }

    /**
     * Get absolute value of the current motor velocity in rotations per minuite.
     * 
     * @return Absolute value of the current motor velocity in rotations per
     *         minuite.
     */
    public double getAbsoluteVelocityRPM() {
        try {
            return Math.abs(falcon.getSelectedSensorVelocity() / kUnitsPerRev * 600.);
        } catch (Exception e) {
            System.out.println(e);
            System.out.println(encoderMissing);
            return 0;
        }
    }

    /**
     * Config motion magic
     * 
     * @param pid            pid
     * @param cruiseVelocity cruising velocity :)
     * @param acceleration   accel
     */
    public void configureMotionMagic(KPID pid, double cruiseVelocity, double acceleration) {
        all.forEach(f -> {
            f.selectProfileSlot(slotId, 0);
            f.config_kF(slotId, pid.f(), timeoutMs);
            f.config_kP(slotId, pid.p(), timeoutMs);
            f.config_kI(slotId, pid.i(), timeoutMs);
            f.config_kD(slotId, pid.d(), timeoutMs);
            f.configMotionCruiseVelocity(cruiseVelocity, timeoutMs);
            f.configMotionAcceleration(acceleration, timeoutMs);
        });
    }

    /**
     * Smoothing (S)
     * 
     * @param smoothing 0-8
     */
    public void configureSCurve(int smoothing) {
        all.forEach(f -> f.configMotionSCurveStrength(smoothing));
    }

    /**
     * @return Raw Talon object
     */
    public WPI_TalonFX getFalcon() {
        return falcon;
    }
}
