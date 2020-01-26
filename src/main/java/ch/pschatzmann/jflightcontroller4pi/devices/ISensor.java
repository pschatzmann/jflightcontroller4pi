package ch.pschatzmann.jflightcontroller4pi.devices;

import java.io.IOException;

/**
 * Input Device
 * 
 * @author pschatzmann
 *
 */
public interface ISensor extends IDevice {
	/**
	 * Process the Input from a Sensor by updating the values (in the parameter store)
	 * */
	void processInput() ;
	
}
