package org.texastorque.auto;

import org.texastorque.auto.sequences.mode0.*;
import org.texastorque.auto.sequences.mode1.*;
import org.texastorque.auto.sequences.mode2.*;
import org.texastorque.auto.sequences.mode3.*;
import org.texastorque.auto.sequences.mode4.*;
import org.texastorque.auto.sequences.mode5.*;
import org.texastorque.auto.sequences.mode6.*;
import org.texastorque.torquelib.auto.*;

public class AutoManager extends TorqueAutoManager {
    private static volatile AutoManager instance;

    @Override
    public void init() {
        addSequence("Mode 0", new Mode0("Mode 0"));

        addSequence("Mode 1 Center Left", new Mode1CenterLeft(""));
        addSequence("Mode 1 Center Right", new Mode1CenterRight(""));
        addSequence("Mode 1 Left", new Mode1Left(""));
        addSequence("Mode 1 Right", new Mode1Right(""));

        addSequence("Mode 2 Center Left", new Mode2CenterLeft(""));
        addSequence("Mode 2 Center Right", new Mode2CenterRight(""));
        addSequence("Mode 2 Left", new Mode2Left(""));
        addSequence("Mode 2 Right", new Mode2Right(""));

        addSequence("Mode 3 Center Right", new Mode3CenterRight(""));
        addSequence("Mode 3 Left", new Mode3Left(""));
        addSequence("Mode 3 Right", new Mode3Right(""));

        addSequence("Mode 4 Left", new Mode4Left(""));
        addSequence("Mode 4 Center Right", new Mode4CenterRight(""));
        addSequence("Mode 4 Far Right", new Mode4FarRight(""));

        addSequence("Mode 5 Center Right", new Mode5CenterRight(""));

        addSequence("Mode 6 Left", new Mode6Left(""));
        addSequence("Mode 6 Right", new Mode6Right(""));
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