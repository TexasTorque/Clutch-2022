package org.texastorque.modules;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

enum MagazineState {
    NONE, BLUE, RED
}

public class MagazineBallManager {
    private NetworkTableInstance NT;
    private NetworkTableEntry ballColorEntry;
    private MagazineState state = MagazineState.NONE;
    private String ballColor;

    public MagazineBallManager(String magazineTableName) {
        NT = NetworkTableInstance.getDefault();
        ballColorEntry = NT.getTable(magazineTableName).getEntry("ballColor");
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

    public MagazineState getMagazineState() {
        return state;
    }
}