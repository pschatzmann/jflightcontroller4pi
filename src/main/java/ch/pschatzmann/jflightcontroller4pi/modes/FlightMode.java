package ch.pschatzmann.jflightcontroller4pi.modes;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.devices.IDevice;
import ch.pschatzmann.jflightcontroller4pi.devices.IOutDeviceEx;
import ch.pschatzmann.jflightcontroller4pi.devices.IRecalculate;

/**
 * Generic Flight Mode implementation which can be configured e.g. with Spring. We manage the devices
 * and Rules which are relevant for this Mode: Stabilized mode e.g. needs an IMU device
 * 
 * @author pschatzmann
 *
 */
public class FlightMode implements IFlightMode {
	private static Logger log = LoggerFactory.getLogger(FlightMode.class);
	private Collection<IRecalculate> recalcCollection = new HashSet<IRecalculate>();
	private FlightController flightController;
	private String name = this.getClass().getSimpleName();
	private Collection<IDevice> devices = new HashSet<IDevice>();

	/**
	 * Empty Constructor
	 */
	public FlightMode() {
	}

	/**
	 * Setup of with Initial (Empty) Mode
	 */
	public FlightMode(Collection<IRecalculate> rules ) {
		this.recalcCollection = rules;
	}
	
	/**
	 * Add local relevant devices to the controller and updates the calculation rules in the devices
	 */
	@Override
	public void setup(FlightController flightController) {
		log.info("setup '{}'",this.getName());
		this.flightController = flightController;	
		// reset all existing calculation rules
		this.flightController.getDevices().stream()
			.filter(d -> d instanceof IOutDeviceEx)
			.map(d -> (IOutDeviceEx)d)
			.forEach(dev -> dev.setRecalculate(null));
		
		// add devices from rules
		recalcCollection.stream().map(c -> c.getDevice()).forEach(dev -> this.devices.add(dev));
		
		// make the devices known to the controller
		flightController.addDevices(this.getDevices());
				
		// set the new calculation rules in the devices
		recalcCollection.forEach(rule -> rule.getDevice().setRecalculate(rule));
	}

	@Override
	public void shutdown() {
		log.info("shutdown {}",this.getName());
		if (flightController!=null) {
			flightController.removeDevices(this.getDevices());
		}
	}

	public Collection<IDevice> getDevices() {
		return recalcCollection.stream().map(c -> c.getDevice()).collect(Collectors.toSet());
	}
	
	/**
	 * Define Devices owned by the mode
	 * @param devices
	 */
	public void setDevices(Collection<IDevice>  devices) {
		this.devices = devices;
	}

	public FlightController getFlightController() {
		return flightController;
	}

	public void setFlightController(FlightController flightController) {
		this.flightController = flightController;
	}
	
	public Collection<IRecalculate> getRules() {
		return recalcCollection;
	}

	public void setRules(Collection<IRecalculate> recalcCollection) {
		this.recalcCollection = recalcCollection;
		recalcCollection.forEach(c -> {
			IDevice dev = c.getDevice();
			((IOutDeviceEx)dev).setRecalculate(c);
		});
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return this.getName();
	}

}
