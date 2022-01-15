package org.texastorque.torquelib.controlLoop;

public class LowPassFilter {

    private static final double MIN_ALPHA = 1e-3;

    private double lastValue;
    private final double alpha;

    public LowPassFilter(double alpha) {
        this.alpha = Math.max(MIN_ALPHA, Math.min(alpha, 1));
    } 

    public double filter(double value) {
        this.lastValue = value * alpha + lastValue * (1 - alpha);
        return this.lastValue;
    }

    public void clear(){
        this.lastValue = 0;
    }
}