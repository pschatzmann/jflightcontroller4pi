package ch.pschatzmann.jflightcontroller4pi.devices;

/**
 * Device where the writing gets managed by the event loop
 * 
 * @author pschatzmann
 *
 */
public interface IOutDevice extends IDevice {
	/**
	 * Process the output of a single record
	 */
	void processOutput();

}
