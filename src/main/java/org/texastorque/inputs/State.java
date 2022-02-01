package org.texastorque.inputs;

public class State {
    private static volatile State instance;

    private RobotState state;
    private AutomaticMagazineState automaticMagazineState = AutomaticMagazineState.OFF;

    private State() {
        state = RobotState.DISABLED;
    }

    public static enum RobotState {
        DISABLED,
        AUTONOMOUS,
        TELEOP,
        TEST;
    }

    public static enum AutomaticMagazineState {
        OFF, SHOOTING, REFLECTING
    }

    public RobotState getRobotState() {
        return this.state;
    }

    public AutomaticMagazineState getAutomaticMagazineState() {
        return automaticMagazineState;
    }

    public void setRobotState(RobotState state) {
        this.state = state;
    }

    public void setAutomaticMagazineState(AutomaticMagazineState state) {
        this.automaticMagazineState = state;
    }

    public static synchronized State getInstance() {
        return instance == null ? instance = new State() : instance;
    }
}
