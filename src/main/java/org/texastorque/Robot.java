/**
 * Copyright 2022 Texas Torque.
 * 
 * This file is part of Clutch-2022, which is not licensed for distribution.
 * For more details, see ./license.txt or write <jus@gtsbr.org>.
 */
package org.texastorque;

import edu.wpi.first.wpilibj.IterativeRobotBase;
import edu.wpi.first.wpilibj.TimedRobot;
import java.util.ArrayList;
import org.texastorque.auto.AutoManager;
import org.texastorque.torquelib.base.*;

public final class Robot extends TimedRobot implements Subsystems {

    private final Input input = Input.getInstance();
    private final AutoManager autoManager = AutoManager.getInstance();

    private final ArrayList<TorqueSubsystem> subsystems = new ArrayList<TorqueSubsystem>();

    public Robot() { super(1 / 50.); }

    @Override
    public final void robotInit() {
        subsystems.add(drivebase);
        // subsystems.add(intake);
        subsystems.add(magazine);
        subsystems.add(shooter);
        subsystems.add(turret);
        subsystems.add(climber);
    }

    @Override
    public final void disabledInit() {
        // This makes no sense
        // subsystems.forEach(subsystem -> subsystem.initialize(TorqueMode.DISABLED));
    }

    @Override
    public final void disabledPeriodic() {
        // This makes no sense
        // subsystems.forEach(subsystem -> subsystem.update(TorqueMode.DISABLED));
    }

    @Override
    public final void teleopInit() {
        subsystems.forEach(subsystem -> subsystem.initialize(TorqueMode.TELEOP));
    }

    @Override
    public final void teleopPeriodic() {
        input.update();
        subsystems.forEach(subsystem -> subsystem.update(TorqueMode.TELEOP));
    }

    @Override
    public final void autonomousInit() {
        autoManager.chooseCurrentSequence();
        subsystems.forEach(subsystem -> subsystem.initialize(TorqueMode.AUTO));
    }

    @Override
    public final void autonomousPeriodic() {
        autoManager.runCurrentSequence();
        subsystems.forEach(subsystem -> subsystem.update(TorqueMode.AUTO));
    }

    @Override
    public final void testInit() {
        subsystems.forEach(subsystem -> subsystem.initialize(TorqueMode.TEST));
    }

    @Override
    public final void testPeriodic() {
        input.update();
        subsystems.forEach(subsystem -> subsystem.update(TorqueMode.TEST));
    }
    @Override
    public final void endCompetition() {}
}
