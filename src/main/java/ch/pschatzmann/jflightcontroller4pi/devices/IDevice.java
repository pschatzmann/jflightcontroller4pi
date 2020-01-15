package ch.pschatzmann.jflightcontroller4pi.devices;

import java.io.IOException;

import ch.pschatzmann.jflightcontroller4pi.FlightController;

/**
 * Generic functionality for input and output devices
 * 
 * @author pschatzmann
 *
 */
public interface IDevice {
	/**
	 * Initialize the device
	 * @throws IOException 
	 */
	void setup(FlightController flightController) throws IOException;
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
