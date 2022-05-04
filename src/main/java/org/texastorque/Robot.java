package org.texastorque;

import org.texastorque.auto.AutoManager;
import org.texastorque.subsystems.*;
import org.texastorque.torquelib.base.*;

import java.util.ArrayList;


public final class Robot extends TorqueIterative {

    private Input input = Input.getInstance();
    private AutoManager autoManager = AutoManager.getInstance();

    private ArrayList<TorqueSubsystem> subsystems = new ArrayList<TorqueSubsystem>();

    @Override
    public void robotInit() {
        subsystems.add(Drivebase.getInstance());
        // subsystems.add(Intake.getInstance());
    }

    @Override
    public void alwaysContinuous() {
        subsystems.forEach(TorqueSubsystem::smartDashboard);
    }

    @Override
    public void disabledInit() {
        subsystems.forEach(TorqueSubsystem::initDisabled);
    }

    @Override
    public void disabledContinuous() {
        subsystems.forEach(TorqueSubsystem::updateDisabled);
    }

    @Override
    public void teleopInit() {
        subsystems.forEach(TorqueSubsystem::initTeleop);
    }

    @Override
    public void teleopContinuous() {
        input.update();
        input.smartDashboard();
        subsystems.forEach(TorqueSubsystem::updateTeleop);
    }

    @Override
    public void autoInit() {
        autoManager.chooseCurrentSequence();
        subsystems.forEach(TorqueSubsystem::initAuto);
    }

    @Override
    public void testContinuous() {
        teleopContinuous();
    }

    @Override
    public void testInit() {
        teleopInit();
    }

    @Override
    public void autoContinuous() {
        autoManager.runCurrentSequence();
        subsystems.forEach(TorqueSubsystem::updateAuto);
    }

    @Override
    public void endCompetition() {
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
