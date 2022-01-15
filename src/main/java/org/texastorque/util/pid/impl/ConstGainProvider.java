package org.texastorque.util.pid.impl;

import org.texastorque.util.pid.IGainProvider;
import org.texastorque.util.pid.KPIDGains;

public class ConstGainProvider implements IGainProvider {

    private final KPIDGains gains;

    public ConstGainProvider(double k, double p, double i, double d) {
        this.gains = new KPIDGains(k, p, i, d);
    }

    @Override
    public KPIDGains provide(double setpoint) {
        return this.gains;
    }
}