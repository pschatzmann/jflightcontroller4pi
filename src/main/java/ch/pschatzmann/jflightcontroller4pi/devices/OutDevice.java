package ch.pschatzmann.jflightcontroller4pi.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.control.Scaler;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;
import ch.pschatzmann.jflightcontroller4pi.protocols.IPinOut;
import ch.pschatzmann.jflightcontroller4pi.protocols.PwmOutput;

/**
 * Output Device which needs to be configured to implement the following flow:
 * sensor =) scaler =) control =) IPwmOut.
 * (e.g. 
 * 
 * @author pschatzmann
 *
 */

public class OutDevice implements IOutDeviceEx {
    private static final Logger log = LoggerFactory.getLogger(OutDevice.class);
	private String pinName = null;
	private Scaler scaler = new Scaler(-1.0,1.0,0,100.0);
	private IPinOut pwmPi;
	private IPinOut pwmPiInverted;
	private ParametersEnum control;
	private FlightController flightController;
	private IRecalculate recalculate;
	private double defaultValue = 0;;
	private double minValue = -1.0;
	private double frequency;

	/**
	 * Empty Constructor. The ParametersEnum control, ParametersEnum sensor, double sensorDefaultSetting, String pinNam
	 * need to be defined by calling the related setters!
	 */
	public OutDevice() {
	}
	
	public OutDevice(ParametersEnum control,  double defaultSetting, IPinOut out) {
		this.control = control;
		this.defaultValue = defaultSetting;
		this.setPin(out);
	}

	public OutDevice( ParametersEnum control, IPinOut out) {
		this.control = control;
		this.setPin(out);
	}

	
	@Override
	public void setup(FlightController flightController) {  
		this.flightController = flightController;
        // Define handler to update the Servo Position
		flightController.getParameterStore().register(control, value -> {
			int scaledValue = (int)scaler.scale(ParametersEnum.PIN, value.value);			
			log.debug(control+"->"+scaledValue);
			if (pwmPi.getValue()!=scaledValue) {
				pwmPi.setValue(scaledValue);
				// Ailerons might have 2 outputs
				if (pwmPiInverted!=null)
					this.pwmPiInverted.setValue(-scaledValue);
			}
		});

        // Set control device to neutral position
		flightController.getParameterStore().setValue(control, defaultValue);
	}

	@Override
	public void shutdown() {
		log.info("shutdown");
		flightController.getParameterStore().setValue(control, defaultValue);
		this.recalculate = null;
		pwmPi.shutdown();
	}

	public String getPinName() {
		return pinName;
	}

	public void setPinName(String pinName) {
		this.pinName = pinName;
		this.pwmPi = new PwmOutput(pinName);
	}

	@Override
	public ParametersEnum getControlParameter() {
		return control;
	}

	public void setControlParameter(ParametersEnum control) {
		this.control = control;
	}

	public double getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(double defaultVal) {
		this.defaultValue = defaultVal;
	}
	
	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
		this.scaler.setInputMin(minValue);
	}

	public IPinOut getPin() {
		return pwmPi;
	}
    /**
     * Defines the output hardware device and protocal
     * @param pwmPi
     */
	public void setPin(IPinOut pwmPi) {
		this.pwmPi = pwmPi;
		double value = scaler.scale(ParametersEnum.PIN, this.defaultValue);
		this.pwmPi.setValue(value);
		if (this.pwmPiInverted!=null)
			this.pwmPiInverted.setValue(-value);
	}
	
	public IPinOut getPwmPiInverted() {
		return pwmPiInverted;
	}

	/**
	 * Defines an optional inverted output hardware device and protocal. E.g ailerons need to have
	 * opposing values e.g 1 and -1
	 * @param pwmPiInverted
	 */
	public void setPwmPiInverted(IPinOut pwmPiInverted) {
		this.pwmPiInverted = pwmPiInverted;
	}

	public String getName() {
		return this.getControlParameter().toString();
	}	
	
	@Override
	public String toString() {
		return this.getName();
	}

	@Override
	public IRecalculate getRecalculate() {
		return recalculate;
	}

	@Override
	public void setRecalculate(IRecalculate recalculate) {
		this.recalculate = recalculate;
	}

	public FlightController getFlightController() {
		return flightController;
	}

	public void setFlightController(FlightController flightController) {
		this.flightController = flightController;
	}

	@Override
	public void processOutput() {
		if (this.recalculate!=null) {
			this.recalculate.recalculate();
		}
	}
	
	@Override
	public void setFrequency(double frequency) {
		this.frequency = frequency;
		
	}

	@Override
	public double getFrequency() {
		return this.frequency;
	}


}
