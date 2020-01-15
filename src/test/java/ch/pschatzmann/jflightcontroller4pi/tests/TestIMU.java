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
	public void testBMP180() throws Exception {
		// Create I2C bus
		I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
		// Get I2C device, BMP180 I2C address is 0x77(119)
		I2CDevice device = bus.getDevice(0x77);

		// Calibration Cofficients stored in EEPROM of the device
		// Read 22 bytes of data from address 0xAA(170)
		byte[] data = new byte[22];
		device.read(0xAA, data, 0, 22);

		// Convert the data
		int AC1 = data[0] * 256 + data[1];
		int AC2 = data[2] * 256 + data[3];
		int AC3 = data[4] * 256 + data[5];
		int AC4 = ((data[6] & 0xFF) * 256) + (data[7] & 0xFF);
		int AC5 = ((data[8] & 0xFF) * 256) + (data[9] & 0xFF);
		int AC6 = ((data[10] & 0xFF) * 256) + (data[11] & 0xFF);
		int B1 = data[12] * 256 + data[13];
		int B2 = data[14] * 256 + data[15];
		int MB = data[16] * 256 + data[17];
		int MC = data[18] * 256 + data[19];
		int MD = data[20] * 256 + data[21];
		Thread.sleep(500);
		
		// Select measurement control register
		// Enable temperature measurement
		device.write(0xF4, (byte)0x2E);
		Thread.sleep(100);
		
		// Read 2 bytes of data from address 0xF6(246)
		// temp msb, temp lsb
		device.read(0xF6, data, 0, 2);
		
		// Convert the data
		int temp = ((data[0] & 0xFF) * 256 + (data[1] & 0xFF));
		
		// Select measurement control register
		// Enable pressure measurement, OSS = 1
		device.write(0xF4, (byte)0x74);
		Thread.sleep(100);
		
		// Read 3 bytes of data from address 0xF6(246)
		// pres msb1, pres msb, pres lsb
		device.read(0xF6, data, 0, 3);
		
		// Convert the data
		double pres = (((data[0] & 0xFF) * 65536) + ((data[1] & 0xFF) * 256) + (data[2] & 0xFF)) / 128;

		// Callibration for Temperature
		double X1 = (temp - AC6) * AC5 / 32768.0;
		double X2 = (MC * 2048.0) / (X1 + MD);
		double B5 = X1 + X2;
		double cTemp = ((B5 + 8.0) / 16.0) / 10.0;
		double fTemp = cTemp * 1.8 + 32;
		
		// Calibration for Pressure
		double B6 = B5 - 4000;
		X1 = (B2 * (B6 * B6 / 4096.0)) / 2048.0;
		X2 = AC2 * B6 / 2048.0;
		double X3 = X1 + X2;
		double B3 = (((AC1 * 4 + X3) * 2) + 2) / 4.0;
		X1 = AC3 * B6 / 8192.0;
		X2 = (B1 * (B6 * B6 / 2048.0)) / 65536.0;
		X3 = ((X1 + X2) + 2) / 4.0;
		double B4 = AC4 * (X3 + 32768) / 32768.0;
		double B7 = ((pres - B3) * (25000.0));
		double pressure = 0.0;
		pressure = ((double)B7 * 2.0) / (double)B4;
		X1 = (pressure / 256.0) * (pressure / 256.0);
		X1 = (X1 * 3038.0) / 65536.0;
		X2 = ((-7357) * pressure) / 65536.0;
		pressure = (pressure + (X1 + X2 + 3791) / 16.0) / 100;
		
		// Output data to screen
		System.out.printf("Pressure : %.2f hPa %n", pressure);
		System.out.printf("Temperature in Celsius : %.2f C %n", cTemp);
		System.out.printf("Temperature in Fahrenheit : %.2f F %n", fTemp);

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

//	@Test
//	public void testIMUDevice() throws IOException, InterruptedException {
//		FlightController fc = new FlightController();
//		ISensor sensor = new SensorGY87();
//		IMUDevice imu = new IMUDevice();
//		fc.addDevices(Arrays.asList(sensor, imu));
//
//		new Thread(() -> {
//			// execute control loop in the background
//			fc.run();
//		}).start();
//
//		for (int j = 0; j < 30; j++) {
//			Thread.sleep(1000);
//			System.out.println(imu);
//		}
//	}

}
