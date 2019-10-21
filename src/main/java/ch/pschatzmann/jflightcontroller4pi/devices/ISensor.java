package ch.pschatzmann.jflightcontroller4pi.devices;

/**
 * Input Device
 * 
 * @author pschatzmann
 *
 */
public interface ISensor extends IDevice {
	/**
	 * Process the Input from a Sensor
	 */
	void processInput();
}
