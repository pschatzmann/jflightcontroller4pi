package ch.pschatzmann.jflightcontroller4pi.tests;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

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
