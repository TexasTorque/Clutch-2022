package org.texastorque.util.pid;

public interface IConfiguratorPlugin {
    void initialize();
    void updateGains(KPIDGains gains);
    void setOutputTarget(double speed);
}