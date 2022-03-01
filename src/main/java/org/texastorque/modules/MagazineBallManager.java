package org.texastorque.modules;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import org.texastorque.inputs.State;

public class MagazineBallManager {
    private static MagazineBallManager instance;

    public enum MagazineState { NONE, BLUE, RED }

    private final String magazineTableName = "ball_mag";
    private NetworkTableInstance NT;
    private NetworkTableEntry ballColorEntry;
    private MagazineState state = MagazineState.NONE;
    private String ballColor;

    public MagazineBallManager() {
        NT = NetworkTableInstance.getDefault();
        ballColorEntry = NT.getTable(magazineTableName).getEntry("color");
    }

    public void update() {
        ballColor = ballColorEntry.getString("none");

        if (ballColor.equals("blue"))
            setMagState(MagazineState.BLUE);
        else if (ballColor.equals("red"))
            setMagState(MagazineState.RED);
        else
            setMagState(MagazineState.NONE);
    }

    public void setMagState(MagazineState state) { this.state = state; }

    /**
     * @return If the magazine detects our alliance's ball
     */
    public boolean isOurAlliance() { return !isEnemyAlliance(); }

    /**
     * @return If the magazine detects our enemy's ball
     */
    public boolean isEnemyAlliance() {
        return state == MagazineState.BLUE &&
            State.getInstance().getAllianceColor() == State.AllianceColor.RED ||
            state == MagazineState.RED &&
                State.getInstance().getAllianceColor() ==
                    State.AllianceColor.BLUE;
    }

    public MagazineState getMagazineState() { return state; }

    public static MagazineBallManager getInstance() {
        return instance == null ? instance = new MagazineBallManager()
                                : instance;
    }
}