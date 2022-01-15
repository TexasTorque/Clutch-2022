package org.texastorque.torquelib.powershuffle;

import org.texastorque.torquelib.util.TorqueLogiPro;

import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.util.sendable.SendableBuilder;

public class PowerShuffleLogiPro extends TorqueLogiPro implements Sendable {

    private String name;

    public PowerShuffleLogiPro(int port) {
        super(port);
        name = "Logitech Pro (port " + port + ")";
    }

    public PowerShuffleLogiPro(int port, double deadband) {
        super(port, deadband);
        name = "Logitech Pro (port " + port + ")";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void pass(double d) {
        d += 0; // do nothing with it 
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        builder.addStringProperty("Name", this::getName, this::setName);
        builder.addDoubleProperty("Pitch", this::getPitch, this::pass);
        builder.addDoubleProperty("Roll", this::getRoll, this::pass);
        builder.addDoubleProperty("Yaw", this::getYaw, this::pass);
    }
}
