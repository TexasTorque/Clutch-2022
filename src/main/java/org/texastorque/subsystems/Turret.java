package org.texastorque.subsystems;

import org.texastorque.constants.Constants;
import org.texastorque.constants.Ports;
import org.texastorque.inputs.Feedback;
import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.component.TorqueSparkMax;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Turret extends TorqueSubsystem {
    public static volatile Turret instance;

    private TorqueSparkMax rotator = new TorqueSparkMax(Ports.TURRET);

    private double changeRequest = 0; // power needed to achieve target

    private final SimpleMotorFeedforward simpleMotorFeedforward = new SimpleMotorFeedforward(Constants.TURRET_Ks,
            Constants.TURRET_Kv, Constants.TURRET_Ka);
    private final PIDController pidController = new PIDController(Constants.TURRET_Kp, Constants.TURRET_Ki,
            Constants.TURRET_Kp);

    enum EncoderOverStatus {
        OFF, TOLEFT(-190, 160), TORIGHT(190, -160), HOMING;
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

        private double toPosition;
        private double overPosition;

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
                return currentPos - 6 <= toPosition; // 6 degrees of freedom
            } else {
                return currentPos + 6 >= toPosition;
            }
        }
    }

    private EncoderOverStatus encoderOverStatus = EncoderOverStatus.OFF;

    public Turret() {
    }

    public void updateTeleop() {

        if (encoderOverStatus == EncoderOverStatus.OFF) { // turret is tracking tape
            if (!checkOver() && !checkHoming()) {
                // calculate pid controller with goal at 0
                // change request (power) updates continously
                double reqVelocity = Feedback.getInstance().getLimelightFeedback().gethOffset() * 20; // one/twenty sec

                changeRequest = -1 * simpleMotorFeedforward.calculate(reqVelocity)
                        + pidController.calculate(reqVelocity,
                                rotator.getVelocityDegrees());
            }

        } else if (encoderOverStatus == EncoderOverStatus.HOMING) { // we lost target :( .. let's find it!
            if (!checkOver() && checkHoming()) {
                if (Feedback.getInstance().getGyroFeedback().getGyroDirection() == Feedback.GyroDirection.CLOCKWISE) {
                    changeRequest = .1;
                } else if (Feedback.getInstance().getGyroFeedback()
                        .getGyroDirection() == Feedback.GyroDirection.COUNTERCLOCKWISE) {
                    changeRequest = -.1;
                }
            }
        } else {
            // if good get out of encodercorrecting
            if (encoderOverStatus.atPosition(rotator.getDegrees())) {
                encoderOverStatus = EncoderOverStatus.OFF;
            } else {
                // set approp changeReq using pid
                double reqVelocity = (encoderOverStatus.getToPosition() - rotator.getDegrees()) * 4; // one sec

                changeRequest = simpleMotorFeedforward.calculate(reqVelocity)
                        + pidController.calculate(reqVelocity, rotator.getVelocityDegrees());
            }
        }

    }

    private boolean checkOver() {
        // if encoder is over limit (left / right)
        // encoderCorrecting = true;
        if (rotator.getDegrees() > EncoderOverStatus.TORIGHT.getOverPosition()) { // if the turret is over the right
                                                                                  // degree limit (190)
            encoderOverStatus = EncoderOverStatus.TORIGHT; // turret resets to -160
            return true;
        } else if (rotator.getDegrees() < EncoderOverStatus.TOLEFT.getOverPosition()) { // if the turret is over te
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

    @Override
    public void updateSmartDashboard() {
        SmartDashboard.putNumber("rotatorPos", rotator.getPosition());
        SmartDashboard.putNumber("rotatorPosConv", rotator.getPositionConverted());
        SmartDashboard.putNumber("rotatorPosDeg", rotator.getDegrees());
        SmartDashboard.putNumber("changeRequest", changeRequest);
        SmartDashboard.putString("encoderOver", encoderOverStatus.name());
        SmartDashboard.putNumber("Req voltage", changeRequest);
    }

    @Override
    public void output() {
        rotator.setVoltage(changeRequest);
    }

    public static Turret getInstance() {
        if (instance == null)
            instance = new Turret();
        return instance;
    }

}
