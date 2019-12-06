package ch.pschatzmann.jflightcontroller4pi.protocols;

/**
 * Interface for Pin Output
 * 
 * @author pschatzmann
 *
 */
public interface IPinOut {
	/**
	 * Sets the value
	 * @param value
	 */
	public void setValue(double value);
	/**
	 * Provides the latest value
	 * @return
	 */
	public double getValue();
	
	/**
	 * Shut down the interface
	 */
	public void shutdown();
}
