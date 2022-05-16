package org.texastorque.subsystems;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.constants.Constants;
import org.texastorque.constants.Ports;
import org.texastorque.inputs.AutoInput;
import org.texastorque.inputs.Feedback;
import org.texastorque.inputs.Input;
import org.texastorque.inputs.State;
import org.texastorque.inputs.State.TurretState;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.component.TorqueSparkMax;

public class Turret extends TorqueSubsystem {
    public static volatile Turret instance;

    private static final double maxVoltage = 11;

    private TorqueSparkMax rotator = new TorqueSparkMax(Ports.TURRET);

    private double changeRequest = 0; // power needed to achieve target
    private final PIDController pidController = new PIDController(
            Constants.TURRET_Kp, Constants.TURRET_Ki, Constants.TURRET_Kd);

    public enum HomingDirection {
        LEFT, NONE, RIGHT;
    }

    public enum EncoderOverStatus {
        OFF,
        TOLEFT(Constants.TURRET_MAX_ROTATION_RIGHT, Constants.TURRET_MAX_ROTATION_LEFT - 10),
        TORIGHT(Constants.TURRET_MAX_ROTATION_LEFT, Constants.TURRET_MAX_ROTATION_RIGHT + 10),
        HOMING;
        /*
         * Think of these like states of the turret
         * off - tracking the tape
         *
         * toleft(-190, 160) - robot's moving along tracking the target (in off
         * mode), once -190 degrees is reached, turret resets to 160 degrees and
         * the turret is back in off mode
         *
         * toright(190, -160) - same thing but for the right side
         *
         * homing - looking for target
         */

        private double overPosition;
        private double toPosition;

        private EncoderOverStatus() {
        }

        private EncoderOverStatus(double overPosition, double toPosition) {
            this.overPosition = overPosition;
            this.toPosition = toPosition;
        };

        /**
         * @return the toPosition
         */
        public double getToPosition() {
            return toPosition;
        }

        /**
         * @return the overPosition
         */
        public double getOverPosition() {
            return overPosition;
        }

        /**
         *
         * @param currentPos Current pos
         * @return If at pos
         */
        public boolean atPosition(double currentPos) {
            if (Math.signum(toPosition) == -1) {
                return currentPos - Constants.TOLERANCE_DEGREES <= toPosition;
            } else {
                return currentPos + Constants.TOLERANCE_DEGREES >= toPosition;
            }
        }
    }

    private EncoderOverStatus encoderOverStatus = EncoderOverStatus.OFF;

    public Turret() {
        rotator.setPosition(Constants.TURRET_RATIO * -90. / 360.);
    }

    public void updateTeleop() {
        if (Input.getInstance().getClimberInput().hasClimbStarted()) {
            double pidOut = pidController.calculate(
                    getDegrees(), Constants.TURRET_BACK_ROT);
            changeRequest = Constants.TURRET_Ks * Math.signum(pidOut) + pidOut;
        } else if (State.getInstance().getTurretState() == TurretState.TO_POSITION) {
            double pidOut = pidController.calculate(getDegrees(),
                    State.getInstance().getTurretToPosition().getDegrees());
            changeRequest = Constants.TURRET_Ks * Math.signum(pidOut) + pidOut;
            if (Math.abs(State.getInstance().getTurretToPosition().getDegrees()
                    - getDegrees()) < 5) {
                State.getInstance().setTurretState(TurretState.ON);
            }
        } else if (State.getInstance().getTurretState() == TurretState.ODOMETRY) {
            double pidOut = pidController.calculate(getDegrees(),
                    State.getInstance().getTurretToPosition().getDegrees());
            changeRequest = Constants.TURRET_Ks * Math.signum(pidOut) + pidOut;
        } else if (State.getInstance().getTurretState() == TurretState.ON) {
            if (encoderOverStatus == EncoderOverStatus.OFF) { // turret is tracking tape
                if ((Input.getInstance().getShooterInput().getUsingOdometry() || !checkOver()) && !checkHoming()) {
                    double hOffset = Feedback.getInstance()
                            .getTorquelightFeedback()
                            .getTargetYaw();

                    SmartDashboard.putNumber("Turret HOffset", hOffset);
                    if (Math.abs(hOffset) < Constants.TOLERANCE_DEGREES) {
                        changeRequest = 0;
                    } else {
                        double pidOutput = pidController.calculate(hOffset, 0);
                        changeRequest = (Constants.TURRET_Ks * Math.signum(pidOutput)) +
                                pidOutput;
                    }
                }
            } else if (encoderOverStatus == EncoderOverStatus.HOMING) { // we lost target
                // :( .. let's find it!
                if (!checkOver() && checkHoming()) {
                    if (Input.getInstance().getShooterInput().getHomingDirection() == HomingDirection.LEFT) {
                        changeRequest = 5 + Constants.TURRET_Ks;
                    } else if (Input.getInstance().getShooterInput().getHomingDirection() == HomingDirection.RIGHT) {
                        changeRequest = -5 - Constants.TURRET_Ks;
                    } else {
                        changeRequest = 5 + Constants.TURRET_Ks;
                    }
                }
            } else {
                // if good get out of encodercorrecting
                if (encoderOverStatus.atPosition(getDegrees())) {
                    encoderOverStatus = EncoderOverStatus.OFF;
                } else {
                    // set approp changeReq using pid
                    double pidOut = pidController.calculate(
                            getDegrees(), encoderOverStatus.getToPosition());
                    changeRequest = Constants.TURRET_Ks * Math.signum(pidOut) + pidOut;
                }
            }
        } else if (State.getInstance().getTurretState() == TurretState.CENTER) {
            // Attempt to be at center
            double pidOut = pidController.calculate(
                    getDegrees(), Constants.TURRET_CENTER_ROT);
            changeRequest = Constants.TURRET_Ks * Math.signum(pidOut) + pidOut;
        } else {
            changeRequest = 0;
        }
    }

    @Override
    public void updateAuto() {
        if (AutoInput.getInstance().getSettingTurretPosition()) {
            double pidOut = pidController.calculate(
                    getDegrees(), AutoInput.getInstance().getTurretPosition());
            changeRequest = Constants.TURRET_Ks * Math.signum(pidOut) + pidOut;
        } else {
            updateTeleop();
        }
    }

    private boolean checkOver() {
        // if encoder is over limit (left / right)
        // encoderCorrecting = true;
        if (getDegrees() > EncoderOverStatus.TORIGHT
                .getOverPosition()) { // if the turret is over the right
                                      // degree limit (190)
            encoderOverStatus = EncoderOverStatus.TORIGHT; // turret resets to -160
            return true;
        } else if (getDegrees() < EncoderOverStatus.TOLEFT
                .getOverPosition()) { // if the turret is over te
                                      // left degree limit (-190)
            encoderOverStatus = EncoderOverStatus.TOLEFT; // turret resets to 160
            return true;
        }
        return false;
    }

    private boolean checkHoming() {
        if (!Feedback.getInstance().getTorquelightFeedback().hasTargets()) {
            encoderOverStatus = EncoderOverStatus.HOMING;
            return true;
        }
        encoderOverStatus = EncoderOverStatus.OFF;
        return false;
    }

    public double getDegrees() {
        double pos = rotator.getPosition() / Constants.TURRET_RATIO * 360.;
        return pos % 360;
    }

    public double getRate() {
        return rotator.getVelocity();
    }

    @Override
    public void updateSmartDashboard() {
        SmartDashboard.putNumber("Turret Position", getDegrees());
        SmartDashboard.putNumber("Turret Voltage", changeRequest);
        SmartDashboard.putString("Turret State", State.getInstance().getTurretState().name());
        SmartDashboard.putString("Turret Encoder Over", encoderOverStatus.name());
    }

    @Override
    public void output() {
        rotator.setVoltage(Math.signum(changeRequest) *
                Math.min(Math.abs(changeRequest), maxVoltage));
    }

    public static Turret getInstance() {
        return instance == null ? instance = new Turret() : instance;
    }
}
