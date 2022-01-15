package org.texastorque.torquelib.component;

import org.texastorque.torquelib.util.TorqueClick;
import org.texastorque.torquelib.util.TorqueMathUtil;

/**
 * An implementation of the speedshifter used in 
 * some of the robots. 
 * 
 * @author Justus Omar Jack
 */
public class TorqueSpeedSettings {

    private TorqueClick clickUp = new TorqueClick();
    private TorqueClick clickDown = new TorqueClick();
    private TorqueClick clickMin = new TorqueClick();
    private TorqueClick clickMax = new TorqueClick();

    private final double maximum;
    private final double minimum;
    private final double increment;

    private double speed;

    /**
     * Creates a new TorqueSpeedSettings object with default increment of 0.1.
     * 
     * @param speed The initial speed setting.
     * @param minimum The maximum speed setting.
     * @param maximum The minimum speed setting.
     */
    public TorqueSpeedSettings(double speed, double minimum, double maximum) {
        this.speed = speed;
        this.minimum = minimum;
        this.maximum = maximum;
        this.increment = .1;
    }

    /**
     * Creates a new TorqueSpeedSettings object.
     * 
     * @param speed The initial speed setting.
     * @param minimum The maximum speed setting.
     * @param maximum The minimum speed setting.
     * @param increment The increment of the speed settings.
     */
    public TorqueSpeedSettings(double speed, double minimum, double maximum, double increment) {
        this.speed = speed;
        this.minimum = minimum;
        this.maximum = maximum;
        this.increment = increment;
    }

    /**
     * Updates (and optionally returns) the speed setting based on the controller input.
     * 
     * @param up The button mapped to incrementing speed.
     * @param down The button mapped to decrementing speed.
     * @param min The button mapped to setting speed to minimum.
     * @param max The button mapped to setting speed to maximum.
     * 
     * @return The current speed setting.
     */
    public double update(boolean up, boolean down, boolean min, boolean max) {
        if (clickUp.calc(up))
            speed = (double) TorqueMathUtil.constrain(speed + increment, minimum, maximum); 
        if (clickDown.calc(down))
            speed = (double) TorqueMathUtil.constrain(speed - increment, minimum, maximum); 

        if (clickMin.calc(min))
            speed = minimum;
        if (clickMax.calc(max))
            speed = maximum;

        return speed;
    }

    /**
     * Returns the current speed setting.
     * 
     * @return Current speed setting.
     */
    public double getSpeed() {
        return speed;
    }
}
