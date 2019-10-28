package ch.pschatzmann.jflightcontroller4pi.loop;

/**
 * Support for different alternative implementations of the control loop
 * 
 * @author pschatzmann
 *
 */
public interface IControlLoop {
	/**
	 * Starts the execution
	 */
	void run();

	/**
	 * Halts the execution
	 */
	void stop();
	
	/**
	 * Checks if the control loop has been stopped
	 */
	boolean isStopped();
	
	

}
