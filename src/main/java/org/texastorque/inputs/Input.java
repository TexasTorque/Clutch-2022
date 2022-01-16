package org.texastorque.inputs;

import org.texastorque.torquelib.base.TorqueInput;
import org.texastorque.torquelib.util.GenericController;

public class Input {
    private static volatile Input instance;

    private GenericController driver;

    private DrivebaseInput drivebaseInput;

    private Input() {
        driver = new GenericController(0, 0.1);

        drivebaseInput = new DrivebaseInput();
    }

    public void update() {
        drivebaseInput.update();
    }

    public void smartDashboard() {
        drivebaseInput.smartDashboard();
    }

    public class DrivebaseInput extends TorqueInput {

        public DrivebaseInput() {
        }

        @Override
        public void update() {
        }

        @Override
        public void reset() {
        }

        @Override
        public void smartDashboard() {
        }
    }

    public DrivebaseInput getDrivebaseInput() {
        return drivebaseInput;
    }

    public static synchronized Input getInstance() {
        return (instance == null) ? instance = new Input() : instance;
    }
}
