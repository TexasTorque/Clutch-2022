package org.texastorque.subsystems;

import org.texastorque.torquelib.base.TorqueSubsystem;
import org.texastorque.torquelib.util.TorqueSwerveOdometry;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.util.Units;

public class Drivebase extends TorqueSubsystem {
    private static volatile Drivebase instance;

    private final double DISTANCE_TO_CENTER_X = Units.inchesToMeters(10.875);
    private final double DISTANCE_TO_CENTER_Y = Units.inchesToMeters(10.875);

    private final Translation2d locationBackLeft = new Translation2d(DISTANCE_TO_CENTER_X, -DISTANCE_TO_CENTER_Y);
    private final Translation2d locationBackRight = new Translation2d(DISTANCE_TO_CENTER_X, DISTANCE_TO_CENTER_Y);
    private final Translation2d locationFrontLeft = new Translation2d(-DISTANCE_TO_CENTER_X, -DISTANCE_TO_CENTER_Y);
    private final Translation2d locationFrontRight = new Translation2d(-DISTANCE_TO_CENTER_X, DISTANCE_TO_CENTER_Y);

    public final SwerveDriveKinematics kinematics;
    public final TorqueSwerveOdometry odometry;

    private Drivebase() {
        

    }

    @Override
    public void initTeleop() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateTeleop() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void initAuto() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateAuto() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void output() {
        // TODO Auto-generated method stub
        
    }

    public static void 
    
    
}
