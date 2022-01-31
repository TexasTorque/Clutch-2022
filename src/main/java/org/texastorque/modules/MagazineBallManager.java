package org.texastorque.modules;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class MagazineBallManager {
    private static MagazineBallManager instance;

    public enum MagazineState {
        NONE, BLUE, RED
    }

    private final String magazineTableName = "magazineDetector"; // TODO: update with correct value;
    private NetworkTableInstance NT;
    private NetworkTableEntry ballColorEntry;
    private NetworkTableEntry allianceEntryRed;
    private MagazineState state = MagazineState.NONE;
    private String ballColor;

    public MagazineBallManager() {
        NT = NetworkTableInstance.getDefault();
        ballColorEntry = NT.getTable(magazineTableName).getEntry("ballColor");
        allianceEntryRed = NT.getTable("FMSInfo").getEntry("IsRedAlliance"); // TODO: check if correct
    }

    public void update() {
        ballColor = ballColorEntry.getString("none");

        if (ballColor.equals("blue")) {
            setMagState(MagazineState.BLUE);
        } else if (ballColor.equals("red")) {
            setMagState(MagazineState.RED);
        } else {
            setMagState(MagazineState.NONE);
        }
    }

    public void setMagState(MagazineState state) {
        this.state = state;
    }

    /**
     * @return If the magazine detects our alliance's ball
     */
    public boolean isOurAlliance() {
        return state == MagazineState.BLUE && !allianceEntryRed.getBoolean(false)
                || state == MagazineState.RED && allianceEntryRed.getBoolean(false);
    }

    /**
     * @return If the magazine detects our enemy's ball
     */
    public boolean isEnemyAlliance() {
        return state == MagazineState.BLUE && allianceEntryRed.getBoolean(false)
                || state == MagazineState.RED && !allianceEntryRed.getBoolean(false);
    }

    public MagazineState getMagazineState() {
        return state;
    }

    public static MagazineBallManager getInstance() {
        if (instance == null)
            instance = new MagazineBallManager();
        return instance;
    }
}