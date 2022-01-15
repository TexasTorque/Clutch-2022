package org.texastorque.torquelib.admindashboard;

public class AdminDashboardEntry {
    private AdminDashboardIdentifiersI identifier;
    private AdminDashboardType type;
    private double defaultDouble;

    public AdminDashboardEntry(AdminDashboardIdentifiersI identifier, AdminDashboardType type, double defaultValue) {
        this.identifier = identifier;
        this.type = type;
        this.defaultDouble = defaultValue;
    }

    public AdminDashboardIdentifiersI getIdentifier() {
        return identifier;
    }

    public AdminDashboardType getType() {
        return type;
    }

    public double getDefaultDouble() {
        return defaultDouble;
    }
}