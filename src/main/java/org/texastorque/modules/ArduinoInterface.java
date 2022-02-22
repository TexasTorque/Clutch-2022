package org.texastorque.modules;

import org.texastorque.constants.Ports;
import org.texastorque.inputs.State;

import edu.wpi.first.wpilibj.DigitalOutput;

public class ArduinoInterface {
    private static volatile ArduinoInterface instance;

    private LightMode currentLightMode = LightMode.NO_LIGHTS;

    // No lights = 000
    // Set to red alliance = 001
    // Set to blue alliance = 010
    // Engage target lock = 011
    // Emgage endgame = 100
    // Awsome light pattern = 101
    // Not assigned = 110
    // Not assigned = 111
    public static enum LightMode {
        NO_LIGHTS(false, false, false),
        RED_ALLIANCE(false, false, true),
        BLUE_ALLIANCE(false, true, false),
        TARGET_LOCK(false, true, true),
        ENDGAME(true, false, false),
        AWESOME_LIGHTS(true, false, true),
        AUTO_LIGHTS(true, true, false),
        NOT_ASSIGNED_B(true, true, true);

        private final boolean a;
        private final boolean b;
        private final boolean c;

        LightMode(boolean a, boolean b, boolean c) {

            this.a = a;
            this.b = b;
            this.c = c;
        }

        public boolean getA() {
            return this.a;
        }

        public boolean getB() {
            return this.b;
        }

        public boolean getC() {
            return this.c;
        }
    }

    DigitalOutput a = new DigitalOutput(Ports.ARDUINO_A);
    DigitalOutput b = new DigitalOutput(Ports.ARDUINO_B);
    DigitalOutput c = new DigitalOutput(Ports.ARDUINO_C);

    private ArduinoInterface() {
    }

    public void setLightMode(LightMode mode) {
        this.currentLightMode = mode;
        this.a.set(mode.getA());
        this.b.set(mode.getB());
        this.c.set(mode.getC());
    }

    public void setToAllianceColor() {
        this.setLightMode(State.getInstance().getAllianceColor().isRed()
                ? LightMode.RED_ALLIANCE
                : LightMode.BLUE_ALLIANCE);
    }

    public LightMode getCurrentLightMode() {
        return this.currentLightMode;
    }

    public static synchronized ArduinoInterface getInstance() {
        return instance == null ? instance = new ArduinoInterface() : instance;
    }

}
