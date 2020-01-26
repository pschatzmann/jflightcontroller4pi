package ch.pschatzmann.jflightcontroller4pi.loop;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.control.IFrequency;

/**
 * Support for different alternative implementations of the control loop
 * 
 * @author pschatzmann
 *
 */
public interface IControlLoop extends IFrequency {
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

	/**
	 * Assings the flight controller to the control loop
	 * @param controller
	 */
	void setup(FlightController controller);
	
	/**
	 * Defines that the run command is blocking
	 * @param blocking
	 */
	void setBlocking(boolean blocking);
	
	/**
	 * Checks if the run command is blocking.
	 * @return
	 */
	boolean isBlocking();
	
	

}
