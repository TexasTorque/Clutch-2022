package org.texastorque.torquelib.auto;

import java.util.ArrayList;

/**
 * Basically a typedef to wrap ArrayList<TorqueCommand> with Block
 */
public class TorqueBlock extends ArrayList<TorqueCommand> {
    public TorqueBlock() { super(); }

    public TorqueBlock(TorqueCommand... commands) {
        for (TorqueCommand command : commands) {
            add(command);
        }
    }

    public void addCommand(TorqueCommand command) {
        add(command);
    }
}
