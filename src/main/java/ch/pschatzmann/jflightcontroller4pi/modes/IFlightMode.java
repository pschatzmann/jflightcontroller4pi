package ch.pschatzmann.jflightcontroller4pi.modes;

import ch.pschatzmann.jflightcontroller4pi.FlightController;

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
	 * Name for mode
	 * @return
	 */
	public String getName();
}
