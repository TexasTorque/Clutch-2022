package org.texastorque.torquelib.auto;

import java.util.HashMap;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class TorqueAutoManager {
    private static volatile TorqueAutoManager instance;;
    private HashMap<String, TorqueSequence> autoSequences;
    private SendableChooser<String> autoSelector = new SendableChooser<String>();

    private TorqueSequence currentSequence;
    private boolean sequenceEnded;

    private final String autoSelectorKey = "AutoList";

    public TorqueAutoManager(TorqueSequence... sequences) {
        autoSequences = new HashMap<String, TorqueSequence>();

        for (TorqueSequence sequence : sequences) {
            addSequence(sequence.getName(), sequence);
        }

        displayChoices();
    }

    private void addSequence(String name, TorqueSequence seq) {
        autoSequences.put(name, seq);

        if (autoSequences.size() == 0) {
            autoSelector.setDefaultOption(name, name);
        } else {
            autoSelector.addOption(name, name);
        }
    }

    public void runCurrentSequence() {
        if (currentSequence != null) {
            currentSequence.run();
            sequenceEnded = currentSequence.hasEnded(); // manage state of sequence
        } else {
            DriverStation.reportError("No auto selected!", false);
        }
    }

    public void chooseCurrentSequence() {
        String autoChoice = NetworkTableInstance.getDefault().getTable("SmartDashboard").getSubTable(autoSelectorKey)
                .getEntry("selected").getString("N/A");

        if (autoSequences.containsKey(autoChoice)) {
            System.out.println("Switching to auto: " + autoChoice);
            currentSequence = autoSequences.get(autoChoice);
        }

        resetCurrentSequence();
        sequenceEnded = false;
    }

    /**
     * Set sequence with sequence object
     */
    public void setCurrentSequence(TorqueSequence seq) {
        currentSequence = seq;
        resetCurrentSequence();
    }

    /**
     * Send sequence list to SmartDashboard
     */
    public void displayChoices() {
        SmartDashboard.putData(autoSelectorKey, autoSelector);
    }

    public void resetCurrentSequence() {
        if (currentSequence != null)
            currentSequence.reset();
    }

    /**
     * Return the state variable that shows whether the sequence is ended or not
     */
    public boolean getSequenceEnded() {
        return sequenceEnded;
    }

    /**
     * Get the AutoManager instance
     * 
     * @return AutoManager
     */
    public static synchronized TorqueAutoManager getInstance() {
        if (instance == null) {
            instance = new TorqueAutoManager();
        }
        return instance;
    }
}
