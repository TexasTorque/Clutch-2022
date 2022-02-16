package org.texastorque;

import org.texastorque.torquelib.base.*;

import java.util.ArrayList;

import org.texastorque.auto.AutoManager;
import org.texastorque.inputs.*;
import org.texastorque.inputs.State.RobotState;
import org.texastorque.subsystems.*;

public class Robot extends TorqueIterative {

    private Input input = Input.getInstance();
    private Feedback feedback = Feedback.getInstance();
    private State state = State.getInstance();

    private ArrayList<TorqueSubsystem> subsystems = new ArrayList<TorqueSubsystem>();

    private AutoManager autoManager = AutoManager.getInstance();

    @Override
    public void robotInit() {
        subsystems.add(Drivebase.getInstance());
        // subsystems.add(Magazine.getInstance());
        // subsystems.add(Intake.getInstance());
        subsystems.add(Climber.getInstance());
        // subsystems.add(Shooter.getInstance());
        // subsystems.add(Turret.getInstance());
    }

    @Override
    public void alwaysContinuous() {
        feedback.update();
        feedback.smartDashboard();
        subsystems.forEach(TorqueSubsystem::updateSmartDashboard);
    }

    @Override
    public void disabledInit() {
        state.setRobotState(RobotState.DISABLED);
        subsystems.forEach(TorqueSubsystem::disable);
    }

    @Override
    public void teleopInit() {
        state.setRobotState(RobotState.TELEOP);
        subsystems.forEach(TorqueSubsystem::initTeleop);
    }

    @Override
    public void teleopContinuous() {
        input.update();
        input.smartDashboard();
        subsystems.forEach(TorqueSubsystem::updateTeleop);
        subsystems.forEach(TorqueSubsystem::output);
        subsystems.forEach(TorqueSubsystem::updateFeedbackTeleop);
    }

    @Override
    public void autoInit() {
        autoManager.chooseCurrentSequence();
        state.setRobotState(RobotState.AUTONOMOUS);
        subsystems.forEach(TorqueSubsystem::initAuto);
    }

    @Override
    public void autoContinuous() {
        autoManager.runCurrentSequence();
        subsystems.forEach(TorqueSubsystem::updateAuto);
        subsystems.forEach(TorqueSubsystem::output);
        subsystems.forEach(TorqueSubsystem::updateFeedbackAuto);
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
