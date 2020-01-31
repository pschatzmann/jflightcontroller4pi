package ch.pschatzmann.jflightcontroller4pi.modes;

import java.util.Collection;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.devices.IDevice;

/**
 * Interface for Flight Modes
 * @author pschatzmann
 *
 */
public interface IFlightMode {
	/**
	 * Initialize the processing
	 */
	public void setup(FlightController flightController);

	/**
	 * Shut down e.g. before switching to another mode
	 */
	public void shutdown();
	
	/**
	 * Lists all devices which are available for this mode
	 * @return
	 */
	public Collection<IDevice> getDevices();


	/**
	 * Name for mode
	 * @return
	 */
	public String getName();
}
