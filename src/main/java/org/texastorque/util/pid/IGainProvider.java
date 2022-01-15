package org.texastorque.util.pid;

public interface IGainProvider {
    KPIDGains provide(double setpoint);
}