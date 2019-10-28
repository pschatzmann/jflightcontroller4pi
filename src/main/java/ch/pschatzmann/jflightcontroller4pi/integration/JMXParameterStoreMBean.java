package ch.pschatzmann.jflightcontroller4pi.integration;

import ch.pschatzmann.jflightcontroller4pi.devices.IDevice;
import ch.pschatzmann.jflightcontroller4pi.modes.IFlightMode;

public interface JMXParameterStoreMBean extends IDevice {
	public double getRollTarget(); 
	public void setRollTarget(double roll);
	public double getPitchTarget(); 
	public void setPitchTarget(double pitch);
	public double getYawTarget();
	public void setYawTarget(double yaw);
	public double getSpeedTarget();
	public void setSpeedTarget(double speed);

	public double getRollStatus(); 
	public double getPitchStatus(); 
	public double getYawStatus();
	public double getSpeedStatus();


	
	//public void setControlThrottle(double throttle);
	public double getControlThrottle();
	//public void setControlElevator(double elevator);
	public double getControlElevator();
	//public void setControlRudder(double rudder);
	public double getControlRudder();
	//public void setControlAileron(double ailerond);
	public double getControlAileron();
	
	public String getMode();
	public void setMode(String mode);
}
