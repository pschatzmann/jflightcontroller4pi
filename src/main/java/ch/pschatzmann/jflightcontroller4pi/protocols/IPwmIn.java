package ch.pschatzmann.jflightcontroller4pi.protocols;

import ch.pschatzmann.jflightcontroller4pi.data.IData;

/**
 * Interface for Reading PWM information
 * 
 * @author pschatzmann
 *
 */
public interface IPwmIn {
	IData getValues() ;
	void shutdown() ;
}
