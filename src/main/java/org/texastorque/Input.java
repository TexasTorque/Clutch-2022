package org.texastorque;

import org.texastorque.torquelib.base.TorqueInputManager;
import org.texastorque.torquelib.util.GenericController;

public class Input extends TorqueInputManager {
    private static volatile Input instance;

    private Input() {
        driver = new GenericController(0, 0.1);
        operator = new GenericController(1, 0.1);
    }

    @Override
    public void update() {
    }

    public static synchronized Input getInstance() {
        return instance == null ? instance = new Input() : instance;
    }
}
