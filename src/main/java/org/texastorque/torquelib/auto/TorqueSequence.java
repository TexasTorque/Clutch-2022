package org.texastorque.torquelib.auto;

import java.util.ArrayList;

public abstract class TorqueSequence {
    private ArrayList<TorqueBlock> commands = new ArrayList<TorqueBlock>();
    private boolean ended = false;
    private int blockIndex = 0;
    private String name = "unnamed";

    public TorqueSequence(String name) {
        this.name = name;
        init();
    }

    protected abstract void init();

    protected void addBlock(TorqueBlock block) {
        commands.add(block);
    }

    public void run() {
        if (blockIndex < commands.size()) {
            boolean blockEnded = true;
            for (TorqueCommand command : commands.get(blockIndex)) {
                if (!command.run())
                    blockEnded = false;
            }
            if (blockEnded)
                blockIndex++;
        } else if (!ended)
            ended = true;
    }

    public boolean hasEnded() {
        return ended;
    }

    public void reset() {
    };

    public String getName() {
        return name;
    }
}
