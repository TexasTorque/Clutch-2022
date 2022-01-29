package org.texastorque.auto;

import org.texastorque.auto.sequences.Example;
import org.texastorque.torquelib.auto.TorqueAutoManager;

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