package ch.pschatzmann.jflightcontroller4pi.modes;

import java.util.Collection;
import java.util.Collections;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.control.Mixer;
import ch.pschatzmann.jflightcontroller4pi.control.MixerComponent;
import ch.pschatzmann.jflightcontroller4pi.devices.IOutDeviceEx;
import ch.pschatzmann.jflightcontroller4pi.devices.IRecalculate;

/**
 * We are combining multiple input values into one combined output to drive the indicated
 * device by calculating the weighted average.
 *  
 * @author pschatzmann
 *
 */
public class MixingModeRule implements IRecalculate {
	private String name = this.getClass().getSimpleName();
	private Collection<MixerComponent> mixerComponents = Collections.emptyList();
	private IOutDeviceEx device;
	private Mixer mixer;
	
	public MixingModeRule() {
	}
	
	@Override
	public IOutDeviceEx getDevice() {
		return device;
	}
	
	public void setDevice(IOutDeviceEx device) {
		this.device = device;
	}
	
	@Override	
	public void recalculate() {	
		FlightController fc = this.getFlightController();
		if (fc==null) {
			throw new RuntimeException("The flightController is not defined for "+getName());			
		}
		if (this.mixer==null) {
			this.mixer = new Mixer(fc, mixerComponents);
		}
		double value = this.mixer.getValue();
		fc.setValue(this.device.getControlParameter(),value);
	}

	public Collection<MixerComponent> getMixerComponents() {
		return mixerComponents;
	}

	public void setMixerComponents(Collection<MixerComponent> mixerComponents) {
		this.mixerComponents = mixerComponents;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public FlightController getFlightController() {
		return this.getDevice()==null ? null : this.getDevice().getFlightController();
	}


}
