package ch.pschatzmann.jflightcontroller4pi.protocols;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.Pin;

/**
 * Manage Pulse Width Moduleted (PWM) Output to Rasperry PI to drive Servos and Coreless Motors
 * 
 * @author pschatzmann
 *
 */

public class OutputToPiPwm implements IPwmOut {
	private GpioController gpio;
	private GpioPinPwmOutput pwm;
	private double value;

	public OutputToPiPwm(String pinName){
        gpio = GpioFactory.getInstance();
        Pin pin = gpio.getProvisionedPin(pinName).getPin();
        pwm = gpio.provisionSoftPwmOutputPin(pin);
        pwm.setPwmRange(100);
	}
	
	@Override
	public void setValue(double value) {
		this.value = value;
		pwm.setPwm((int) value);
	}

	@Override
	public void shutdown() {
        gpio.shutdown();
	}

	@Override
	public double getValue() {
		return this.value;
	}

}
