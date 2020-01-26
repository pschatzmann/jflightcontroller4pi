package ch.pschatzmann.jflightcontroller4pi.devices;

import java.io.IOException;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.control.IFrequency;

/**
 * Generic functionality for input and output devices
 * 
 * @author pschatzmann
 *
 */
public interface IDevice  extends IFrequency{
	/**
	 * Initialize the device
	 * @throws IOException 
	 */
	void setup(FlightController flightController) ;
	/**
	 * Shutdown the device
	 */
	void shutdown();
	
	/**
	 * Name for device
	 * @return
	 */
	String getName();
}
