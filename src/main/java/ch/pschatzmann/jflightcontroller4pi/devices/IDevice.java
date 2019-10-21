package ch.pschatzmann.jflightcontroller4pi.devices;

/**
 * Generic functionality for input and output devices
 * 
 * @author pschatzmann
 *
 */
public interface IDevice {
	/**
	 * Initialize the device
	 */
	void setup();
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
