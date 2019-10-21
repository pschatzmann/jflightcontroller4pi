package ch.pschatzmann.jflightcontroller4pi.protocols;

/**
 * Interface for PWM Output
 * 
 * @author pschatzmann
 *
 */
public interface IPwmOut {
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
