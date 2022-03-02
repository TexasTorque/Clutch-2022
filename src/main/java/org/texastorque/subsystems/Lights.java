package org.texastorque.subsystems;

import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.constants.Constants;
import org.texastorque.constants.Ports;
import org.texastorque.inputs.AutoInput;
import org.texastorque.inputs.Feedback;
import org.texastorque.inputs.Input;
import org.texastorque.inputs.State;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.component.TorqueSparkMax;

/**
 * Replacment for ArduinoInterface using continuous
 * update loop.
 */
public class Lights extends TorqueSubsystem {
    private volatile static Lights instance = null;

    public static enum LightMode {
        NO_LIGHTS(false, false, false), // 000, Nothing
        RED_TELEOP(false, false, true), // 001, Solid red
        BLUE_TELEOP(false, true, false), // 010, Solid blue
        TARGET_LOCK(false, true, true), // 011, Solid green
        ENDGAME(true, false, false), // 100, Strobe rainbow
        SHOOTING(true, false, true), // 101, Flash green
        RED_AUTO(true, true, false), // 110, Flash red
        BLUE_AUTO(true, true, true); // 111, Flash blue

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

    DigitalOutput a;
    DigitalOutput b;
    DigitalOutput c;

    private LightMode lightMode = LightMode.NO_LIGHTS;

    // I was having bugs when i didn't check
    // if the DIO had already been set, I think
    // setting them too frequently breaks it
    // - Jacob (blame me if it ends up being useless)
    private boolean lightModeSet;

    private Lights() {
        lightModeSet = false;
        a = new DigitalOutput(Ports.ARDUINO_A);
        b = new DigitalOutput(Ports.ARDUINO_B);
        c = new DigitalOutput(Ports.ARDUINO_C);
    }

    public void resetTeleop() {
        this.setLightMode(State.getInstance().getAllianceColor().isRed()
                ? LightMode.RED_TELEOP
                : LightMode.BLUE_TELEOP);
    }

    public void resetAuto() {
        this.setLightMode(State.getInstance().getAllianceColor().isRed()
                ? LightMode.RED_AUTO
                : LightMode.BLUE_AUTO);
    }

    @Override
    public void updateTeleop() {
        if (Feedback.getInstance().isTurretAlligned() && Feedback.getInstance().getShooterFeedback().getRPM() != 0)
            this.setLightMode(LightMode.TARGET_LOCK);
        else if (Input.getInstance().getShooterInput().getFlywheel() != 0)
            this.setLightMode(LightMode.SHOOTING);
        else if (Input.getInstance().getClimberInput().getClimbHasStarted()) {
            System.out.println("set to endgame");
            this.setLightMode(LightMode.ENDGAME);
        } else
            resetTeleop();
    }

    @Override
    public void updateAuto() {
        if (Feedback.getInstance().isTurretAlligned() && Feedback.getInstance().getShooterFeedback().getRPM() != 0)
            this.setLightMode(LightMode.TARGET_LOCK);
        else if (AutoInput.getInstance().getFlywheelSpeed() != 0)
            this.setLightMode(LightMode.SHOOTING);
        else
            resetAuto();
    }

    @Override
    public void updateFeedbackTeleop() {
    }

    // Use this instead of just the variable!
    public void setLightMode(LightMode lightMode) {
        if (this.lightMode != lightMode) {
            this.lightMode = lightMode;
            lightModeSet = false;
        }
    }

    @Override
    public void output() {
        if (!lightModeSet) {
            a.set(lightMode.getA());
            b.set(lightMode.getB());
            c.set(lightMode.getC());
            lightModeSet = true;
        }
    }

    @Override
    public void updateSmartDashboard() {
    }

    @Override
    public void disable() {
        // set lights to solid
        resetTeleop();
        output();
    }

    public static synchronized Lights getInstance() {
        return instance == null ? instance = new Lights() : instance;
    }
}