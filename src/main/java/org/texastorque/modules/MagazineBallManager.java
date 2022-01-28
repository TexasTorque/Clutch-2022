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
    private MagazineState state;
    private String ballColor;
    //private NetworkTable ballColorTable;
    public MagazineBallManager(String magazineTableName) {
        NT = NetworkTableInstance.getDefault();
        // not bothering to save the table, code is below if you need it (uncomment the instance variable too)
        // ballColorTable = NT.getTable(magazineTableName);
        ballColorEntry = NT.getTable(magazineTableName).getEntry("ballColor");
    }

    public void update() {
        ballColor = ballColorEntry.getString("none");
        /* SWITCH STATEMENT, use for inferior code - inferier per
        switch(ballColor) {
            case "red":
                setMagState(MagazineState.RED);
                break;
            case "blue":
                setMagState(MagazineState.BLUE);
                break;
            default:
                setMagState(MagazineState.NONE);
                break;
        }
        */

        if (ballColor == "blue") {
            setMagState(MagazineState.BLUE);
        } else if (ballColor == "red") {
            setMagState(MagazineState.RED);
        } else {
            setMagState(MagazineState.NONE);
        } 
    }

    public void setMagState(MagazineState state) { this.state = state; }
}