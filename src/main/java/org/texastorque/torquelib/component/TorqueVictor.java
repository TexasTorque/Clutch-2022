package org.texastorque.torquelib.component;

import java.util.ArrayList;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

public class TorqueVictor extends VictorSPX {
    private ArrayList<VictorSPX> followers = new ArrayList<VictorSPX>();

    public TorqueVictor(int port) {
        super(port);
    }

    public TorqueVictor(int port, boolean inverted) {
        super(port);
        setInverted(inverted);
    }

    public void set(double val) {
        set(ControlMode.PercentOutput, val);
        followers.forEach(v -> v.set(ControlMode.PercentOutput, val));
    }

    public void addFollower(int port) {
        followers.add(new VictorSPX(port));
    }
}