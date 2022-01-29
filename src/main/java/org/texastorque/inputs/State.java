package org.texastorque.inputs;

public class State {
    private static volatile State instance;

    private RobotState state;

    private State() { state = RobotState.DISABLED; }

    public static enum RobotState {
        DISABLED,
        AUTONOMOUS,
        TELEOP,
        TEST;
    }

    public RobotState getRobotState() { return this.state; }
    public void setRobotState(RobotState state) { this.state = state; }

    public static synchronized State getInstance() {
        return instance == null ? instance = new State() : instance;
    }
}
