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
        NO_LIGHTS(false, false, false),  // 000, Nothing
        RED_TELEOP(false, false, true),  // 001, Solid red
        BLUE_TELEOP(false, true, false), // 010, Solid blue
        TARGET_LOCK(false, true, true),  // 011, Solid green
        ENDGAME(true, false, false),     // 100, Strobe rainbow
        SHOOTING(true, false, true),     // 101, Flash green
        RED_AUTO(true, true, false),     // 110, Flash red
        BLUE_AUTO(true, true, true);     // 111, Flash blue

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

    private Lights() {
        a = new DigitalOutput(Ports.ARDUINO_A);
        b = new DigitalOutput(Ports.ARDUINO_B); 
        c = new DigitalOutput(Ports.ARDUINO_C);
    }

    public void resetTeleop() {
        lightMode = State.getInstance().getAllianceColor().isRed()
                ? LightMode.RED_TELEOP
                : LightMode.BLUE_TELEOP;
    }

    public void resetAuto() {
        lightMode = State.getInstance().getAllianceColor().isRed()
                ? LightMode.RED_AUTO
                : LightMode.BLUE_AUTO;
    } 

    @Override
    public void updateTeleop() {
        if (Feedback.getInstance().isTurretAlligned())
            lightMode = LightMode.TARGET_LOCK;
        else if (Input.getInstance().getShooterInput().getFlywheel() != 0)
            lightMode = LightMode.SHOOTING;
        else if (Input.getInstance().getClimberInput().getClimbHasStarted())
            lightMode = LightMode.ENDGAME;
        else resetTeleop();     
    }

    @Override
    public void updateAuto() {
        if (Feedback.getInstance().isTurretAlligned())
            lightMode = LightMode.TARGET_LOCK;
        else if (AutoInput.getInstance().getFlywheelSpeed() != 0)
            lightMode = LightMode.SHOOTING; 
        else resetAuto(); 
    }

    @Override
    public void updateFeedbackTeleop() {
    }

    @Override
    public void output() {
        a.set(lightMode.getA());
        b.set(lightMode.getB());
        c.set(lightMode.getC());
    }

    @Override
    public void updateSmartDashboard() {
        
    }

    public static synchronized Lights getInstance() {
        return instance == null ? instance = new Lights() : instance;
    }
}