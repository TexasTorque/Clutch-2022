package org.texastorque;

import java.util.ArrayList;
import org.texastorque.auto.AutoManager;
import org.texastorque.torquelib.base.*;

public final class Robot extends TorqueIterative implements Subsystems {

    private final Input input = Input.getInstance();
    private final AutoManager autoManager = AutoManager.getInstance();

    private final ArrayList<TorqueSubsystem> subsystems = new ArrayList<TorqueSubsystem>();

    @Override
    public final void robotInit() {
        subsystems.add(drivebase);
        subsystems.add(intake);
        subsystems.add(magazine);
        subsystems.add(shooter);
        subsystems.add(turret);
        subsystems.add(climber);
    }

    @Override
    public final void alwaysContinuous() {}

    @Override
    public final void disabledInit() {
        // This makes no sense
        //subsystems.forEach(subsystem -> subsystem.initialize(TorqueMode.DISABLED));
    }

    @Override
    public final void disabledContinuous() {
        // This makes no sense
        //subsystems.forEach(subsystem -> subsystem.update(TorqueMode.DISABLED));
    }

    @Override
    public final void teleopInit() {
        subsystems.forEach(subsystem -> subsystem.initialize(TorqueMode.TELEOP));
    }

    @Override
    public final void teleopContinuous() {
        input.update();
        subsystems.forEach(subsystem -> subsystem.update(TorqueMode.TELEOP));
    }

    @Override
    public final void autoInit() {
        autoManager.chooseCurrentSequence();
        subsystems.forEach(subsystem -> subsystem.initialize(TorqueMode.AUTO));
    }

    @Override
    public final void autoContinuous() {
        autoManager.runCurrentSequence();
        subsystems.forEach(subsystem -> subsystem.update(TorqueMode.AUTO));
    }

    @Override
    public final void testInit() {
        subsystems.forEach(subsystem -> subsystem.initialize(TorqueMode.TEST));
    }

    @Override
    public final void testContinuous() {
        input.update();
        subsystems.forEach(subsystem -> subsystem.update(TorqueMode.TEST));
    }
    @Override
    public final void endCompetition() {}
}
