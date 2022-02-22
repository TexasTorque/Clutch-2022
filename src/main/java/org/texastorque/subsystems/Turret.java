package org.texastorque.subsystems;

import org.texastorque.constants.Constants;
import org.texastorque.constants.Ports;
import org.texastorque.inputs.Feedback;
import org.texastorque.inputs.State;
import org.texastorque.inputs.State.TurretState;
import org.texastorque.modules.MagazineBallManager;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.component.TorqueSparkMax;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Turret extends TorqueSubsystem {
    public static volatile Turret instance;

    private static final double maxVoltage = 11;

    private TorqueSparkMax rotator = new TorqueSparkMax(Ports.TURRET);

    private double changeRequest = 0; // power needed to achieve target
    private final PIDController pidController = new PIDController(Constants.TURRET_Kp, Constants.TURRET_Ki,
            Constants.TURRET_Kd);

    enum EncoderOverStatus {
        OFF, TOLEFT(-130, 65), TORIGHT(70, -120), HOMING;
        /*
         * Think of these like states of the turret
         * off - tracking the tape
         * 
         * toleft(-190, 160) - robot's moving along tracking the target (in off mode),
         * once -190 degrees is reached, turret resets to 160 degrees and the turret is
         * back in off mode
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

    private boolean doingSabotage = false;
    private double sabotageSetpoint = 0;

    public Turret() {
        rotator.setPosition(Constants.TURRET_RATIO * -60. / 360.);
        SmartDashboard.putData("Rpid", pidController);
    }

    public void updateTeleop() {
        if (State.getInstance().getTurretState() == TurretState.ON) {
            if (encoderOverStatus == EncoderOverStatus.OFF) { // turret is tracking tape
                if (!checkOver() && !checkHoming()) {
                    double hOffset = Feedback.getInstance().getLimelightFeedback().gethOffset();

                    // be slightly off :) (do a little trolling)
                    if (MagazineBallManager.getInstance().isEnemyAlliance()) {
                        if (doingSabotage) {
                            hOffset = sabotageSetpoint;
                        } else {
                            doingSabotage = true;
                            sabotageSetpoint = 10 * Math.signum(hOffset) + hOffset;
                            hOffset = sabotageSetpoint;
                        }
                    } else {
                        doingSabotage = false;
                    }

                    if (Math.abs(hOffset) < Constants.TOLERANCE_DEGREES) {
                        changeRequest = 0;
                    } else {
                        double pidOutput = pidController.calculate(
                                hOffset, 0);
                        changeRequest = (Constants.TURRET_Ks * Math.signum(pidOutput)) + pidOutput;
                    }

                }
            } else if (encoderOverStatus == EncoderOverStatus.HOMING) { // we lost target
                // :( .. let's find it!
                if (!checkOver() && checkHoming()) {
                    if (Feedback.getInstance().getGyroFeedback()
                            .getGyroDirection() == Feedback.GyroDirection.CLOCKWISE) {
                        changeRequest = 10 + Constants.TURRET_Ks;
                    } else if (Feedback.getInstance().getGyroFeedback()
                            .getGyroDirection() == Feedback.GyroDirection.COUNTERCLOCKWISE) {
                        changeRequest = -10 - Constants.TURRET_Ks;
                    }
                }
            } else {
                // if good get out of encodercorrecting
                if (encoderOverStatus.atPosition(getDegrees())
                        || Feedback.getInstance().getLimelightFeedback().getTaOffset() > 0) {
                    encoderOverStatus = EncoderOverStatus.OFF;
                } else {
                    // set approp changeReq using pid
                    double pidOut = pidController.calculate(getDegrees(), encoderOverStatus.getToPosition());
                    changeRequest = Constants.TURRET_Ks * Math.signum(pidOut) + pidOut;
                }
            }
        } else {
            changeRequest = 0;
        }

    }

    @Override
    public void updateAuto() {
        updateTeleop();
    }

    private boolean checkOver() {
        // if encoder is over limit (left / right)
        // encoderCorrecting = true;
        if (getDegrees() > EncoderOverStatus.TORIGHT.getOverPosition()) { // if the turret is over the right
                                                                          // degree limit (190)
            encoderOverStatus = EncoderOverStatus.TORIGHT; // turret resets to -160
            return true;
        } else if (getDegrees() < EncoderOverStatus.TOLEFT.getOverPosition()) { // if the turret is over te
                                                                                // left degree limit (-190)
            encoderOverStatus = EncoderOverStatus.TOLEFT; // turret resets to 160
            return true;
        }
        return false;
    }

    private boolean checkHoming() {
        if (Feedback.getInstance().getLimelightFeedback().getTaOffset() == 0) {
            encoderOverStatus = EncoderOverStatus.HOMING;
            return true;
        }
        encoderOverStatus = EncoderOverStatus.OFF;
        return false;
    }

    private double getDegrees() {
        double pos = rotator.getPosition() / Constants.TURRET_RATIO * 360.;
        return pos % 360;
    }

    @Override
    public void updateSmartDashboard() {
        SmartDashboard.putNumber("rotatorPos", rotator.getPosition());
        SmartDashboard.putNumber("rotatorPosConv", rotator.getPositionConverted());
        SmartDashboard.putNumber("rotatorPosDeg", getDegrees());
        SmartDashboard.putNumber("changeRequest", changeRequest);
        SmartDashboard.putString("encoderOver", encoderOverStatus.name());
        SmartDashboard.putNumber("Req voltage", changeRequest);
    }

    @Override
    public void output() {
        rotator.setVoltage(Math.signum(changeRequest) * Math.min(Math.abs(changeRequest), maxVoltage));
    }

    public static Turret getInstance() {
        return instance == null ? instance = new Turret() : instance;
    }

}
