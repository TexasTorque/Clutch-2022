package org.texastorque.modules;

import edu.wpi.first.math.Pair;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import java.util.Optional;
import java.util.stream.Stream;
import org.texastorque.constants.Constants;
import org.texastorque.torquelib.util.TorqueMathUtil;

/**
 * Given 2 camera streams
 * Handle the rotation that must occur for centering of the intake
 */
public class RotateManager {
    private static volatile RotateManager instance;

    private final static int pixWidth = 640;
    private final static String leftTableName = "ball_detection_left";
    private final static String rightTableName = "ball_detection_right";
    private final static double correctionSpeed = Math.PI;

    // Each first entry represents a x-pixel entry (0, 640px) of the center of the
    // ball
    // Each second entry represents a radius entry (0, 640px) of the ball
    private final Pair<NetworkTableEntry, NetworkTableEntry> leftEntry;
    private final Pair<NetworkTableEntry, NetworkTableEntry> rightEntry;

    private RotateManager() {
        NetworkTableInstance NT = NetworkTableInstance.getDefault();

        NetworkTable leftTable = NT.getTable(leftTableName);
        NetworkTable rightTable = NT.getTable(rightTableName);

        leftEntry = new Pair<NetworkTableEntry, NetworkTableEntry>(leftTable.getEntry("position"),
                leftTable.getEntry("radius"));
        rightEntry = new Pair<NetworkTableEntry, NetworkTableEntry>(rightTable.getEntry("position"),
                rightTable.getEntry("radius"));
    }

    /**
     * Process tables to find desired rotation
     *
     * @return Speed to rotate [-MAX_ANGULAR, MAX_ANGULAR]
     */
    public double process() {
        Optional<Pair<NetworkTableEntry, NetworkTableEntry>> opt = Stream
                .of(leftEntry, rightEntry)
                .filter((Pair<NetworkTableEntry, NetworkTableEntry> x) -> x.getSecond().getDouble(0) != 0) // remove
                                                                                                           // non-detections
                .max((Pair<NetworkTableEntry, NetworkTableEntry> x,
                        Pair<NetworkTableEntry, NetworkTableEntry> y) -> {
                    return Double.compare(x.getSecond().getDouble(0),
                            y.getSecond().getDouble(0));
                });
        // if none are detected, just stop
        if (opt.isEmpty()) {
            return 0;
        }

        Pair<NetworkTableEntry, NetworkTableEntry> entry = opt.get();

        double output = 0;
        if (entry == leftEntry) {
            // output is the proportion away multiplied by the correcting speed.
            output = (640 - entry.getSecond().getDouble(0)) / pixWidth * correctionSpeed;
        } else {
            output = entry.getSecond().getDouble(0) / pixWidth * correctionSpeed;
        }
        return TorqueMathUtil.constrain(output, -1, 1);
    }

    public static synchronized RotateManager getInstance() {
        return instance == null ? instance = new RotateManager() : instance;
    }
}
