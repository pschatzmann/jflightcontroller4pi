package ch.pschatzmann.jflightcontroller4pi.modes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.control.PIDController;
import ch.pschatzmann.jflightcontroller4pi.devices.IOutDeviceEx;
import ch.pschatzmann.jflightcontroller4pi.devices.IRecalculate;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

/**
 * Recalculation logic for The Stabilized Mode using a PID
 * 
 * @author pschatzmann
 *
 */
public class PIDModeRule implements IRecalculate {
	private static Logger log = LoggerFactory.getLogger(FlightController.class);
	private double p = 0.0;
	private double i = 0.0;
	private double d = 0.0;
	private PIDController miniPID;
	private FlightController flightController;
	private ParametersEnum targetParameter;
	private ParametersEnum measuredParameter;
	private IOutDeviceEx device;

	public PIDModeRule() {
	}
	
	public PIDModeRule(FlightController flightController, IOutDeviceEx device, ParametersEnum parameterFrom,ParametersEnum measuredVariable) {
		this.flightController = flightController;
		this.targetParameter = parameterFrom;
		this.measuredParameter = measuredVariable;
		this.device = device;
	}
	
	public double getP() {
		return p;
	}

	public void setP(double p) {
		this.p = p;
	}

	public double getI() {
		return i;
	}

	public void setI(double i) {
		this.i = i;
	}

	public double getD() {
		return d;
	}

	public void setD(double d) {
		this.d = d;
	}


	public PIDController getPid() {
		return miniPID;
	}

	public void setPid(PIDController miniPid) {
		this.miniPID = miniPid;
	}

	public void setup() {	
		if (miniPID==null) {
			miniPID = new PIDController(p,i,d);
		}
	}
	
	@Override
	public void recalculate() {
      // calculate output value to device from input parameter
		if (miniPID==null) {
			this.setup();
		}

		if (flightController==null) {
			throw new RuntimeException("You must define the flightController for the PIDModuleRule");
		}
		double target = flightController.getValue(targetParameter).value;
		double actual = flightController.getValue(measuredParameter).value;
		
		
      	double controlValue = miniPID.getOutput(actual,target);
      	flightController.setValue(device.getControlParameter(), controlValue);
      	log.debug("{} : {} | {}: {} => {}:{}",  measuredParameter,actual,targetParameter, target, device.getControlParameter(),controlValue);
	}

	@Override
	public IOutDeviceEx getDevice() {
		return this.device;
	}

	public ParametersEnum getMeasuredParameter() {
		return measuredParameter;
	}

	public void setMeasuredParameter(ParametersEnum measuredVariable) {
		this.measuredParameter = measuredVariable;
	}

	public ParametersEnum getTargetParameter() {
		return targetParameter;
	}

	public void setTargetParameter(ParametersEnum targetParameter) {
		this.targetParameter = targetParameter;
	}

	public FlightController getFlightController() {
		return flightController;
	}

	public void setFlightController(FlightController flightController) {
		this.flightController = flightController;
	}

	public void setDevice(IOutDeviceEx device) {
		this.device = device;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("p=");
		sb.append(this.getP());
		sb.append(" i=");
		sb.append(this.getI());
		sb.append(" d=");
		sb.append(this.getD());
		return sb.toString();
	}
	
}
