package ch.pschatzmann.jflightcontroller4pi.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.RaspiBcmPin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialPort;
import com.pi4j.io.serial.StopBits;
import com.pi4j.wiringpi.Gpio;

import ch.pschatzmann.jflightcontroller4pi.data.IData;
import ch.pschatzmann.jflightcontroller4pi.guidence.navigation.GPS;
import ch.pschatzmann.jflightcontroller4pi.protocols.InputSerialWithEvents;
import ch.pschatzmann.jflightcontroller4pi.protocols.PwmOutput;

/**
 * Test for Raspberry PI Pins
 * 
 * @author pschatzmann
 *
 */
public class TestPINs {

	/**
	 * Basic PWM scenario using raw pi4j calls on GPIO_18
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testPWM() throws InterruptedException {
		if (!isRaspberryPI())
			return;

		GpioController gpio = GpioFactory.getInstance();
		// setup PWM to 50 HZ
		Gpio.pwmSetMode(com.pi4j.wiringpi.Gpio.PWM_MODE_MS);
		Gpio.pwmSetClock(50);
		// set range from 0 to 1000
		Gpio.pwmSetRange(1000);
		
		GpioPinPwmOutput pwm = null;

		try {
			pwm = gpio.provisionPwmOutputPin(RaspiPin.GPIO_18);			
		} catch(Exception ex) {
			pwm = gpio.provisionSoftPwmOutputPin(RaspiPin.GPIO_18);			
		}

		for (int j = 0; j < 10; j++) {
			System.out.println("min");
			pwm.setPwm(0);
			Thread.sleep(1000);

			System.out.println("neutral");
			pwm.setPwm(500);
			Thread.sleep(1000);

			System.out.println("max");
			pwm.setPwm(1000);
			Thread.sleep(1000);
		}

		gpio.shutdown();
	}

	// hardware pwm GPIO_18 (PWM0) pin 12
	@Test
	public void testPWM18() throws InterruptedException {
		if (!isRaspberryPI())
			return;
		System.out.println("testPWM18");
		System.out.println(RaspiBcmPin.GPIO_18.getName());
		PwmOutput pwm = new PwmOutput(RaspiBcmPin.GPIO_18.getName());

		// our managed range is between -1.0 and 1.0
		for (int j = 0; j < 10; j++) {
			pwm.setValue(-1);
			Thread.sleep(1000);

			pwm.setValue(0);
			Thread.sleep(1000);

			pwm.setValue(1);
			Thread.sleep(1000);
		}

		pwm.setValue(0);
		Thread.sleep(5000);

		pwm.shutdown();
	}

	/**
	 * Software PWM on GPIO_21 (pin 40)
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testPWM21() throws InterruptedException {
		if (!isRaspberryPI())
			return;
		System.out.println("testPWM21");
		System.out.println(RaspiBcmPin.GPIO_21.getName());
		PwmOutput pwm = new PwmOutput(RaspiBcmPin.GPIO_21.getName());
		for (int j = 0; j < 100; j++) {
			pwm.setValue(j);
			Thread.sleep(500);
		}
		for (int j = 100; j > 0; j--) {
			pwm.setValue(j);
			Thread.sleep(500);
		}
		pwm.shutdown();
	}


	/**
	 * The mini UART is mapped to the TXD (GPIO 14) and RXD (GPIO 15)
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@Test
	public void testSerial() throws InterruptedException, IOException {
		if (!isRaspberryPI())
			return;
		System.out.println("testSerial");

		SerialConfig config = new SerialConfig();

		config.device("/dev/ttyS0").baud(Baud._9600).dataBits(DataBits._8).parity(Parity.NONE).stopBits(StopBits._1).flowControl(FlowControl.NONE);
		InputSerialWithEvents is = new InputSerialWithEvents(config);

		// read data and process it in a separate thread
		new Thread(new Runnable() {
			public void run() {
				GPS gps = new GPS();

				while (true) {
					IData data = is.getValues();
					if (data != null) {
						System.out.println(data);
					}
				}
			}
		}).start();

		Thread.sleep(10000);
		is.shutdown();
	}

	/**
	 * The mini UART is mapped to the TXD (GPIO 14) and RXD (GPIO 15)
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@Test
	public void testGPS() throws Exception {
		if (!isRaspberryPI())
			return;
		System.out.println("testSerial");

		SerialConfig config = new SerialConfig();

		config.device("/dev/ttyS0").baud(Baud._9600).dataBits(DataBits._8).parity(Parity.NONE).stopBits(StopBits._1).flowControl(FlowControl.NONE);
		InputSerialWithEvents is = new InputSerialWithEvents(config);

		new Thread(new Runnable() {
			public void run() {
				GPS gps = new GPS();
				double time = 0;

				while (true) {
					IData data = is.getValues();
					gps.putValue(data);

					// we print the value only if it has changed
					if (gps.getValue().getTime() != time) {
						System.out.println(gps);
						time = gps.getValue().getTime();
					}
				}
			}
		}).start();

		Thread.sleep(10000);
		is.shutdown();
	}

	public boolean isRaspberryPI() {
		String osRelease = osRelease();
		boolean result = osRelease != null && osRelease.contains("Raspbian");
		System.out.println("The system is a Raspberry pi: "+result);
		return result;
	}

	/**
	 * get the operating System release
	 * 
	 * @return the first line from /etc/os-release or null
	 */
	private static String osRelease() {
		String os = System.getProperty("os.name");
		if (os.startsWith("Linux")) {
			File osRelease = new File("/etc", "os-release");
			return readFirstLine(osRelease);
		}
		return null;
	}

	/**
	 * read the first line from the given file
	 * 
	 * @param file
	 * @return the first line
	 */
	private static String readFirstLine(File file) {
		String firstLine = null;
		try {
			if (file.canRead()) {
				FileInputStream fis = new FileInputStream(file);
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));
				firstLine = bufferedReader.readLine();
				fis.close();
			}
		} catch (Throwable th) {
		}
		return firstLine;
	}

}
