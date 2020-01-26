package ch.pschatzmann.jflightcontroller4pi.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.devices.IMUDevice;
import ch.pschatzmann.jflightcontroller4pi.devices.IRecalculate;
import ch.pschatzmann.jflightcontroller4pi.devices.ISensor;
import ch.pschatzmann.jflightcontroller4pi.devices.OutDevice;
import ch.pschatzmann.jflightcontroller4pi.guidence.imu.IMU;
import ch.pschatzmann.jflightcontroller4pi.guidence.imu.sensors.SensorGY87;
import ch.pschatzmann.jflightcontroller4pi.modes.FlightMode;
import ch.pschatzmann.jflightcontroller4pi.modes.ManualModeRule;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;
import ch.pschatzmann.jflightcontroller4pi.protocols.NullDevice;
import junit.framework.Assert;

/**
 * Tests for the Gy87 Sensor and the IMU
 * 
 * @author pschatzmann
 *
 */
public class TestIMU {

	@Before
	public void beforeMethod() {
		//org.junit.Assume.assumeTrue(i2cExists());
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
		FlightController fc = new FlightController();
		sensor.setup(fc);

		for (int j = 0; j < 10; j++) {
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
				
		fc.run();

		for (int j = 0; j < 10; j++) {
			Thread.sleep(1000);
			System.out.println(imu);
		}
		fc.stop();
	}
	
	@Test
	public void testFastInvSqrRoot() {
		
			float value = 0.15625f;
			Assert.assertEquals(1.0/Math.sqrt(value), IMU.invSqrt(value),0.01);

			value = -0.15625f;
			Assert.assertEquals(1.0/Math.sqrt(value), IMU.invSqrt(value),0.01);

	}

}
