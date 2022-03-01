package org.texastorque.auto.commands;

import edu.wpi.first.wpilibj.Timer;
import org.texastorque.torquelib.auto.TorqueCommand;

public class Wait extends TorqueCommand {
    private double time, start;

    /**
     * @param time The time the robot will go for.
     */
    public Wait(double time) { this.time = time; }

    @Override
    public void init() {
        start = Timer.getFPGATimestamp();
    }

    @Override
    public void continuous() {}

    @Override
    public boolean endCondition() {
        return (Timer.getFPGATimestamp() - start) > time;
    }

    @Override
    public void end() {}
}