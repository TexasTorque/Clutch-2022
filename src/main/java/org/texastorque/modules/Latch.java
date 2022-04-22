package org.texastorque.modules;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Latch {

    // Construction Params
    private final double alpha;
    private final long deltaT;
    private final double jerkThresh;
    private final Timer timer;

    // State Variables
    private long lastUpdate = -1;
    private double lastAccelForJerk;
    private double lastAccel = -1.0;
    private volatile boolean triggered = false;
    private volatile boolean started = false;

    public Latch(
            final double alpha,
            final long deltaT,
            final double jerkThresh,
            final Timer timer) {
        this.alpha = alpha;
        this.deltaT = deltaT;
        this.jerkThresh = jerkThresh;
        this.timer = timer;

        this.reset();
    }

    public void update(double ax, double ay, double az) {
        // Don't waste CPU cycles when this class isn't in use.
        if (!started)
            return;

        final double currentAccelMagnitude = Math.sqrt(ax * ax + ay * ay + az * az);
        final long now = timer.currentTimeMillis();
        if (lastUpdate == -1) {
            this.lastUpdate = now;
            this.lastAccel = currentAccelMagnitude;
            return;
        }

        final long elapsed = now - lastUpdate;

        final double filteredAcceleration = currentAccelMagnitude * alpha + lastAccel * (1 - alpha);
        this.lastAccel = filteredAcceleration;

        if (elapsed >= this.deltaT) {
            // If we have a previous value, we can check for a trigger.
            if (lastAccelForJerk >= 0 && !triggered) {
                final double elapsedSeconds = (double) elapsed / 1000;
                final double jerk = Math.abs((filteredAcceleration - lastAccelForJerk) / elapsedSeconds);
                SmartDashboard.putNumber("jerk", jerk);
                this.triggered = (jerk > jerkThresh);
            }

            lastAccelForJerk = filteredAcceleration;
            SmartDashboard.putNumber("lastAccelForJerk", lastAccelForJerk);
        }
    }

    public synchronized void start() {
        this.started = true;
    }

    public synchronized void reset() {
        this.started = false;
        this.lastUpdate = -1;
        this.lastAccelForJerk = -1;
        this.triggered = false;
    }

    public synchronized boolean didJerk() {
        return this.triggered;
    }

    public interface Timer {
        long currentTimeMillis();
    }
}
