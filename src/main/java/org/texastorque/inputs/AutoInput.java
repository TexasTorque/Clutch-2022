package org.texastorque.inputs;

import edu.wpi.first.math.kinematics.SwerveModuleState;

public class AutoInput {
    private static volatile AutoInput instance;

    private SwerveModuleState[] driveStates;

    public SwerveModuleState[] getDriveStates() {
        return driveStates;
    }

    public void setDriveStates(SwerveModuleState[] states) {
        driveStates = states;
    }

    public static AutoInput getInstance() {
        if (instance == null)
            instance = new AutoInput();
        return instance;
    }
}
