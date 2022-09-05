/**
 * Copyright 2022 Texas Torque.
 *
 * This file is part of Clutch-2022, which is not licensed for distribution.
 * For more details, see ./license.txt or write <jus@gtsbr.org>.
 */
package org.texastorque;

import org.texastorque.auto.AutoManager;
import org.texastorque.torquelib.base.*;

public final class Robot extends TorqueRobotBase implements Subsystems {
    public Robot() {
        super(Input.getInstance(), AutoManager.getInstance());
        addSubsystem(drivebase);
        addSubsystem(intake);
        addSubsystem(magazine);
        addSubsystem(shooter);
        addSubsystem(turret);
        addSubsystem(climber);
    }
}
