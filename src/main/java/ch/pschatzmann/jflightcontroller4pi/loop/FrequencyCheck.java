package ch.pschatzmann.jflightcontroller4pi.loop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.FlightController;

/**
 * We are using a counter to check if the event needs to be executed compared to the requested frequency.
 * @author pschatzmann
 *
 */
public class FrequencyCheck {
	private static Logger log = LoggerFactory.getLogger(FrequencyCheck.class);
	/**
	 * Checks if the event needs to be executed at this iteration.
	 * @param baseFrequency
	 * @param count
	 * @return
	 */
	public static  boolean isRelevantForProcessing(double baseFrequency, double requestedFrequency, long count) {
		boolean result;
		if (requestedFrequency<=0.0) {
			result = true;
		} else {
			double div = baseFrequency / requestedFrequency;		
			if (div<=0) {
				result = true;
			} else {
				long divI = (long) div;
				result = count % (long)divI == 0l;	
			}
		}
		//log.info("{} -> {}",count, result);
		return result;
	}

}
