package org.texastorque.subsystems;

import com.revrobotics.CANSparkMax.ControlType;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.constants.Constants;
import org.texastorque.constants.Ports;
import org.texastorque.inputs.AutoInput;
import org.texastorque.inputs.Input;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.component.TorqueSparkMax;
import org.texastorque.util.KPID;

public class Intake extends TorqueSubsystem {
    private volatile static Intake instance = null;

    public static enum IntakeDirection {
        INTAKE(1),
        STOPPED(0),
        OUTAKE(-1);

        private final int direction;

        IntakeDirection(int direction) {
            this.direction = direction;
        }

        public int getDirection() {
            return direction;
        }
    }

    public static enum IntakePosition {
        UP(1.5),
        PRIME(4.4),
        DOWN(8.5);
        // Intake setpoints

        private final double position;

        IntakePosition(double position) {
            this.position = position;
        }

        public double getPosition() {
            return position;
        }
    }

    private TorqueSparkMax rotary;
    private TorqueSparkMax roller;

    private DigitalInput limitSwitch;

    private IntakePosition rotarySetPoint = IntakePosition.PRIME;
    private double rollerSpeed;

    private Intake() {
        rotary = new TorqueSparkMax(Ports.INTAKE_ROTARY);
        rotary.configurePID(new KPID(0.1, 0.00005, .00002, 0,
                Constants.INTAKE_ROTARY_MIN_SPEED,
                Constants.INTAKE_ROTARY_MAX_SPEED));
        roller = new TorqueSparkMax(Ports.INTAKE_ROLLER);
        limitSwitch = new DigitalInput(Ports.ROTARY_LIMIT_SWITCH);
    }

    @Override
    public void updateTeleop() {
        rotarySetPoint = Input.getInstance().getIntakeInput().getPosition();
        rollerSpeed = -Input.getInstance()
                .getIntakeInput()
                .getDirection()
                .getDirection() *
                Constants.INTAKE_ROLLER_SPEED;
    }

    @Override
    public void updateAuto() {
        rotarySetPoint = AutoInput.getInstance().getIntakePosition();
        rollerSpeed = -AutoInput.getInstance().getIntakeSpeed().getDirection() *
                Constants.INTAKE_ROLLER_SPEED;
    }

    @Override
    public void updateFeedbackTeleop() {
    }

    @Override
    public void output() {
        // We are at the bottom, staph!
        if (limitSwitch.get() && rotarySetPoint == IntakePosition.DOWN) {
            SmartDashboard.putBoolean("rotary running", false);
            rotary.set(0);
        } else {
            SmartDashboard.putBoolean("rotary running", true);
            rotary.set(rotarySetPoint.getPosition(), ControlType.kPosition);
        }
        roller.set(rollerSpeed);
    }

    @Override
    public void updateSmartDashboard() {
        SmartDashboard.putNumber("[Intake]Roller Speed", rollerSpeed);
        SmartDashboard.putNumber("[Intake]Rotary Position",
                rotary.getPosition());
        SmartDashboard.putNumber("[Intake]Rotary Set Point",
                rotarySetPoint.getPosition());
        SmartDashboard.putBoolean("[Intake]Limit switch", limitSwitch.get());
    }

    public static synchronized Intake getInstance() {
        return instance == null ? instance = new Intake() : instance;
    }
}