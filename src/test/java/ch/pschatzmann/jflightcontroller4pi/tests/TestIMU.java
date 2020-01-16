package ch.pschatzmann.jflightcontroller4pi.tests;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.devices.IMUDevice;
import ch.pschatzmann.jflightcontroller4pi.devices.ISensor;
import ch.pschatzmann.jflightcontroller4pi.guidence.imu.sensors.SensorGY87;

/**
 * Tests for the Gy87 Sensor and the IMU
 * 
 * @author pschatzmann
 *
 */
public class TestIMU {

	@Before
	public void beforeMethod() {
		org.junit.Assume.assumeTrue(i2cExists());
	}

	private boolean i2cExists() {
		boolean result = new File("/dev/i2c-1").exists();
		if (!result) {
			System.out.println("I2C not available on your system - Tests ignored");
		}
		return result;
	}
	

	@Test
	public void testGy87() throws IOException, InterruptedException {
		ISensor sensor = new SensorGY87();
		sensor.setup(null);

		for (int j = 0; j < 30; j++) {
			sensor.processInput();
			Thread.sleep(1000);
			System.out.println(sensor);
		}
	}

	@Test
	public void testIMUDevice() throws IOException, InterruptedException {
		FlightController fc = new FlightController();
		ISensor sensor = new SensorGY87();
		IMUDevice imu = new IMUDevice();
		fc.addDevices(Arrays.asList(sensor, imu));

		new Thread(() -> {
			// execute control loop in the background
			fc.run();
		}).start();

		for (int j = 0; j < 30; j++) {
			Thread.sleep(1000);
			System.out.println(imu);
		}
	}

}
