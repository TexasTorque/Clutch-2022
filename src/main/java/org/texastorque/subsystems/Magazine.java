package org.texastorque.subsystems;

import org.texastorque.constants.Constants;
import org.texastorque.constants.Ports;
import org.texastorque.inputs.AutoInput;
import org.texastorque.inputs.Input;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.component.TorqueSparkMax;

public class Magazine extends TorqueSubsystem {
    private volatile static Magazine instance = null;

    public static enum GateSpeeds {
        OPEN(1),
        CLOSED(-.2),
        OFF(0);

        private final double speed;

        GateSpeeds(double speed) {
            this.speed = speed;
        }

        public double getSpeed() {
            return this.speed;
        }
    }

    public static enum BeltDirections {
        OFF(0),
        OUTTAKE(1),
        INTAKE(-1);

        private final int direction;

        BeltDirections(int direction) {
            this.direction = direction;
        }

        public int getDirection() {
            return this.direction;
        }
    }

    private TorqueSparkMax belt;
    private TorqueSparkMax gate;

    private double beltSpeed;
    private double gateSpeed;

    private Magazine() {
        belt = new TorqueSparkMax(Ports.MAGAZINE_BELT);
        gate = new TorqueSparkMax(Ports.MAGAZINE_GATE);
    }

    @Override
    public void updateTeleop() {
        gateSpeed = Input.getInstance()
                .getMagazineInput()
                .getGateDirection()
                .getSpeed();
        beltSpeed = Input.getInstance()
                .getMagazineInput()
                .getBeltDirection()
                .getDirection() *
                Constants.MAGAZINE_BELT_SPEED;
    }

    @Override
    public void updateAuto() {
        gateSpeed = AutoInput.getInstance().getGateDirection().getSpeed();
        beltSpeed = AutoInput.getInstance().getBeltDirection().getDirection() *
                Constants.MAGAZINE_BELT_SPEED;
    }

    @Override
    public void updateFeedbackTeleop() {
    }

    @Override
    public void output() {
        gate.set(gateSpeed);
        belt.set(beltSpeed);
    }

    @Override
    public void updateSmartDashboard() {
    }

    public static synchronized Magazine getInstance() {
        return instance == null ? instance = new Magazine() : instance;
    }
}
