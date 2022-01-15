package org.texastorque.util.pid.impl;

import org.texastorque.util.pid.IGainProvider;
import org.texastorque.util.pid.KPIDGains;

public class StepWiseGainProvider implements IGainProvider {

    public StepWiseGainProvider() {
        // TODO: Implement
    }

    @Override
    public KPIDGains provide(double setpoint) {
        return new KPIDGains(0, 0, 0, 0);
    }
}