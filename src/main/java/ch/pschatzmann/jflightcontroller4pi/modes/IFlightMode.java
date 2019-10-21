package ch.pschatzmann.jflightcontroller4pi.modes;

/**
 * Interface for Flight Modes
 * @author pschatzmann
 *
 */
public interface IFlightMode {
	/**
	 * Initialize the processing
	 */
	public void setup();

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
