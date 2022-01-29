package org.texastorque.auto;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import java.util.HashMap;
import org.texastorque.auto.sequences.*;
import org.texastorque.torquelib.auto.TorqueAutoManager;
import org.texastorque.torquelib.auto.TorqueSequence;

public class AutoManager extends TorqueAutoManager {
    private static volatile AutoManager instance;

    @Override
    public void init() {
        addSequence("Example", new Example("Example"));
    }

    /**
     * Get the AutoManager instance
     *
     * @return AutoManager
     */
    public static synchronized AutoManager getInstance() {
        return instance == null ? instance = new AutoManager() : instance;
    }
}