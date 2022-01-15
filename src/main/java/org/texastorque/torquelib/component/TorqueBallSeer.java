package org.texastorque.torquelib.component;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

/**
 * This class provides an interface for receiving communication from the ballseer.py vison code.
 * 
 * @author Jack
 * @apiNote code was originally created during the 2021 season
 */
public class TorqueBallSeer {
    private NetworkTableInstance NT_instance;
    private NetworkTable NT_table;

    private NetworkTableEntry frame_width;
    private NetworkTableEntry frame_height;
    private NetworkTableEntry reset;
    private NetworkTableEntry target_location;

    private DetectionArea[] detectionAreas;
    private int savedArea;

    public class DetectionArea {
        private int id;
        private double x;
        private double y;
        private double width;
        private double height;

        public DetectionArea(int id, double x_pos, double y_pos, double w, double h) {
            x = x_pos;
            y = y_pos;
            width = w;
            height = h;
            this.id = id;
        }
        
        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
        
        public double getWidth() {
            return width;
        }

        public double getHeight() {
            return height;
        }

        public int getID() {
            return id;
        }
    }


    /**
     * Generate a new TorqueBallSeer without set detection areas
     */
    public TorqueBallSeer() {
        NT_instance = NetworkTableInstance.getDefault();
        NT_table = NT_instance.getTable("BallSeer");
        
        frame_width = NT_table.getEntry("frame_width");
        frame_height = NT_table.getEntry("frame_height");
        reset = NT_table.getEntry("reset");
        target_location = NT_table.getEntry("target_location");
    }

    /**
     * Sets detection areas
     * @param areas
     */
    public void setDetectionAreas(DetectionArea[] areas) {
        detectionAreas = areas;
    }

    /**
     * @return the target_location
     */
    public double[] getTarget_location() {
        double[] arr = target_location.getDoubleArray(new double[2]);
        // if(arr.length < 2) return new double[]{0,0};
        return arr;
    }

    /**
     * @return the frame_height
     */
    public double getFrame_height() {
        return frame_height.getDouble(0.0);
    }

    /**
     * @return the frame_width
     */
    public double getFrame_width() {
        return frame_width.getDouble(0.0);
    }

    /**
     * @return the saved area int
     */
    public int getSavedArea() {
        return savedArea;
    }

    /**
     * Set the saved area
     */
    public void setSavedArea(int area) {
        savedArea = area;
    }

    /**
     * Sends a reset signal to BallSeer to reset current found ball
     */
    public void reset() {
        reset.setBoolean(true);
    }

    /**
     * Find the mostest closest DetectionArea :)
     * @return the most prominent detection area OR null if nothing matches --- make sure you check!
     */
    public DetectionArea find() {
        if(detectionAreas == null) throw new Error("Trying to find without saved detection areas! (TorqueBallSeer)");
        /*
        reset();
        try {
            Thread.sleep(250);
        } catch(InterruptedException e) {
            System.out.println("Interrupted Exception in TorqueBallSeer!");
        }
        */
        double[] found = getTarget_location();
        if(found.length < 2) return null;
        // O(n) find closest detection based on center
        DetectionArea best = null;
        double best_offset = Double.MIN_NORMAL;
        for (int i = 0; i < detectionAreas.length; i++) {
            DetectionArea current = detectionAreas[i];
            // Check if inside square
            System.out.println(i+": "+found[0]+", "+found[1]);
            System.out.println("X: "+current.getX()+", Y:"+current.getY()+", W:"+current.getWidth()+", H:"+current.getHeight());
            if(found[0] > current.getX()-current.getWidth() && found[0] < current.getX()+current.getWidth()
                && found[1] > current.getY()-current.getHeight() && found[1] < current.getY()+current.getHeight()) {
                // calculate center offset
                System.out.println("offset time");
                double offset = Math.sqrt(Math.pow(current.getX()-found[0],2)
                                        +Math.pow(current.getY()-found[1],2));
                if(offset > best_offset) {
                    best_offset = offset;
                    best = current;
                }
            }
        }
        return best;
    }

    /**
     * 
     * @return if it is center
     */
    public boolean isCenter(double[] target_location) {
        double center = getFrame_width()/2;
        // 60 px range for center
        if(target_location.length < 2) return false;
        boolean ret = target_location[0] > center-10 && target_location[0] < center+10;
        //System.out.println(ret);
        return ret;
    }
}