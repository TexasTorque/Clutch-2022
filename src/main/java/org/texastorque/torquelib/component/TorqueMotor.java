package org.texastorque.torquelib.component;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import org.texastorque.util.KPID;

import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.motorcontrol.VictorSP;
import edu.wpi.first.math.controller.PIDController;

import java.util.ArrayList;

public abstract class TorqueMotor {
	private ControllerType type;

	public enum ControllerType {
		VICTOR, TALONSRX, SPARKMAX;
	}

	private SpeedController victor;
	private TalonSRX talon;
	private CANSparkMax spark;

	private CANEncoder sparkEncoder;

	public int port;
	public boolean invert;
	private double minOutput;
	private double maxOutput;
	private boolean currentLimit;

	// ----------------- Constructor -----------------

	// ------------------ Set Methods ------------------
	//for setting raw outputs to all kinds of motors
	public abstract void set(double output);

	// add another method in each class that extends this that takes in the parameter of what control mode / type 

	// ----------------------------- Followers --------------------------
	private ArrayList<TalonSRX> talonFollowers;
	private ArrayList<CANSparkMax> sparkMaxFollowers;

	public abstract void addFollower(int port);
	
	// ----------------------------- PID Stuff ----------------------------
	private PIDController talonPID;
	private CANPIDController sparkPID;
	public abstract void configurePID(KPID kPID);

	public abstract void updatePID(KPID kPID);

	// ----------------------- Encoder Stuff ---------------------

	public abstract double getVelocity();

	public abstract double getPosition();

	// ================ Other Stuff =====================
	public void invertFollower(){
		invert = !invert;
	} // invert follower - flips the direction of the follower from what it was previously, default direction is same as leader 

} // TorqueMotor 