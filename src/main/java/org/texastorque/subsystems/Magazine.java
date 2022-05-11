package org.texastorque.subsystems;

import org.texastorque.Ports;
import org.texastorque.torquelib.base.TorqueSubsystemState;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.motors.TorqueSparkMax;

import edu.wpi.first.wpilibj.Timer;

public class Magazine extends TorqueSubsystem {
    private static volatile Magazine instance;

    public static enum GateDirection implements TorqueSubsystemState {
        FORWARD(1),
        REVERSE(-.4),
        OFF(0);

        private final double direction;

        GateDirection(final double direction) {
            this.direction = direction;
        }

        public double getDirection() {
            return this.direction;
        }
    }

    public static enum BeltDirection implements TorqueSubsystemState {
        UP(-1),
        INTAKING(-.5),
        DOWN(1),
        OFF(0);

        private final double direction;

        BeltDirection(final double direction) {
            this.direction = direction;
        }

        public double getDirection() {
            return this.direction;
        }
    }

    private TorqueSparkMax belt, gate;
    private BeltDirection beltDirection;
    private GateDirection gateDirection;

    private Magazine() {
        belt = new TorqueSparkMax(Ports.MAGAZINE.BELT);
        belt.configureDumbCANFrame();
        gate = new TorqueSparkMax(Ports.MAGAZINE.GATE);
        gate.configureDumbCANFrame();
    }

    public void setState(final BeltDirection state, final GateDirection direction) {
        this.beltDirection = state;
        this.gateDirection = direction;
    }

    public void setBeltDirection(final BeltDirection direction) {
        this.beltDirection = direction;
    }

    public void setGateDirection(final GateDirection direction) {
        this.gateDirection = direction;
    }

    private void reset() {
        this.beltDirection = BeltDirection.OFF;
        this.gateDirection = GateDirection.OFF;
    }

    @Override
    public void initTeleop() {
        reset();
    }

    private boolean shootingStarted = false;
    private double shootingStartedTime = 0;
    private final double DROP_TIME = .5;

    @Override
    public void updateTeleop() {
        if (Intake.getInstance().isIntaking()) {
            beltDirection = BeltDirection.INTAKING;
        }

        if (Shooter.getInstance().isShooting()) {
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

        if (Shooter.getInstance().isReady() && Turret.getInstance().isLocked()) {
            beltDirection = BeltDirection.UP;
            gateDirection = GateDirection.FORWARD;
        }

        belt.setPercent(beltDirection.getDirection());
        gate.setPercent(gateDirection.getDirection());

        TorqueSubsystemState.logState(beltDirection);
        TorqueSubsystemState.logState(gateDirection);
    }

    @Override
    public void initAuto() {
        reset();
    }

    @Override
    public void updateAuto() {
        updateTeleop();  
    }

    public static synchronized Magazine getInstance() {
        return instance == null ? instance = new Magazine() : instance;
    }
}
