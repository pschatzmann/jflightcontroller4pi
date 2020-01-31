package ch.pschatzmann.jflightcontroller4pi.modes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.devices.IOutDeviceEx;
import ch.pschatzmann.jflightcontroller4pi.devices.IRecalculate;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

/**
 * Calculation rules for Acro. We just compy the parameter values from the parameterFrom
 * to the defined parameterTarget.
 * 
 * @author pschatzmann
 *
 */
public class ManualModeRule implements IRecalculate {
    private static final Logger log = LoggerFactory.getLogger(ManualModeRule.class);
	private FlightController flightController;
	private ParametersEnum parameterFrom;
	private IOutDeviceEx device;

	public ManualModeRule() {
	}
	
	public ManualModeRule(FlightController flightController, IOutDeviceEx device, ParametersEnum parameterFrom ) {
		this.flightController = flightController;
		this.parameterFrom = parameterFrom;
		this.device = device;
	}
	
	
	@Override
	public void recalculate() {
		double value = flightController.getValue(parameterFrom).value;
		if (flightController.getValue(device.getControlParameter()).value != value) {
			log.debug(device.getControlParameter()+"->"+value);
			flightController.setValue(device.getControlParameter(), value);
		}
	}
	
	public FlightController getFlightController() {
		return this.getDevice() == null ? null : this.getDevice().getFlightController();
	}
	
	public ParametersEnum getParameterFrom() {
		return parameterFrom;
	}
	
	public void setParameterFrom(ParametersEnum parameterFrom) {
		this.parameterFrom = parameterFrom;
	}
	

	@Override
	public IOutDeviceEx getDevice() {
		return device;
	}

	public void setDevice(IOutDeviceEx device) {
		this.device = device;
	}
	
}
