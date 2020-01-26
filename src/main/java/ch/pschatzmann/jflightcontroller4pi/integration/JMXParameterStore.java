package ch.pschatzmann.jflightcontroller4pi.integration;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.modes.IFlightMode;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

/**
 * Display the actual Airplane State with the help of JMX. Quick and dirty implementation.
 * For the time beeing it is not worth the effort to implement dynamic parameters
 * 
 * @author pschatzmann
 *
 */
public class JMXParameterStore implements JMXParameterStoreMBean {
    private static final Logger log = LoggerFactory.getLogger(JMXParameterStore.class);
	private FlightController flightController;
	private double frequency = 1;
	
	public JMXParameterStore(){
	}
		
	@Override
	public void setup(FlightController flightController)  {
		this.flightController = flightController;
		try {
	        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer(); 
	        ObjectName name = new ObjectName("ch.pschatzmann.jdkflightsimulator4pi.integration:type=JMXParameterStore"); 
	        mbs.registerMBean(this, name); 
	        log.info("JMX is available");
		} catch(Exception ex) {
			log.error(ex.getMessage(),ex);
		}
	}
		

	@Override
	public double getRollStatus() {
		return flightController.getValue(ParametersEnum.SENSORROLL).value;
	}

	@Override
	public double getPitchStatus() {
		return flightController.getValue(ParametersEnum.SENSORPITCH).value;
	}

	@Override
	public double getYawStatus() {
		return flightController.getValue(ParametersEnum.SENSORYAW).value;
	}

	@Override
	public double getSpeedStatus() {
		return flightController.getValue(ParametersEnum.SENSORSPEED).value;
	}


	public FlightController getFlightController() {
		return flightController;
	}

	public void setFlightController(FlightController flightController) {
		this.flightController = flightController;
	}

	@Override
	public void shutdown() {
		log.info("shutdown");
	}

//	@Override
//	public void setControlThrottle(double throttle) {
//		this.flightController.setValue(ParametersEnum.THROTTLE, delimit(throttle));
//		
//	}
	
	@Override
	public double getControlThrottle() {
		return flightController.getValue(ParametersEnum.THROTTLE).value;
	}


//	private double delimit(double value) {
//		if (value<-1.0) return -1;
//		if (value>1.0) return 1.0;
//		return value;
//	}

//	@Override
//	public void setControlElevator(double throttle) {
//		this.flightController.setValue(ParametersEnum.ELEVATOR, delimit(throttle));
//		
//	}

	@Override
	public double getControlElevator() {
		return flightController.getValue(ParametersEnum.ELEVATOR).value;

	}

//	@Override
//	public void setControlRudder(double throttle) {
//		this.flightController.setValue(ParametersEnum.RUDDER, delimit(throttle));		
//	}

	@Override
	public double getControlRudder() {
		return flightController.getValue(ParametersEnum.RUDDER).value;
	}

//	@Override
//	public void setControlAileron(double throttle) {
//		this.flightController.setValue(ParametersEnum.AILERON, delimit(throttle));		
//		
//	}

	@Override
	public double getControlAileron() {
		return flightController.getValue(ParametersEnum.AILERON).value;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public double getRollTarget() {
		return flightController.getValue(ParametersEnum.ROLL).value;
	}

	@Override
	public void setRollTarget(double roll) {
		this.flightController.setValue(ParametersEnum.ROLL, roll);		
		
	}

	@Override
	public double getPitchTarget() {
		return flightController.getValue(ParametersEnum.PITCH).value;
	}

	@Override
	public void setPitchTarget(double pitch) {
		this.flightController.setValue(ParametersEnum.PITCH, pitch);		
		
	}

	@Override
	public double getYawTarget() {
		return flightController.getValue(ParametersEnum.YAW).value;
	}

	@Override
	public void setYawTarget(double yaw) {
		this.flightController.setValue(ParametersEnum.YAW, yaw);		
		
	}

	@Override
	public double getSpeedTarget() {
		return flightController.getValue(ParametersEnum.SPEED).value;
	}

	@Override
	public void setSpeedTarget(double speed) {
		this.flightController.setValue(ParametersEnum.SPEED, speed);		
		
	}
	
	public String getMode() {
		return flightController.getMode().getName();
	}
	
	public void setMode(String mode) {
		flightController.selectMode(mode);
	}
	
	@Override
	public void setFrequency(double frequency) {
		this.frequency  = frequency;
		
	}

	@Override
	public double getFrequency() {
		return this.frequency;
	}

	
	@Override
	public String toString() {
		return this.getName();
	}

}
