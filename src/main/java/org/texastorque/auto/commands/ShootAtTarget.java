package org.texastorque.auto.commands;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.Timer;
import org.texastorque.constants.Constants;
import org.texastorque.inputs.AutoInput;
import org.texastorque.inputs.Feedback;
import org.texastorque.inputs.Input;
import org.texastorque.inputs.State;
import org.texastorque.inputs.State.AutomaticMagazineState;
import org.texastorque.inputs.State.TurretState;
import org.texastorque.subsystems.Drivebase;
import org.texastorque.subsystems.Turret;
import org.texastorque.subsystems.Magazine.BeltDirections;
import org.texastorque.subsystems.Magazine.GateSpeeds;
import org.texastorque.torquelib.auto.TorqueCommand;

public class ShootAtTarget extends TorqueCommand {
    private final double magOutputTime;

    private boolean stop = false;

    private boolean done = false;
    private boolean runMag = false;
    private double startMagTime = 0;

    private boolean turretOn = true;
    private double distance;
    private double outputRPM;
    private double regressionOffset = 0;

    private int readyIterations = 0;
    private final int neededReadyIterations = 2;

    public ShootAtTarget() {
        this.magOutputTime = 2;
    }

    public ShootAtTarget(double magOutputTime) {
        this.magOutputTime = magOutputTime;
    }

    public ShootAtTarget(double magOutputTime, boolean stop) {
        this.magOutputTime = magOutputTime;
        this.stop = stop;
    }

    public ShootAtTarget(double magOutputTime, boolean stop, boolean turretOn) {
        this.magOutputTime = magOutputTime;
        this.stop = stop;
        this.turretOn = turretOn;
    }

    public ShootAtTarget(double magOutputTime, boolean stop, boolean turretOn, double regressionOffset) {
        this.magOutputTime = magOutputTime;
        this.stop = stop;
        this.turretOn = turretOn;
        this.regressionOffset = regressionOffset;
    }

    private double timeMagDown;

    @Override
    protected void init() {
        AutoInput.getInstance().setSetTurretPosition(false);
        State.getInstance().setTurretState(turretOn ? TurretState.TO_POSITION : TurretState.OFF);
        System.out.println("Shoot at target locked & loaded!");
        timeMagDown = Timer.getFPGATimestamp();
    }

    @Override
    protected void continuous() {
        if (Timer.getFPGATimestamp() - timeMagDown < .1) {
            AutoInput.getInstance().setGateDirection(GateSpeeds.CLOSED);
            AutoInput.getInstance().setBeltDirection(BeltDirections.OUTTAKE);
        } else {
            AutoInput.getInstance().setGateDirection(GateSpeeds.CLOSED);
            AutoInput.getInstance().setBeltDirection(BeltDirections.OFF);
        }

        if (Feedback.getInstance().getLimelightFeedback().getTaOffset() > 0) {
            distance = Feedback.getInstance().getLimelightFeedback().getDistance();

            outputRPM = Input.getInstance().getShooterInput().regressionRPM(distance);
            outputRPM += regressionOffset;

            AutoInput.getInstance().setFlywheelSpeed(outputRPM);
            AutoInput.getInstance().setHoodPosition(Input.getInstance().getShooterInput().regressionHood(distance));
        } else {
            distance = Constants.HUB_CENTER_POSITION
                    .getDistance(Drivebase.getInstance().odometry.getPoseMeters().getTranslation());
            outputRPM = Input.getInstance().getShooterInput().regressionRPM(distance);
            outputRPM += regressionOffset;
            AutoInput.getInstance().setFlywheelSpeed(outputRPM);
            AutoInput.getInstance().setHoodPosition(Input.getInstance().getShooterInput().regressionHood(distance));
        }

        Pose2d robotPosition = Drivebase.getInstance().odometry.getPoseMeters();
        double xDist = Constants.HUB_CENTER_POSITION.getX() - robotPosition.getX();
        double yDist = Constants.HUB_CENTER_POSITION.getY() - robotPosition.getY();
        double robotAngleFromGoal = Math.atan2(yDist, xDist);

        if (robotAngleFromGoal < Constants.TURRET_MAX_ROTATION_LEFT
                && robotAngleFromGoal > Constants.TURRET_MAX_ROTATION_RIGHT) {
            Rotation2d rotation = (robotPosition.getRotation().minus(new Rotation2d(robotAngleFromGoal)))
                    .times(-1);
            State.getInstance().setTurretState(TurretState.TO_POSITION);
            State.getInstance().setTurretToPosition(rotation);
        } else {
            State.getInstance().setTurretState(TurretState.ON);
        }

        if (!runMag) {
            // check if rpm is in range (+-x)
            if (Math.abs(outputRPM -
                    Feedback.getInstance().getShooterFeedback().getRPM()) < Constants.SHOOTER_ERROR
                    && Math.abs(Feedback.getInstance().getLimelightFeedback().gethOffset()) < 10) {
                if (readyIterations >= neededReadyIterations) {
                    // if so, launch magazine for x seconds
                    runMag = true;
                    startMagTime = Timer.getFPGATimestamp();

                } else {
                    readyIterations++;
                }
            } else {
                readyIterations = 0;
            }
        } else {
            State.getInstance().setTurretState(TurretState.OFF);
            AutoInput.getInstance().setBeltDirection(BeltDirections.INTAKE);
            AutoInput.getInstance().setGateDirection(GateSpeeds.OPEN);
            if (Timer.getFPGATimestamp() - startMagTime >= magOutputTime)
                done = true;
        }
    }

    @Override
    protected boolean endCondition() {
        return done;
    }

    @Override
    protected void end() {
        System.out.println("ShootAtTarget done, have a good day!");
        done = false;
        runMag = false;
        if (stop)
            AutoInput.getInstance().setFlywheelSpeed(0);
        AutoInput.getInstance().setGateDirection(GateSpeeds.CLOSED);
        State.getInstance().setTurretState(TurretState.OFF);
    }
}
