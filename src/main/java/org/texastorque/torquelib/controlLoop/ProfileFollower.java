package org.texastorque.torquelib.controlLoop;

public class ProfileFollower {
    double kp, ki, kd, kv, ka;

    double last_error, heading;

    int segment;
    double[][] trajectory;

    public ProfileFollower(double[][] traj) {
        this.trajectory = traj;
    }

    /**
     * Set a new trajectory to follow, and reset the cumulative errors and segment counts
     * @param traj a previously generated trajectory
     */
    public void setTrajectory(double[][] traj) {
        this.trajectory = traj;
        reset();
    }

    /**
     * Configure the PID/VA Variables for the Follower
     * @param kp The proportional term. This is usually quite high (0.8 - 1.0 are common values)
     * @param ki The integral term. Currently unused.
     * @param kd The derivative term. Adjust this if you are unhappy with the tracking of the follower. 0.0 is the default
     * @param kv The velocity ratio. This should be 1 over your maximum velocity @ 100% throttle.
     *           This converts m/s given by the algorithm to a scale of -1..1 to be used by your
     *           motor controllers
     * @param ka The acceleration term. Adjust this if you want to reach higher or lower speeds faster. 0.0 is the default
     */
    public void configurePIDVA(double kp, double ki, double kd, double kv, double ka) {
        this.kp = kp; this.ki = ki; this.kd = kd;
        this.kv = kv; this.ka = ka;
    }

    /**
     * Reset the follower to start again. Encoders must be reconfigured.
     */
    public void reset() {
        last_error = 0; segment = 0;
    }

    /**
     * Calculate the desired output for the motors, based on the distance the robot has covered.
     * This does not account for heading of the robot. To account for heading, add some extra terms in your control
     * loop for realignment based on gyroscope input and the desired heading given by this object.
     * @param distance_covered  The distance covered in meters
     * @return                  The desired output for your motor controller
     */
    public double calculate(double distance_covered) {
        if (segment < trajectory.length) {
            double[] seg = trajectory[segment];
            double error = seg[3] - distance_covered;
            double calculated_value =
                    kp * error +                                    // Proportional
                    kd * ((error - last_error) / seg[0]) +          // Derivative
                    (kv * seg[4] + ka * seg[5]);    // V and A Terms
            last_error = error;
            heading = seg[7];
            segment++;

            return calculated_value;
        } else return 0;
    }

    /**
     * @return the desired heading of the current point in the trajectory
     */
    public double getHeading() {
        return heading;
    }

    /**
     * @return the current segment being operated on
     */
    public double[] getSegment() {
        return trajectory[segment];
    }

    /**
     * @return whether we have finished tracking this trajectory or not.
     */
    public boolean isFinished() {
        return segment >= trajectory.length;
    }
}