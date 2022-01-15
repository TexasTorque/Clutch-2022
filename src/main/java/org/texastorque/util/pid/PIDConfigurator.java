package org.texastorque.util.pid;

public class PIDConfigurator {

    private final IGainProvider gainProvider;
    private final IConfiguratorPlugin configuratorPlugin;

    private double lastSetpoint = Double.NEGATIVE_INFINITY;

    public PIDConfigurator(IGainProvider gainProvider, IConfiguratorPlugin configuratorPlugin) {
        this.gainProvider = gainProvider;
        this.configuratorPlugin = configuratorPlugin;

        this.configuratorPlugin.initialize();
    }

    public void update(double setpoint) {
        // We don't want to update anything if the setpoint is the same as the previous.
        // We also want to ignore the setpoint if it is set to negative infinity.
        // Not just because it makes no sense, but because we use this value as the default.
        // Essentially negative infinity means that we have never actually chosen a setpoint.
        if ((lastSetpoint == setpoint) || (setpoint == Double.NEGATIVE_INFINITY)) return;

        var gains = gainProvider.provide(setpoint);
        this.configuratorPlugin.updateGains(gains);

        this.lastSetpoint = setpoint;
    }

    /**
     * Used to update motor controllers using the 
     * @param output The output we want to reach.
     */
    public void setTarget(double output) {
        this.configuratorPlugin.setOutputTarget(output);
    }
}