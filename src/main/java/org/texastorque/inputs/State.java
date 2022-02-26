package org.texastorque.inputs;

import edu.wpi.first.networktables.NetworkTableInstance;

public class State {
    private static volatile State instance;

    private RobotState state;
    private TurretState turretState = TurretState.OFF;
    private AutomaticMagazineState automaticMagazineState = AutomaticMagazineState.OFF;
    private AllianceColor allianceColor;

    private State() {
        state = RobotState.DISABLED;

        fetchAllianceColorFromFMS();
    }

    public static enum RobotState {
        DISABLED,
        AUTONOMOUS,
        TELEOP,
        TEST;
    }

    public static enum TurretState {
        OFF, ON, CENTER
    }

    public static enum AutomaticMagazineState {
        OFF, SHOOTING, REFLECTING
    }

    public RobotState getRobotState() {
        return this.state;
    }

    public void setRobotState(RobotState state) {
        this.state = state;
    }

    public TurretState getTurretState() {
        return turretState;
    }

    public void setTurretState(TurretState turretState) {
        this.turretState = turretState;
    }

    public AutomaticMagazineState getAutomaticMagazineState() {
        return automaticMagazineState;
    }

    public void setAutomaticMagazineState(AutomaticMagazineState state) {
        this.automaticMagazineState = state;
    }

    public static enum AllianceColor {
        RED(true), BLUE(false);

        private final boolean red;

        AllianceColor(boolean red) {
            this.red = red;
        }

        public boolean isRed() {
            return this.red;
        }
    }

    public void fetchAllianceColorFromFMS() {
        this.allianceColor = NetworkTableInstance.getDefault()
                .getTable("FMSInfo").getEntry("IsRedAlliance")
                .getBoolean(false) ? AllianceColor.RED : AllianceColor.BLUE;
    }

    public AllianceColor getAllianceColor() {
        if (this.allianceColor == null)
            fetchAllianceColorFromFMS();
        return this.allianceColor;
    }

    public static enum AutoClimb {
        OFF, ON
    }

    private AutoClimb autoClimb;

    /**
     * @return the autoClimb
     */
    public AutoClimb getAutoClimb() {
        return autoClimb;
    }

    /**
     * @param autoClimb the autoClimb to set
     */
    public void setAutoClimb(AutoClimb autoClimb) {
        this.autoClimb = autoClimb;
    }

    public static synchronized State getInstance() {
        return instance == null ? instance = new State() : instance;
    }
}
