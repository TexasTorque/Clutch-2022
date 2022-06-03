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
}
