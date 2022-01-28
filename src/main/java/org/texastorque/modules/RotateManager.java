package org.texastorque.modules;

import java.util.Optional;
import java.util.stream.Stream;

import org.texastorque.constants.Constants;
import org.texastorque.torquelib.util.TorqueMathUtil;

import edu.wpi.first.math.Pair;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

/**
 * Given 4 camera streams
 * front, back, left, right
 * Handle the rotation that must occur for centering of the intake
 */
public class RotateManager {
    final static String detectionEntryName = "detection";
    final static int pixWidth = 640;

    // Each first entry represents a x-pixel entry (0, 640px) of the center of the
    // ball
    // Each second entry represents a radius entry (0, 640px) of the ball
    private final Pair<NetworkTableEntry, NetworkTableEntry> frontEntry;
    private final Pair<NetworkTableEntry, NetworkTableEntry> backEntry;
    private final Pair<NetworkTableEntry, NetworkTableEntry> leftEntry;
    private final Pair<NetworkTableEntry, NetworkTableEntry> rightEntry;

    private final PIDController rotatePID;

    public RotateManager(String frontTableName, String backTableName, String leftTableName, String rightTableName) {
        NetworkTableInstance NT = NetworkTableInstance.getDefault();

        NetworkTable frontTable = NT.getTable(frontTableName);
        NetworkTable backTable = NT.getTable(backTableName);
        NetworkTable leftTable = NT.getTable(leftTableName);
        NetworkTable rightTable = NT.getTable(rightTableName);

        frontEntry = new Pair<NetworkTableEntry, NetworkTableEntry>(frontTable.getEntry("x"), frontTable.getEntry("r"));
        backEntry = new Pair<NetworkTableEntry, NetworkTableEntry>(backTable.getEntry("x"), backTable.getEntry("r"));
        leftEntry = new Pair<NetworkTableEntry, NetworkTableEntry>(leftTable.getEntry("x"), leftTable.getEntry("r"));
        rightEntry = new Pair<NetworkTableEntry, NetworkTableEntry>(rightTable.getEntry("x"), rightTable.getEntry("r"));

        rotatePID = new PIDController(Constants.ROTATE_MANAGER_PID_P, Constants.ROTATE_MANAGER_PID_I,
                Constants.ROTATE_MANAGER_PID_D);
        rotatePID.enableContinuousInput(-180, 180);
    }

    /**
     * Process tables to find desired rotation
     * 
     * @return [-1, 1] of power output to rotation
     */
    public double process() {
        Optional<Pair<NetworkTableEntry, NetworkTableEntry>> opt = Stream
                .of(frontEntry, backEntry, leftEntry, rightEntry)
                .filter((Pair<NetworkTableEntry, NetworkTableEntry> x) -> x.getSecond().getDouble(0) != 0) // remove
                                                                                                           // non-detections
                .max((Pair<NetworkTableEntry, NetworkTableEntry> x, Pair<NetworkTableEntry, NetworkTableEntry> y) -> {
                    // nutshell: radius most important. Scaled by .9 if on side, .8 on back
                    double mulLeft = 1;
                    double mulRight = 1;

                    if (x == leftEntry || x == rightEntry) {
                        mulLeft = .9;
                    } else if (x == backEntry) {
                        mulLeft = .8;
                    }

                    if (y == leftEntry || y == rightEntry) {
                        mulRight = .9;
                    } else if (y == backEntry) {
                        mulRight = .8;
                    }

                    return Double.compare(x.getSecond().getDouble(0) * mulLeft, y.getSecond().getDouble(0) * mulRight);
                });
        // if none are detected, just stop
        if (opt.isEmpty()) {
            return 0;
        }

        Pair<NetworkTableEntry, NetworkTableEntry> entry = opt.get();

        // Now we need to flatten 3d space.
        // We want rotation to be [-180, 180] rapping, allowed by the continuous pid

        // Note: each camera is treated as being 90 degress. They aren't tho! This only
        // works in this algorithm b/c we are using a controller!!!

        double trueAngle = 0;
        double reportedPixel = entry.getSecond().getDouble(0);
        // CCW Positive
        if (entry == frontEntry) {
            trueAngle = (reportedPixel - pixWidth) * (90. / pixWidth);
        } else if (entry == leftEntry) {
            trueAngle = -45 + (pixWidth - reportedPixel) * (90. / pixWidth);
        } else if (entry == rightEntry) {
            trueAngle = 45 + (reportedPixel) * (90 / pixWidth);
        } else { // back
            if (reportedPixel < pixWidth / 2) {
                trueAngle = 180 - (pixWidth / 2 - reportedPixel) * (90. / pixWidth);
            } else {
                trueAngle = -180 + (reportedPixel - pixWidth / 2) * (90 / pixWidth);
            }
        }

        double ret = TorqueMathUtil.constrain(rotatePID.calculate(trueAngle, 0), -1, 1);
        return ret;
    }

}
