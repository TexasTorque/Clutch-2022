package org.texastorque.util.interfaces;

public interface Stopwatch {

	/** The total time passed since the last {@link #reset()} (or the first {@link #start()})..
	 * 
	 * @return The elapsed time in seconds.
	 */
	public double elapsed();

	/** The total time passed since the last call to {@link #startLap()}.
	 * 
	 * @return The elapsed time in seconds.
	 */
	public double lapTime();
	
	/** Starts timing the current lap. */
	public void startLap();
	
	/** Measures the elapsed time since a specific moment.
	 * 
	 * @param lastTime The time from which to measure.
	 * @return The time in seconds since lastTime.
	 */
	public double timeSince(double lastTime);
	
	/** Begins the timer if it is not already running.
	 * 
	 * @return The start time.
	 */
	public double start();
	
	/** Effectively stops the timer. */
	public void reset();
	
	/** Used to determine if the stop watch is running.
	 * 
	 * @return true if the stop watch is running.
	 */
	public boolean isRunning();
}
