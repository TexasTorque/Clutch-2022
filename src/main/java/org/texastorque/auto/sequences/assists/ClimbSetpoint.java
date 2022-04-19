package org.texastorque.auto.sequences.assists;

import org.texastorque.auto.commands.PullUntillLatch;
import org.texastorque.auto.commands.ClimbToSetpoint;
import org.texastorque.auto.commands.ExtendUpWithIMU;
import org.texastorque.auto.commands.SetClimberServos;
import org.texastorque.auto.commands.ShreyasApproval;
import org.texastorque.constants.Constants;
import org.texastorque.subsystems.Climber.ServoDirection;
import org.texastorque.torquelib.auto.TorqueBlock;
import org.texastorque.torquelib.auto.TorqueSequence;

/**
 * Go to a mid point
 */
public class ClimbSetpoint extends TorqueSequence {

        public ClimbSetpoint() {
                init();
        }

        @Override
        protected void init() {
                // Extend to intermediate point on high bar
                addBlock(new TorqueBlock(new ClimbToSetpoint(130, -130)));
        }
}