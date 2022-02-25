package org.texastorque.auto;

import org.texastorque.auto.sequences.BluLeft;
import org.texastorque.auto.sequences.BluRight1;
import org.texastorque.auto.sequences.BluRight2;
import org.texastorque.auto.sequences.Example;
import org.texastorque.auto.sequences.PathTuning;
import org.texastorque.auto.sequences.PathplannerSequence;
import org.texastorque.torquelib.auto.TorqueAutoManager;

public class AutoManager extends TorqueAutoManager {
    private static volatile AutoManager instance;

    @Override
    public void init() {
        addSequence("Example", new Example("Example"));
        addSequence("Test1", new PathplannerSequence("test1"));
        addSequence("bluleft", new BluLeft("BluLeft"));
        addSequence("bluright1", new BluRight1("BluRight1"));
        addSequence("bluright2", new BluRight2("BluRight2"));
        addSequence("pathtuning", new PathTuning());
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