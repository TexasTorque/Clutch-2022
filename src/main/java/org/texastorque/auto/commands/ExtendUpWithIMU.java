package org.texastorque.auto.commands;

import org.texastorque.constants.Constants;
import org.texastorque.inputs.AutoInput;
import org.texastorque.inputs.Feedback;
import org.texastorque.inputs.State;
import org.texastorque.inputs.State.AutoClimb;
import org.texastorque.subsystems.Climber.ClimberDirection;
import org.texastorque.torquelib.auto.TorqueCommand;

import edu.wpi.first.wpilibj.Timer;

/**
 * Extend up using the IMU to only extend at maximum pitch swing
 */
public class ExtendUpWithIMU extends TorqueCommand {
    private static double allowedError = 3;

    private double lastTime;
    private double lastPitch;
    private double lastPitchVelocity;
    private double lastPitchAcceleration;

    @Override
    protected void init() {
        State.getInstance().setAutoClimb(AutoClimb.ON);
        System.out.println("Extending with IMU!");
        lastTime = Timer.getFPGATimestamp();
        lastPitch = Feedback.getInstance().getGyroFeedback().getPitch();
        lastPitchVelocity = 0;
        lastPitchAcceleration = 0;
    }

    @Override
    protected void continuous() {
        double currentTime = Timer.getFPGATimestamp();
        double currentPitch = Feedback.getInstance().getGyroFeedback().getPitch();
        double dt = currentTime - lastTime;

        double currentPitchVelocity = (currentPitch - lastPitch) / dt;
        double currentPitchAcceleration = (currentPitchVelocity - lastPitchVelocity) / dt;

        if (Math.signum(currentPitchVelocity) == -1 && Math.signum(currentPitchAcceleration) == -1
                && Math.signum(lastPitchAcceleration) == -1) {
            AutoInput.getInstance().setClimberDirection(ClimberDirection.PUSH);
        }

        lastTime = currentTime;
        lastPitch = currentPitch;
        lastPitchVelocity = currentPitchVelocity;
        lastPitchAcceleration = currentPitchAcceleration;
    }

    @Override
    protected boolean endCondition() {
        return Math.abs(Feedback.getInstance()
                .getClimberFeedback()
                .getLeftPosition() -
                Constants.CLIMBER_LEFT_LIMIT_HIGH) < allowedError &&
                Math.abs(
                        Feedback.getInstance().getClimberFeedback().getRightPosition() -
                                Constants.CLIMBER_RIGHT_LIMIT_HIGH) < allowedError;

    }

    @Override
    protected void end() {
        System.out.println("IMU done :)");
        AutoInput.getInstance().setClimberDirection(ClimberDirection.STOP);
        State.getInstance().setAutoClimb(AutoClimb.OFF);
    }

}
