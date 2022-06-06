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
    public final void disabledInit() {}

    @Override
    public final void disabledContinuous() {}

    @Override
    public final void teleopInit() {
        subsystems.forEach(TorqueSubsystem::initTeleop);
    }

    @Override
    public final void teleopContinuous() {
        input.update();
        subsystems.forEach(TorqueSubsystem::updateTeleop);
    }

    @Override
    public final void autoInit() {
        autoManager.chooseCurrentSequence();
        subsystems.forEach(TorqueSubsystem::initAuto);
    }

    @Override
    public final void autoContinuous() {
        autoManager.runCurrentSequence();
        subsystems.forEach(TorqueSubsystem::updateAuto);
    }

    @Override
    public final void testContinuous() {
        teleopContinuous();
    }

    @Override
    public final void testInit() {
        teleopInit();
    }

    @Override
    public final void endCompetition() {}
}
