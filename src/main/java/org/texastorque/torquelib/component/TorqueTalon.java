package org.texastorque.torquelib.component;

import java.util.ArrayList;

import com.ctre.phoenix.ErrorCode;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import org.texastorque.util.KPID;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


// I plan on doing a rewrite of (at least) this motor controller
// Honestly might as well clean up TorqueLib and Utils... 
// - Justus

public class TorqueTalon extends TorqueMotor {
    private WPI_TalonSRX talon;
    private ArrayList<WPI_TalonSRX> talonFollowers = new ArrayList<>();
    private boolean invert = false;

    // ===================== constructor stuff =================
    public TorqueTalon(int port) {
        talon = new WPI_TalonSRX(port);
        // talon.configFactoryDefault();
        // talon.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative,
        // 0, 0);
        this.port = port;
    } // torque talon

    @Override
    public void addFollower(int port) {
        talonFollowers.add(new WPI_TalonSRX(port));
    } // add follower

    public void addFollower(int port, boolean invert) {
        WPI_TalonSRX t = new WPI_TalonSRX(port);
        t.setInverted(invert);
        talonFollowers.add(t);
            
    } // add follower

    public void setInverted(boolean set) {
        talon.setInverted(set);
    }

    public double getRPM() {
        try {
            return Math.abs((talon.getSelectedSensorVelocity() * 600.0) / 4096.0);
        } catch (Exception e) {
            System.out.println("No encoder present! :(  -- " + e);
            return 0;
        }
    }

    // ====================== set methods ==========================
    @Override
    public void set(double output) {
        talon.set(ControlMode.PercentOutput, output);
        for (WPI_TalonSRX talonSRX : talonFollowers) {
            talonSRX.set(ControlMode.Follower, port);
            //talonSRX.setInverted(invert);
            SmartDashboard.putNumber("FollowerVelocity", output);
        } // takes care of followers
    } // generic set method

    public void set(double output, ControlMode modeTalon) {
        talon.set(modeTalon, output);
        for (WPI_TalonSRX talonSRX : talonFollowers) {
            talonSRX.set(ControlMode.Follower, port);
        } // takes care of followers
    } // set with ControlMode for talon

    // ====================== pid stuff ==========================

    @Override
    public void configurePID(KPID kPID) {
        talon.config_kP(0, kPID.p());
        talon.config_kI(0, kPID.i());
        talon.config_kD(0, kPID.d());
        talon.config_kF(0, kPID.f());
        talon.configPeakOutputForward(kPID.max());
        talon.configPeakOutputReverse(kPID.min());
    } // configure PID

    @Override
    public void updatePID(KPID kPID) {
        talon.config_kP(0, kPID.p());
        talon.config_kI(0, kPID.i());
        talon.config_kD(0, kPID.d());
        talon.config_kF(0, kPID.f());
    } // update PID

    @Override
    public double getVelocity() {
        try {
            return talon.getSelectedSensorVelocity();
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("There is no encoder present, you need to put one in");
        }
        return 0;
    } // get position

    @Override
    public double getPosition() {
        try {
            return talon.getSelectedSensorPosition();
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("There is no encoder present, you need to put one in");
        }
        return 0;
    } // get position

    public double getOutput() {
        return talon.getMotorOutputPercent();
    }

    public void invertFollower() {
        invert = !invert;
    } // invert follower - flips the direction of the follower from what it was
      // previously, default direction is same as leader
} // Torque Talon