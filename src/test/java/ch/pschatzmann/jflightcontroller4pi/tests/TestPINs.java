package ch.pschatzmann.jflightcontroller4pi.tests;

import java.io.IOException;

import org.junit.Test;

import com.pi4j.io.gpio.RaspiBcmPin;
import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialPort;
import com.pi4j.io.serial.StopBits;

import ch.pschatzmann.jflightcontroller4pi.data.IData;
import ch.pschatzmann.jflightcontroller4pi.navigation.GPS;
import ch.pschatzmann.jflightcontroller4pi.protocols.InputFromPiI2C;
import ch.pschatzmann.jflightcontroller4pi.protocols.InputSerial;
import ch.pschatzmann.jflightcontroller4pi.protocols.OutputToPiPwm;

/**
 * Test for Raspberry PI Pins
 * 
 * @author pschatzmann
 *
 */
public class TestPINs {

	// hardware pwm GPIO_18 (PWM0) pin 12
	@Test
	public void testPWM18() throws InterruptedException {
		System.out.println("testPWM18");
		System.out.println(RaspiBcmPin.GPIO_18.getName());
		OutputToPiPwm pwm = new OutputToPiPwm(RaspiBcmPin.GPIO_18.getName());
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
	 * Software PWM on GPIO_21 (pin 40)
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testPWM21() throws InterruptedException {
		System.out.println("testPWM21");
		System.out.println(RaspiBcmPin.GPIO_21.getName());
		OutputToPiPwm pwm = new OutputToPiPwm(RaspiBcmPin.GPIO_21.getName());
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
	 * BCM2 (pin 3) and BCM3 (pin 5) for bus 0
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testI2C() throws InterruptedException {
		System.out.println("testI2C");
		byte dataAddresses[] = { (byte) 0x8C, (byte) 0x8E };

		InputFromPiI2C i2c = new InputFromPiI2C(0, (byte) 0x01, dataAddresses);
		for (int j = 0; j < 100; j++) {
			IData data = i2c.getValues();
			System.out.println(data.toString());
		}
		i2c.shutdown();
	}

	/**
	 * The mini UART is mapped to the TXD (GPIO 14) and RXD (GPIO 15)
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@Test
	public void testSerial() throws InterruptedException, IOException {
		System.out.println("testSerial");

		SerialConfig config = new SerialConfig();

		config.device("/dev/ttyS0").baud(Baud._9600).dataBits(DataBits._8).parity(Parity.NONE).stopBits(StopBits._1).flowControl(FlowControl.NONE);
		InputSerial is = new InputSerial(config);
		new Thread(new Runnable() {
			public void run() {
				GPS gps = new GPS();

				while (true) {
					IData data = is.getValues();
					System.out.println(data);
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
		System.out.println("testSerial");

		SerialConfig config = new SerialConfig();

		config.device("/dev/ttyS0").baud(Baud._9600).dataBits(DataBits._8).parity(Parity.NONE).stopBits(StopBits._1).flowControl(FlowControl.NONE);
		InputSerial is = new InputSerial(config);

		new Thread(new Runnable() {
			public void run() {
				GPS gps = new GPS();

				while (true) {
					IData data = is.getValues();
					gps.putValue(data);

					System.out.println(gps);
				}
			}
		}).start();

		Thread.sleep(10000);
		is.shutdown();
	}

}
