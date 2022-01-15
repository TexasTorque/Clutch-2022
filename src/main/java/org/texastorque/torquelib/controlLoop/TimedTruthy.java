package org.texastorque.torquelib.controlLoop;

import edu.wpi.first.wpilibj.Timer;

/**
 * Concurrent timer for truthy values.
 */
public class TimedTruthy {
    private double time = 0;
    private double currTime = 0;

    /**
     * Update time
     * 
     * @param t
     */
    public void setTime(double t) {
        time = Math.max(t, time);
        currTime = Timer.getFPGATimestamp();
    }

    /**
     * @return if time not expired
     */
    public boolean calc() {
        if (time <= 0 || currTime <= 0)
            return false;
        double xTime = Timer.getFPGATimestamp();
        time -= xTime - currTime;
        currTime = xTime;
        return time > 0;
    }

}
