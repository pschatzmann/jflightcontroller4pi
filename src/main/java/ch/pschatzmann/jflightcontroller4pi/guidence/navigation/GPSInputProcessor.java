package ch.pschatzmann.jflightcontroller4pi.guidence.navigation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.data.IData;
import ch.pschatzmann.jflightcontroller4pi.devices.IInputProcessor;
import ch.pschatzmann.jflightcontroller4pi.guidence.navigation.coordinates.Coordinate3D;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

/**
 * As IInputProcessor it records the data to the GPS data to the parameter store
 * 
 * @author pschatzmann
 *
 */
public class GPSInputProcessor implements IInputProcessor {
	private static final Logger log = LoggerFactory.getLogger(GPSInputProcessor.class);
	private GPS gps = new GPS();
	
	/**
	 * Process the input string received by the GPS Sensor
	 */
	@Override
	public void processInput(FlightController fc, IData input) {
		if (log.isDebugEnabled()) log.debug(input.toString());

		if (isValid(input)) {
			// Record current settings
			gps.putValue(input);
			
			// Write info to parameters
			fc.setValue(ParametersEnum.GPSALTITUDE, gps.getValue().getAltitude());
			fc.setValue(ParametersEnum.GPSLONGITUDE, gps.getValue().getLongitude());
			fc.setValue(ParametersEnum.GPSLATITUDE, gps.getValue().getLatitude());		
			fc.setValue(ParametersEnum.GPSQUALITY, gps.getValue().getQuality());
			fc.setValue(ParametersEnum.GPSSPEED, gps.getSpeed());
			
			// additional information - please note that the SENSORHEADING is updated from the compass
			fc.setValue(ParametersEnum.GPSHEADING, gps.getDirection());
		}
		
	}

	@Override
	public boolean isValid(IData input) {
		return input != null;
	}

	/**
	 * Returns the GPS object
	 * @return the gps
	 */
	public GPS getGps() {
		return gps;
	}

	/**
	 * Assigns the GPS object
	 * @param gps the gps to set
	 */
	public void setGps(GPS gps) {
		this.gps = gps;
	}
	
	/**
	 * Provides the current Coordinate3D
	 * @return
	 */
	public Coordinate3D getCoordinate3D() {
		return this.gps.getCoordinate3D();
	}
	
	
	public String toString() {
		return gps != null ? this.getGps().toString() : "GPSInputProcessor";
	}

	
}
