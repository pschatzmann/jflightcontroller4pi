package ch.pschatzmann.jflightcontroller4pi.protocols;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiBcmPin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.Gpio;

import ch.pschatzmann.jflightcontroller4pi.control.Scaler;
import ch.pschatzmann.jflightcontroller4pi.guidence.navigation.NMEAParser;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

/**
 * Manage Pulse Width Moduleted (PWM) Output to Rasperry PI to drive Servos and
 * Coreless Motors. The default frequency is 50 HZ. We expect to manage input
 * values between -1.0 and 1.0 where 0.0 is the neutral position.
 * 
 * @author pschatzmann
 *
 */

public class OutputToPiPwm implements IPinOut {
	private static Logger log = LoggerFactory.getLogger(OutputToPiPwm.class);
	private GpioController gpio;
	private GpioPinPwmOutput pwm;
	private double value;
	private String pinName;
	private Scaler scaler;
	private int maxScale = 1000;  // defines internal precision. We can manage up to 1000 steps.
	private int frequenceyHZ = 50; // PWM frequency
	private double min = -1.0;
	private double max = 1.0;

	public OutputToPiPwm(String pinName) {
		this.pinName = pinName;
	}

	public OutputToPiPwm() {
	}

	protected void setup() {
		// run this only once
		if (pwm != null) {
			return;
		}
		// we must know the pin Name
		if (pinName == null) {
			log.error("The pin has not been defined.");
			return;
		}

		// setup scaling
		scaler = new Scaler(min, max, 0, maxScale);

		// setup PWM to 50 HZ
		Gpio.pwmSetMode(com.pi4j.wiringpi.Gpio.PWM_MODE_MS);
		Gpio.pwmSetClock(frequenceyHZ);

		// set range from 0 to 1000
		Gpio.pwmSetRange(maxScale);

		gpio = GpioFactory.getInstance();
		Pin pin = RaspiPin.getPinByName(pinName);
		if (pin==null) {
			log.error("The pin {} does not exist",pinName);
			return;
		}
		
		try {
			pwm = gpio.provisionPwmOutputPin(pin);			
			log.info("Using hardware PWM");
		} catch(Exception ex) {
			pwm = gpio.provisionSoftPwmOutputPin(pin);	
			log.info("Using software PWM");
		}
	}

	@Override
	public void setValue(double value) {
		if (pwm == null) {
			setup();
		}
		this.value = value;
		if (pwm != null) {
			int pwmScaledValue = (int) scaler.scale(ParametersEnum.NA, value);
			pwm.setPwm(pwmScaledValue);
		}
	}

	@Override
	public void shutdown() {
		if (gpio != null)
			gpio.shutdown();
	}

	@Override
	public double getValue() {
		return this.value;
	}

	/**
	 * @return the pinName
	 */
	public String getPinName() {
		return pinName;
	}

	/**
	 * @param pinName
	 *            the pinName to set
	 */
	public void setPinName(String pinName) {
		this.pinName = pinName;
	}


	/**
	 * @return the maxScale
	 */
	public int getMaxScale() {
		return maxScale;
	}

	/**
	 * @param maxScale
	 *            the maxScale to set
	 */
	public void setMaxScale(int maxScale) {
		this.maxScale = maxScale;
	}

	/**
	 * @return the frequenceyHZ
	 */
	public int getFrequenceyHZ() {
		return frequenceyHZ;
	}

	/**
	 * @param frequenceyHZ
	 *            the frequenceyHZ to set
	 */
	public void setFrequenceyHZ(int frequenceyHZ) {
		this.frequenceyHZ = frequenceyHZ;
	}
	
	/**
	 * Defines the input range for the set value function
	 * @param min
	 * @param max
	 */
	public void setRange(double min, double max) {
		this.min = min;
		this.max = max;
	}

}
