package org.texastorque.torquelib.controlLoop;

import edu.wpi.first.wpilibj.Timer;

/**
 * This class implements a slew rate limiter. In a nutshell, this limits the
 * rate-of-change of the units to avoid sharp movement acceleration while still
 * providing fine control.
 *
 * @author Jack Pittenger
 */
public class TorqueSlewLimiter {
    private final double limitAsc;
    private final double limitDesc;

    private double lastVal;
    private double lastTime;

    /**
     * Creates a new TorqueSlewLimiter with the ascending and descending limit the
     * same.
     * 
     * @param limit The max units-per-second
     */
    public TorqueSlewLimiter(double limit) {
        this.limitAsc = limit;
        this.limitDesc = limit;
    }

    /**
     * Creates a new TorqueSlewLimiter with a different ascending and descending
     * limit.
     * 
     * @param limitAsc  The max units-per-second increasing absolutely
     * @param limitDesc The max units-per-second descending absolutely
     */
    public TorqueSlewLimiter(double limitAsc, double limitDesc) {
        this.limitAsc = limitAsc;
        this.limitDesc = limitDesc;
    }

    /**
     * @param val The requested input
     * @return The limited value
     */
    public double calculate(double val) {
        double t = Timer.getFPGATimestamp();
        double dt = t - lastTime;
        double dx = val - lastVal;

        if (Math.abs(val) - Math.abs(lastVal) >= 0) {
            lastVal += Math.max(Math.min(dx, limitAsc * dt), -limitAsc * dt);
        } else {
            lastVal += Math.max(Math.min(dx, limitDesc * dt), -limitDesc * dt);
        }

        lastTime = t;

        return lastVal;
    }
}
