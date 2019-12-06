package ch.pschatzmann.jflightcontroller4pi.protocols;

import ch.pschatzmann.jflightcontroller4pi.data.IData;

/**
 * Interface for Reading Pin information
 * 
 * @author pschatzmann
 *
 */
public interface IPinIn {
	IData getValues() ;
	void shutdown() ;
}
