package org.texastorque.subsystems;

import org.texastorque.Ports;
import org.texastorque.torquelib.base.TorqueState;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.motors.TorqueSparkMax;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Magazine extends TorqueSubsystem {
    private static volatile Magazine instance;

    public static enum GateDirection implements TorqueState {
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

    public static enum BeltDirection implements TorqueState {
        UP(-1),
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

    @Override
    public void updateTeleop() {
        if (Intake.getInstance().isIntaking())
            beltDirection = BeltDirection.UP;

        if (Shooter.getInstance().isReady() && Turret.getInstance().isLocked())
            gateDirection = GateDirection.FORWARD;

        belt.setPercent(beltDirection.getDirection());
        gate.setPercent(gateDirection.getDirection());

        SmartDashboard.putNumber("Belt", beltDirection.getDirection());
        SmartDashboard.putNumber("Gate", gateDirection.getDirection());
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
