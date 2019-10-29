package ch.pschatzmann.jflightcontroller4pi.tuning;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.devices.ISensor;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

/**
 * We try to make sure that filghtgear is running and prividing actual data. We do this by checking the last
 * update of the pitch sensor.
 * 
 * @author pschatzmann
 *
 */
public class HeartBeatSensor implements ISensor {
	FlightController flightController;
	boolean isActive;

	@Override
	public void setup(FlightController flightController) {
		this.flightController = flightController;
	}

	@Override
	public void shutdown() {
	}

	@Override
	public String getName() {
		return "HeartBeatSensor";
	}

	@Override
	public void processInput() {
		isActive = (System.currentTimeMillis() - this.flightController.getValue(ParametersEnum.SENSORPITCH).timestamp) < 1000;
	}

	/**
	 * @return the flightController
	 */
	public FlightController getFlightController() {
		return flightController;
	}

	/**
	 * @return the isActive
	 */
	public boolean isActive() {
		return isActive;
	}
}