package org.texastorque.subsystems;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.Ports;
import org.texastorque.Subsystems;
import org.texastorque.torquelib.base.TorqueMode;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.base.TorqueSubsystemState;
import org.texastorque.torquelib.motors.TorqueSparkMax;
import org.texastorque.torquelib.util.TorqueLogging;

public final class Magazine extends TorqueSubsystem implements Subsystems {
    private static volatile Magazine instance;

    public static enum GateDirection implements TorqueSubsystemState {
        FORWARD(1),
        REVERSE(-.4),
        OFF(0);

        private final double direction;

        GateDirection(final double direction) { this.direction = direction; }

        public final double getDirection() { return this.direction; }
    }

    public static enum BeltDirection implements TorqueSubsystemState {
        UP(-1),
        INTAKING(-.5),
        DOWN(1),
        OFF(0);

        private final double direction;

        BeltDirection(final double direction) { this.direction = direction; }

        public final double getDirection() { return this.direction; }
    }

    private final TorqueSparkMax belt, gate;
    private BeltDirection beltDirection;
    private GateDirection gateDirection;

    private Magazine() {
        belt = new TorqueSparkMax(Ports.MAGAZINE.BELT);
        belt.configureDumbCANFrame();
        gate = new TorqueSparkMax(Ports.MAGAZINE.GATE);
        gate.configureDumbCANFrame();
    }

    public final void setState(final BeltDirection state, final GateDirection direction) {
        this.beltDirection = state;
        this.gateDirection = direction;
    }

    public final void setBeltDirection(final BeltDirection direction) { this.beltDirection = direction; }

    public final void setGateDirection(final GateDirection direction) { this.gateDirection = direction; }

    @Override
    public final void initialize(final TorqueMode mode) {
        this.beltDirection = BeltDirection.OFF;
        this.gateDirection = GateDirection.OFF;
    }

    private boolean shootingStarted = false;
    private double shootingStartedTime = 0;
    private final double DROP_TIME = .2;

    @Override
    public final void update(final TorqueMode mode) {
        if (intake.isIntaking()) { beltDirection = BeltDirection.UP; }

        if (shooter.isShooting()) {
            if (!shootingStarted) {
                shootingStarted = true;
                shootingStartedTime = Timer.getFPGATimestamp();
            }
            if (Timer.getFPGATimestamp() - shootingStartedTime <= DROP_TIME) {
                beltDirection = BeltDirection.DOWN;
                gateDirection = GateDirection.REVERSE;
            }
        } else {
            shootingStarted = false;
        }

        if (shooter.isReady() && turret.isLocked()) {
            beltDirection = BeltDirection.UP;
            gateDirection = GateDirection.FORWARD;
        }

        belt.setPercent(beltDirection.getDirection());
        gate.setPercent(gateDirection.getDirection());

        TorqueSubsystemState.logState(beltDirection);
        TorqueSubsystemState.logState(gateDirection);

        // SmartDashboard.putNumber("Belt Amps", belt.getCurrent());
        TorqueLogging.putNumber("Belt Amps", belt.getCurrent());
    }


    public static final synchronized Magazine getInstance() {
        return instance == null ? instance = new Magazine() : instance;
    }
}
