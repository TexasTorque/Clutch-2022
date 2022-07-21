/**
 * Copyright 2022 Texas Torque.
 * 
 * This file is part of Clutch-2022, which is not licensed for distribution.
 * For more details, see ./license.txt or write <jus@gtsbr.org>.
 */
package org.texastorque;

import org.texastorque.subsystems.*;

/**
 * Interface that holds references to the instances of subsystems.
 * A class that implements this interface has direct access to the subsystems,
 * without the need for a static .getInstance() call.
 *
 * @author Justus Languell
 */
public interface Subsystems {
    public final Drivebase drivebase = Drivebase.getInstance();
    public final Intake intake = Intake.getInstance();
    public final Magazine magazine = Magazine.getInstance();
    public final Shooter shooter = Shooter.getInstance();
    public final Turret turret = Turret.getInstance();
    public final Climber climber = Climber.getInstance();
}
