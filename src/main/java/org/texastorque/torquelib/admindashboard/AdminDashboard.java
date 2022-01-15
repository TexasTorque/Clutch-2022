package org.texastorque.torquelib.admindashboard;

import java.util.HashMap;
import java.util.List;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;

/**
 * This class provides a shuffleboard implementation that allows controls of
 * defined variables. Check out 2021 Cavalier carnival code for a sample
 * implementation.
 */
public class AdminDashboard {
    private static volatile AdminDashboard instance;

    private final ShuffleboardTab tab = Shuffleboard.getTab("Admin");
    private final HashMap<AdminDashboardIdentifiersI, NetworkTableEntry> entries = new HashMap<>();

    private AdminDashboard(List<AdminDashboardEntry> entries) {
        entries.parallelStream().forEach(v -> {
            this.entries.put(v.getIdentifier(), tab.add(v.getIdentifier().toString(), v.getDefaultDouble()).getEntry());
        });
    }

    public double getDoubleValue(AdminDashboardIdentifiersI identifier) {
        if (!checkExists(identifier))
            return 0;

        return entries.get(identifier).getDouble(0);
    }

    public void setDoubleValue(AdminDashboardIdentifiersI identifier, double value) {
        if (!checkExists(identifier))
            return;

        entries.get(identifier).setDouble(value);
    }

    /**
     * Check whether a given identifier exists
     * 
     * @param identifier The identifier to check
     * @return Whether it exists or not
     */
    private boolean checkExists(AdminDashboardIdentifiersI identifier) {
        if (!entries.containsKey(identifier)) {
            System.out.println("WARNING: '" + identifier + "' not found in admin dashboard!");
            return false;
        }
        return true;
    }

    public static synchronized AdminDashboard getInstance() {
        if (instance == null) {
            System.out.println("Attempting to get AdminDashboard instance before creation!!!");
        }
        return instance;
    }

    public static synchronized void makeInstance(List<AdminDashboardEntry> entries) {
        instance = new AdminDashboard(entries);
    }
}