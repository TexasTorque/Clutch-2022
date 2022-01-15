package org.texastorque.torquelib.component;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

/**
* This class provides an interface for receiving communication from the RPLidar A1
* as described [here](https://github.com/TexasTorque/TorqueLidarA1).
*
* @author Jack
* @apiNote was originally created during the 2021 season! 
*/    
public class TorqueLidarA1Receiver {
    private NetworkTableInstance NT_instance;
    private NetworkTable NT_table;
    
    private NetworkTableEntry run;
    private NetworkTableEntry left;
    private NetworkTableEntry middle;
    private NetworkTableEntry right;

    public TorqueLidarA1Receiver() {
        NT_instance = NetworkTableInstance.getDefault();
        NT_table = NT_instance.getTable("lidar");

        run = NT_table.getEntry("run");
        left = NT_table.getEntry("left");
        middle = NT_table.getEntry("middle");
        right = NT_table.getEntry("right");
    }

    /**
    * Initiates a scan by setting run to true
    */
    public void startScan() {
        run.setBoolean(true);
    }

    /**
    * Stops a scan by setting run to false
    */
    public void stopScan() {
        run.setBoolean(false);
    }

    /**
    * Toggles a scan by setting run to its opposite
    *
    * @return Boolean of the new run
    */
    public boolean toggleScan() {
        boolean current = run.getBoolean(true); // This default is set to true, so the default operation is to stop
        run.setBoolean(!current);
        return !current;
    }

    /**
    * Returns if it found something under "left"
    *
    * If the request fails the default value returned is false!
    * @return Boolean
    */
    public boolean foundLeft() {
        return left.getBoolean(false);
    }

    /**
    * Returns if it found something under "middle"
    *
    * @apiNote If the request fails the default value returned is false!
    * @return Boolean
    */
    public boolean foundMiddle() {
        return middle.getBoolean(false);
    }

    /**
    * Returns if it found something under "right"
    * 
    * @apiNote If the request fails the default value returned is false!
    * @return Boolean
    */
    public boolean foundRight() {
        return right.getBoolean(false);
    }

    /**
    * Returns a length-3 boolean array of found indicies
    *
    * @return Boolean[]{left, middle, right}
    */
    public boolean[] foundArray() {
        return new boolean[]{foundLeft(), foundMiddle(), foundRight()};
    }

}
