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
    }

    @Override
    public final void alwaysContinuous() {
        subsystems.forEach(TorqueSubsystem::smartDashboard);
    }

    @Override
    public final void disabledInit() {
        subsystems.forEach(TorqueSubsystem::initDisabled);
    }

    @Override
    public final void disabledContinuous() {
        subsystems.forEach(TorqueSubsystem::updateDisabled);
    }

    @Override
    public final void teleopInit() {
        subsystems.forEach(TorqueSubsystem::initTeleop);
    }

    @Override
    public final void teleopContinuous() {
        input.update();
        input.smartDashboard();
        subsystems.forEach(TorqueSubsystem::updateTeleop);
    }

    @Override
    public final void autoInit() {
        autoManager.chooseCurrentSequence();
        subsystems.forEach(TorqueSubsystem::initAuto);
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
    public final void autoContinuous() {
        autoManager.runCurrentSequence();
        subsystems.forEach(TorqueSubsystem::updateAuto);
    }

    @Override
    public final void endCompetition() {
        System.out.printf("     _______              _______                 \n"
                          + "    |__   __|            |__   __|                                \n"
                          + "       | | _____  ____ _ ___| | ___  _ __ __ _ _   _  ___         \n"
                          + "       | |/ _ \\ \\/ / _` / __| |/ _ \\| '__/ _` | | | |/ _ \\    \n"
                          + "       | |  __/>  < (_| \\__ \\ | (_) | | | (_| | |_| |  __/      \n"
                          + "       |_|\\___/_/\\_\\__,_|___/_|\\___/|_|  \\__, |\\__,_|\\___| \n"
                          + "                                            | |                   \n"
                          + "                                            |_|                   \n");
    }
}
