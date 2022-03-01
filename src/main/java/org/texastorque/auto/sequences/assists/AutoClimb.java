package org.texastorque.auto.sequences.assists;

import org.texastorque.auto.commands.ClimbToSetpoint;
import org.texastorque.auto.commands.Wait;
import org.texastorque.constants.Constants;
import org.texastorque.torquelib.auto.TorqueBlock;
import org.texastorque.torquelib.auto.TorqueSequence;

/**
 * Climb sequence for after initial lineup
 */
public class AutoClimb extends TorqueSequence {

    public AutoClimb() { init(); }

    @Override
    protected void init() {
        // We are starting with climber fully extended, over mid rung

        // Pull down on mid rung.
        addBlock(new TorqueBlock(
            new ClimbToSetpoint(Constants.CLIMBER_LEFT_LIMIT_LOW,
                                Constants.CLIMBER_RIGHT_LIMIT_LOW)));

        // Wait for 2 seconds to avoid over swinging
        addBlock(new TorqueBlock(new Wait(2)));

        // Bring climber up to high rung.
        addBlock(new TorqueBlock(
            new ClimbToSetpoint(Constants.CLIMBER_LEFT_LIMIT_HIGH,
                                Constants.CLIMBER_RIGHT_LIMIT_HIGH)));

        // Wait for 2 seconds to avoid over swinging
        addBlock(new TorqueBlock(new Wait(2)));

        // Bring climber down on high rung
        addBlock(new TorqueBlock(
            new ClimbToSetpoint(Constants.CLIMBER_LEFT_LIMIT_LOW,
                                Constants.CLIMBER_RIGHT_LIMIT_LOW)));

        // Wait for 2 second to avoid over swinging
        addBlock(new TorqueBlock(new Wait(2)));

        // Bring climber up to travesal rung.
        addBlock(new TorqueBlock(
            new ClimbToSetpoint(Constants.CLIMBER_LEFT_LIMIT_HIGH,
                                Constants.CLIMBER_RIGHT_LIMIT_HIGH)));

        // Wait for 2 seconds to avoid over swinging
        addBlock(new TorqueBlock(new Wait(2)));

        // Bring climber down on traversal rung
        addBlock(new TorqueBlock(
            new ClimbToSetpoint(Constants.CLIMBER_LEFT_LIMIT_HIGH / 2,
                                Constants.CLIMBER_RIGHT_LIMIT_HIGH / 2)));
    }
}