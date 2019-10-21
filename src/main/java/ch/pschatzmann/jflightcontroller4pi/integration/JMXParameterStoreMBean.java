package ch.pschatzmann.jflightcontroller4pi.integration;

import ch.pschatzmann.jflightcontroller4pi.devices.IDevice;

public interface JMXParameterStoreMBean extends IDevice {
	public double getStatusRoll(); 
	public double getStatusPitch(); 
	public double getStatusYaw();
	public double getStatusSpeed();
	public void setControlThrottle(double throttle);
	public double getControlThrottle();
	public void setControlElevator(double throttle);
	public double getControlElevator();
	public void setControlRudder(double throttle);
	public double getControlRudder();
	public void setControlAileron(double throttle);
	public double getControlAileron();
}
