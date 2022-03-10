package org.texastorque.subsystems;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.Timer;
import org.texastorque.constants.Ports;
import org.texastorque.inputs.AutoInput;
import org.texastorque.inputs.Feedback;
import org.texastorque.inputs.Input;
import org.texastorque.inputs.State;
import org.texastorque.torquelib.base.TorqueSubsystem;

/**
 * Controls the LEDs on the robot using the AddressableLED
 * API. If we add more lights, we can make a TorqueLib
 * abstraction to manage that better.
 * 
 * @author Justus, Omar, Jacob
 */
public class Lights extends TorqueSubsystem {

    private volatile static Lights instance = null;

    private AddressableLED leds;
    private AddressableLEDBuffer buffer;

    private double last;
    private boolean flashState;
    private int rainbowHue;

    private Lights() {
        leds = new AddressableLED(Ports.LIGHTS);
        buffer = new AddressableLEDBuffer(59); // ?
        leds.setLength(buffer.getLength());
        leds.setData(buffer);
        leds.start();

        last = Timer.getFPGATimestamp();
        flashState = false;
        rainbowHue = 0;

        defaultTeleop();
    }

    /**
     * Sets lights solid
     * 
     * @param r Red
     * @param g Green
     * @param b Blue 
     */
    private void setLights(int r, int g, int b) {
        for (int i = 0; i < buffer.getLength(); i++)
            buffer.setRGB(i, r, g, b);
    }

    /**
     * Sets lights flashing based on the period
     * 
     * @param r Red
     * @param g Green
     * @param b Blue 
     * @param period Time delay (seconds)
     */
    private void setLights(int r, int g, int b, double period) {
        double now = Timer.getFPGATimestamp();
        if (now - last > period) {
            last = now;
            flashState = !flashState;
        }

        if (flashState)
            setLights(r, g, b);
        else
            setLights(0, 0, 0);
    }

    /**
     * fun function for an endgame rainbow effect
     */
    private void setRainbow() {
        for (int i = 0; i < buffer.getLength(); i++)
            buffer.setHSV(i, (rainbowHue + 
                    (i * 180 / buffer.getLength())) % 180, 255, 128);
        rainbowHue = (rainbowHue + 3) % 180;
    }

    @Override
    public void updateTeleop() {
        if (Feedback.getInstance().isTurretAlligned() && Feedback.getInstance().getShooterFeedback().getRPM() != 0)
            // TARGET LOCK
            setLights(0, 255, 0);
        else if (Input.getInstance().getShooterInput().getFlywheel() != 0)
            // SHOOTING
            setLights(0, 255, 0, .25);
        else if (Input.getInstance().getClimberInput().hasClimbStarted())
            // ENDGAME RAINBOW
            setRainbow();
        else
            defaultTeleop();
    }

    @Override
    public void updateAuto() {
        if (Feedback.getInstance().isTurretAlligned() && Feedback.getInstance().getShooterFeedback().getRPM() != 0)
            // TARGET LOCK
            setLights(0, 255, 0);
        else if (AutoInput.getInstance().getFlywheelSpeed() != 0)
            // SHOOTING
            setLights(0, 255, 0, .25);
        else
            defaultAuto();
    }

    public void defaultTeleop() {
        if (State.isRedAlliance())
            setLights(255, 0, 0);
        else
            setLights(0, 0, 255);
    }

    public void defaultAuto() {
        if (State.isRedAlliance())
            setLights(255, 0, 0, .25);
        else
            setLights(0, 0, 255, .25);
    }

    @Override
    public void output() {
        leds.setData(buffer);
    }

    @Override
    public void updateSmartDashboard() {
    }

    @Override
    public void disable() {
        // set lights to solid
        defaultTeleop();
        output();
    }

    public static synchronized Lights getInstance() {
        return instance == null ? instance = new Lights() : instance;
    }
}