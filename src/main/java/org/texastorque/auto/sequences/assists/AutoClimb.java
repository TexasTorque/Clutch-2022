package org.texastorque.auto.sequences.assists;

import org.texastorque.auto.commands.PullUntillLatch;
import org.texastorque.auto.commands.ClimbToSetpoint;
import org.texastorque.auto.commands.ExtendUpWithIMU;
import org.texastorque.auto.commands.SetClimberServos;
import org.texastorque.auto.commands.ShreyasApproval;
import org.texastorque.auto.commands.Wait;
import org.texastorque.constants.Constants;
import org.texastorque.subsystems.Climber.ServoDirection;
import org.texastorque.torquelib.auto.TorqueBlock;
import org.texastorque.torquelib.auto.TorqueSequence;

/**
 * Climb sequence for after initial lineup
 */
public class AutoClimb extends TorqueSequence {

    public AutoClimb() {
        init();
    }

    @Override
    protected void init() {
        // We are starting with climber fully extended, over mid rung

        // Detach servo
        addBlock(new TorqueBlock(new SetClimberServos(ServoDirection.DETACH)));

        // Climb to half point
        addBlock(new TorqueBlock(
                new ClimbToSetpoint(Constants.CLIMBER_LEFT_LIMIT_HIGH / 2, Constants.CLIMBER_RIGHT_LIMIT_HIGH / 2)));

        // Get shreyas approval :)
        addBlock(new TorqueBlock(new ShreyasApproval()));

        // Hook to mid bar
        addBlock(new TorqueBlock(new PullUntillLatch()));

        // Extend to intermediate point on high bar
        addBlock(new TorqueBlock(
                new ClimbToSetpoint(Constants.CLIMBER_LEFT_LIMIT_HIGH / 2, Constants.CLIMBER_RIGHT_LIMIT_HIGH / 2)));

        // Extend out with the IMU
        addBlock(new TorqueBlock(new ExtendUpWithIMU()));

        // Get shreyas approval :)
        addBlock(new TorqueBlock(new ShreyasApproval()));

        // Get the climber hooked
        addBlock(new TorqueBlock(new PullUntillLatch()));

    }
}