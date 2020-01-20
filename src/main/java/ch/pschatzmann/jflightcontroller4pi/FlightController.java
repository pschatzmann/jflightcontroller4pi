package ch.pschatzmann.jflightcontroller4pi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import ch.pschatzmann.jflightcontroller4pi.devices.IDevice;
import ch.pschatzmann.jflightcontroller4pi.devices.IOutDevice;
import ch.pschatzmann.jflightcontroller4pi.devices.IOutDeviceEx;
import ch.pschatzmann.jflightcontroller4pi.loop.ControlLoop;
import ch.pschatzmann.jflightcontroller4pi.loop.ControlLoopWithTimers;
import ch.pschatzmann.jflightcontroller4pi.loop.IControlLoop;
import ch.pschatzmann.jflightcontroller4pi.modes.FlightMode;
import ch.pschatzmann.jflightcontroller4pi.modes.IFlightMode;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParameterStore;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParameterValue;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry class which manages the current state of the Flight Controller and
 * triggers the control loop.
 * 
 * @author pschatzmann
 *
 */

public class FlightController {
	private static Logger log = LoggerFactory.getLogger(FlightController.class);

	private Collection<IDevice> devices = new ArrayList<IDevice>();
	private ParameterStore parameterStore = new ParameterStore();
	private IFlightMode mode = new FlightMode(Collections.emptyList()); // assign value to prevent npe
	private List<IFlightMode> modes = Collections.emptyList();
	private IOutDevice imu, autoPilot;
	private IControlLoop controlLoop; 

	/**
	 * Default Constructor
	 */
	public FlightController() {
		log.info("** FlightController **");
		// default settings
	}

	public FlightController(ParameterStore parameterStore, Collection<IDevice> devices) {
		this();
		this.addDevices(devices);
		this.parameterStore = parameterStore;
	}

	/**
	 * Determines the currently defined flight mode
	 * 
	 * @return
	 */
	public IFlightMode getMode() {
		return mode;
	}

	/**
	 * Changes the flight mode
	 * 
	 * @param mode
	 */
	public void setMode(IFlightMode mode) {
		this.mode.shutdown();
		this.mode = mode;
		mode.setup(this);
		// update the mode as parameter
		this.setValue(ParametersEnum.FLIGHTMODE, this.getModes().indexOf(mode));
	}
	
	/**
	 * Changes the flight mode to the indicated name
	 */
	public void setMode(String mode) {
		for (IFlightMode m : this.getModes()) {
			if (m.getName().equals(mode)) {
				this.setMode(m);
				return;
			}
		}
		
		log.error("The mode '{}' does not exist - command was ignored", mode);
	}
	

	/**
	 * Provides the currently defined devices
	 * 
	 * @return
	 */
	public Collection<IDevice> getDevices() {
		return this.devices;
	}

	/**
	 * Defines all available devices. This is usually done via Spring
	 * 
	 * @param devices
	 */
	public void setDevices(Collection<IDevice> devices) {
		this.addDevices(devices);
	}

	/**
	 * Add some additional devices
	 * 
	 * @param devices2
	 */
	public void addDevices(Collection<IDevice> devices2) {
		// determine new Devices
		List<IDevice> newDevices = new ArrayList<IDevice>(devices2);
		newDevices.removeAll(this.devices);
		this.devices.addAll(newDevices);
		// setup initial input devices
		newDevices.forEach(device -> setupDevice(device));
	}

	protected void setupDevice(IDevice device) {
		try {
			device.setup(this);
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
	}

	/**
	 * Find a device with the help of the control parameter
	 * 
	 * @param control
	 * @return
	 */
	public IOutDeviceEx getControlDevice(ParametersEnum control) {
		for (IDevice device : this.getDevices()) {
			if (device instanceof IOutDeviceEx) {
				if (((IOutDeviceEx) device).getControlParameter() == control) {
					return ((IOutDeviceEx) device);
				}
			}
		}
		throw new RuntimeException("The device does not exist: "+control);
	}

	public IDevice getDevice(String name) {
		for (IDevice device : this.getDevices()) {
			if (name.equals(device.getName())) {
				return device;
			}
		}
		throw new RuntimeException("The device does not exist: "+name);
	}

	/**
	 * Remove the indicated devices
	 * 
	 * @param devices2
	 */
	public void removeDevices(Collection<IDevice> devices2) {
		devices2.forEach(device -> device.shutdown());
		this.devices.removeAll(devices2);
	}

	/**
	 * Provides access to the Parameters
	 * 
	 * @return
	 */
	public ParameterStore getParameterStore() {
		return parameterStore;
	}

	/**
	 * Defines the Parameter Store.
	 * 
	 * @param parameterStore
	 */
	public void setParameterStore(ParameterStore parameterStore) {
		this.parameterStore = parameterStore;
	}

	public List<IFlightMode> getModes() {
		return modes;
	}

	public void setModes(List<IFlightMode> modes) {
		this.modes = modes;
	}

	public IFlightMode selectMode(String name) {
		for (IFlightMode mode : modes) {
			if (name.equals(mode.getName())) {
				this.setMode(mode);
				return mode;
			}
		}
		throw new RuntimeException("The mode does not exist: "+name+" use one of "+modes.stream().map(s -> s.getName()).collect(Collectors.toList()));
	}

	/**
	 * Returs the control loop
	 * 
	 * @return
	 */
	public IControlLoop getControlLoop() {
		return controlLoop;
	}

	/**
	 * Defines the implementation of the control loop
	 * 
	 * @param controlLoop
	 */
	public void setControlLoop(IControlLoop cl) {
		this.controlLoop = cl;
		if (cl!=null) {
			cl.setup(this);
		}
	}

	/**
	 * Returns the latest parameter value from the Parameter Store
	 * 
	 * @param parametersEnum
	 * @return
	 */
	public ParameterValue getValue(ParametersEnum parametersEnum) {
		return this.parameterStore.getValue(parametersEnum);
	}

	/**
	 * Records a new parameter value in the Paramter Store
	 * 
	 * @param parametersEnum
	 * @param value
	 */
	public void setValue(ParametersEnum parametersEnum, double value) {
		this.parameterStore.setValue(parametersEnum, value);
	}

	/**
	 * Returns the current Inertial Measurement Unit (IMU) implementation
	 * 
	 * @return
	 */
	public IOutDevice getImu() {
		return imu;
	}

	/**
	 * Registers the current Inertial Measurement Unit (IMU) implementation
	 * 
	 * @param imu
	 */
	public void setImu(IOutDevice imu) {
		this.imu = imu;
	}

	/**
	 * Returns the current implementation of the gps auto pilot
	 * 
	 * @return
	 */
	public IOutDevice getAutoPilot() {
		return autoPilot;
	}

	public void setAutoPilot(IOutDevice autoPilot) {
		this.autoPilot = autoPilot;
	}


	/**
	 * Process inputs and outputs. This method is blocking!
	 */
	public void run() {
		if (mode != null) {
			mode.setup(this);
		} else {
			log.info("No flight mode!");
		}

		// setup default control loop
		if(this.controlLoop==null) {
			this.setControlLoop(new ControlLoopWithTimers());
		}
		
		this.controlLoop.run();
	}
	

	public void stop() {
		if (controlLoop!=null)
			controlLoop.stop();
	}

	/**
	 * Shut down all devices! This should stop the airplane completely.
	 */
	public void shutdown() {
		log.info("shutdown");
		if (controlLoop!=null)
			this.controlLoop.stop();

		devices.forEach(device -> {
			device.shutdown();
		});
	}

	/**
	 * Sleep for the ineicated milliseconds
	 * 
	 * @param millis
	 */
	public void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}

	}

}
