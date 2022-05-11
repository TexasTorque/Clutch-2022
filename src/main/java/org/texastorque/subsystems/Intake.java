package org.texastorque.subsystems;

import org.texastorque.Ports;
import org.texastorque.torquelib.base.TorqueSubsystemState;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.motors.TorqueSparkMax;
import org.texastorque.torquelib.util.KPID;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The intake subsystem. 
 * 
 * @author Jack Pittenger
 * @author Justus Languell
 */
public final class Intake extends TorqueSubsystem {
    private static volatile Intake instance;

    public enum IntakeDirection implements TorqueSubsystemState {
        INTAKE(-1),
        STOPPED(0),
        OUTAKE(.3);

        private final double direction;

        IntakeDirection(final double direction) {
            this.direction = direction;
        }

        public double getDirection() {
            return direction;
        }
    }

    public static enum IntakePosition implements TorqueSubsystemState {
        CLIMB(.25),
        UP(1.5),
        PRIME(4.4),
        DOWN(8.3);

        private final double position;

        IntakePosition(final double position) {
            this.position = position;
        }

        public double getPosition() {
            return position;
        }
    }

    private static final double ROTARY_MIN_SPEED = -.35;
    private static final double ROTARY_MAX_SPEED = .35;
    private static final double ROLLER_MIN_SPEED = .8;
    private static final double ROLLER_MAX_SPEED = 1;

    private IntakeDirection direction;
    private IntakePosition position;

    private TorqueSparkMax rotary, rollers;
    private DigitalInput limitSwitch;

    private Intake() {
        rotary = new TorqueSparkMax(Ports.INTAKE.ROTARY);
        rotary.configurePID(new KPID(0.1, 0.00005, .00002, 0,
                ROTARY_MIN_SPEED, ROTARY_MAX_SPEED));
        rotary.configurePositionalCANFrame();
        rotary.burnFlash();

        rollers = new TorqueSparkMax(Ports.INTAKE.ROLLER.LEFT);
        // rollers.addFollower(Ports.INTAKE.ROLLER.RIGHT);
        // rollers.configureDumbLeaderCANFrame();
        rollers.burnFlash();

        limitSwitch = new DigitalInput(Ports.INTAKE.SWITCH);
    }

    public void setState(final IntakeDirection direction, final IntakePosition position) {
        this.direction = direction;
        this.position = position;
    }

    public void setDirection(final IntakeDirection direction) {
        this.direction = direction;
    }

    public void setPosition(final IntakePosition position) {
        this.position = position;
    }

    public IntakeDirection getDirection() {
        return direction;
    }

    public IntakePosition getPosition() {
        return position;
    }

    public boolean isIntaking() {
        return direction == IntakeDirection.INTAKE && position == IntakePosition.DOWN;
    }

    @Override
    public void initTeleop() {
    }

    @Override
    public void updateTeleop() {
        // Needs climber logic
        // if (limitSwitch.get() && position.getPosition() >= rotary.getPosition()) 
        //     rotary.setCurrent(.1);
        // else
        //     rotary.setPosition(position.getPosition());

        rollers.setPercent(direction.getDirection());

        TorqueSubsystemState.logState(direction);
        TorqueSubsystemState.logState(position);

        SmartDashboard.putNumber("Rotary Position", rotary.getPosition());
       
        // Amazing one liner (;
        // rollers.setPercent(direction.getDirection() != 0 
        //         ? -Math.min(
        //                 (Math.sqrt(
        //                         Math.pow(Drivebase.getInstance().getSpeeds().vxMetersPerSecond, 2)
        //                         + Math.pow(Drivebase.getInstance().getSpeeds().vyMetersPerSecond, 2)
        //                 ) / Drivebase.DRIVE_MAX_TRANSLATIONAL_SPEED) 
        //                 * (ROLLER_MAX_SPEED - ROLLER_MIN_SPEED) 
        //                 + ROLLER_MIN_SPEED, ROLLER_MAX_SPEED) 
        //         : direction.getDirection());

        // The above one liner but readable
        // final double xSpeed = Drivebase.getInstance().getSpeeds().vxMetersPerSecond;
        // final double ySpeed = Drivebase.getInstance().getSpeeds().vyMetersPerSecond;
        // final double speed = Math.sqrt(Math.pow(xSpeed, 2) + Math.pow(ySpeed, 2));
        // final double percent = speed / Drivebase.DRIVE_MAX_ROTATIONAL_SPEED;
        // final double intake = Math.min(percent * (ROLLER_MAX_SPEED - ROLLER_MIN_SPEED) + ROLLER_MIN_SPEED, ROLLER_MAX_SPEED);
        // rollers.setPercent(direction.getDirection() > 0 ? intake : -direction.getDirection());
    }

    @Override
    public void initAuto() {
        
        
    }

    @Override
    public void updateAuto() {
        if (limitSwitch.get() && position.getPosition() >= rotary.getPosition()) 
            rotary.setCurrent(.1);
        else
            rotary.setPosition(position.getPosition());
        
        rollers.setPercent(direction.getDirection());
    }

    public static synchronized Intake getInstance() {
        return instance == null ? instance = new Intake() : instance;
    }
}
