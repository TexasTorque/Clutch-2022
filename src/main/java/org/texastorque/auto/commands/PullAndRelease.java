package org.texastorque.auto.commands;

import org.texastorque.inputs.Input;
import org.texastorque.subsystems.Climber.ClimberDirection;
import org.texastorque.subsystems.Climber.ServoDirection;
import org.texastorque.torquelib.auto.TorqueCommand;

import edu.wpi.first.wpilibj.Timer;

public class PullAndRelease extends TorqueCommand {

    private Timer timer;
    private double totalTime;
    private double timeToRelease;

    /**
     * @param timeToRun     # of seconds to run command
     * @param timeToRelease # of seconds after start to release hooks
     */
    public PullAndRelease(double timeToRun, double timeToRelease) {
        this.totalTime = timeToRun;
        this.timeToRelease = timeToRelease;
    }

    @Override
    protected void init() {
        timer = new Timer();
        timer.start();
    }

    @Override
    protected void continuous() {
        Input.getInstance().getClimberInput().setClimberDirection(ClimberDirection.PULL);
        if (timer.hasElapsed(timeToRelease))
            Input.getInstance().getClimberInput().setServoDirection(ServoDirection.DETACH);
    }

    @Override
    protected boolean endCondition() {
        return timer.hasElapsed(totalTime);
    }

    @Override
    protected void end() {
        Input.getInstance().getClimberInput().setClimberDirection(ClimberDirection.STOP);
        Input.getInstance().getClimberInput().setServoDirection(ServoDirection.ATTACH);
    }

}
