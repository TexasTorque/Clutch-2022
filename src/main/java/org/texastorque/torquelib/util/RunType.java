package org.texastorque.torquelib.util;

/**
 * An enum for the type of running environment the robot is. Usually a variable
 * would be placed in Constants.java or Ports.java, and then referenced based on
 * this enum for differential operation (such as starting a simulator, etc.).
 * 
 * Enums should only be added, not removed!
 */
public enum RunType {
    NORMAL, SIMULATOR, OFFBOARD
}
