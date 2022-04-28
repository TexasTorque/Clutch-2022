package org.texastorque.auto;

import org.texastorque.torquelib.auto.*;

public class AutoManager extends TorqueAutoManager {
    private static volatile AutoManager instance;

    @Override
    public void init() {
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