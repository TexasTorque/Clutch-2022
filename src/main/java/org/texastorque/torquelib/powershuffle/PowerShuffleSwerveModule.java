package org.texastorque.torquelib.powershuffle;

import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.util.sendable.SendableBuilder;

public class PowerShuffleSwerveModule implements Sendable {

    private double requestedTurn;
    private double turn;
    private double requestedSpeed;
    private double speed;

    public PowerShuffleSwerveModule() {
        setValues(1, 1, .3, 1);
    }

    public PowerShuffleSwerveModule(double requestedTurn, double turn, double requestedSpeed, double speed) {
        setValues(requestedTurn, turn, requestedSpeed, speed);
    }

    public void setValues(double requestedTurn, double turn, double requestedSpeed, double speed) {
        this.requestedTurn = requestedTurn;
        this.turn = turn;
        this.requestedSpeed = requestedSpeed;
        this.speed = speed;
    }

    public double getRequestedTurn() {
        return requestedTurn;
    }

    public double getTurn() {
        return turn;
    }

    public double getRequestedSpeed() {
        return requestedSpeed;
    }

    public double getSpeed() {
        return speed;
    }

    public void setRequestedTurn(double requestedTurn) {
        this.requestedTurn = requestedTurn;
    }

    public void setTurn(double turn) {
        this.turn = turn;
    }

    public void setRequestedSpeed(double requestedSpeed) {
        this.requestedSpeed = requestedSpeed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        builder.setSmartDashboardType(PowerShuffleWidgets.SwerveModule.name());
        builder.addDoubleProperty("requestedTurn", this::getRequestedTurn, this::setRequestedTurn);
        builder.addDoubleProperty("turn", this::getTurn, this::setTurn);
        builder.addDoubleProperty("requestedSpeed", this::getRequestedSpeed, this::setRequestedSpeed);
        builder.addDoubleProperty("speed", this::getSpeed, this::setSpeed);
    }

}
