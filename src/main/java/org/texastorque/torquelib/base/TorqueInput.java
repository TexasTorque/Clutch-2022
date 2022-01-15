package org.texastorque.torquelib.base;

/**
 * @apiNote This is a big bruh moment...
 * You overload the update method 
 * You run it using the run method
 * (this is for the assist sequence)
 * 
 * @author Justus (see me for questions)
 */
public abstract class TorqueInput {
    private boolean blocked = false;
    public final void block() { blocked = true; }
    public final void unblock() { blocked = false; }
    public final boolean isBlocked() { return blocked; }

    public abstract void update();

    protected void reset() {};

    protected void smartDashboard() {};

    public final void run() { 
        if (!blocked) update();
        unblock();
    }
}
