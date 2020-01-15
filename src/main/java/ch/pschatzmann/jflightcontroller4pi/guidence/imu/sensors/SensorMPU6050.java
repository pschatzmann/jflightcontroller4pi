package ch.pschatzmann.jflightcontroller4pi.guidence.imu.sensors;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.devices.ISensor;
import ch.pschatzmann.jflightcontroller4pi.guidence.imu.Value3D;
import ch.pschatzmann.jflightcontroller4pi.integration.I2C;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

/**
 * MPU6050 Accelerometer and Gyroscope Senosor. We update the ACCELEROMETERx and
 * GYROx parameter values.
 * 
 * https://www.rlocman.ru/i/File/2017/01/28/MPU-6000-Datasheet1.pdf
 * 
 * @author pschatzmann
 *
 */
public class SensorMPU6050 implements ISensor {
	private static final Logger log = LoggerFactory.getLogger(SensorMPU6050.class);
	private I2C i2c = new I2C(0x68); // (Accelerometers/Gyroscope)
	private FlightController flightController;
	private short rx_buffer[] = new short[10];
	private float accelFactor;
	private Value3D accelerometer = new Value3D();;
	private Value3D gyro = new Value3D();
	private float temperature;

	@Override
	public void setup(FlightController flightController) throws IOException {
		this.flightController = flightController;
		// configure the MPU6050 (gyro/accelerometer)
		i2c.write((byte)0x6B,(byte) 0x00); // exit sleep
		i2c.write((byte)0x19,(byte) 109); // sample rate = 8kHz 110 = 72.7Hz
		i2c.write((byte)0x1B,(byte) 0x18); // gyro full scale = +/- 2000dps
		i2c.write((byte)0x1C,(byte) 0x08); // accelerometer full scale = +/- 4g
	}

	/**
	 * In the GY87 the magnetometer is not available automatically on the I2C bus. It must be activated by
	 * enabling the master mypass mode. 
	 * 
	 * @throws IOException
	 */
	public void enableMagnetometer() throws IOException {
		i2c.write((byte)0x6A, (byte)0x00); // disable i2c master mode
		i2c.write((byte)0x37, (byte)0x02); // enable i2c master bypass mode
	}

	@Override
	public void shutdown() {
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public void processInput() {
		try {
			i2c.read((byte)0x3B, 10, rx_buffer);
			short accel_x = rx_buffer[0];
			short accel_y = rx_buffer[1];
			short accel_z = rx_buffer[2];
			short mpu_temp = rx_buffer[3];
			short gyro_x = rx_buffer[4];
			short gyro_y = rx_buffer[5];
			short gyro_z = rx_buffer[6];

			// convert temperature reading into degrees Celsius
			temperature = mpu_temp / 340.0f + 36.53f;

			// convert accelerometer readings into G's
			float accelFactor = 1.0f / 8192.0f;

			// convert gyro readings into Radians per second
			float gyroFactor = 1f / 939.650784f;

			accelerometer.set(accel_x * accelFactor, accel_y * accelFactor, accel_z * accelFactor);
			gyro.set(gyro_x * gyroFactor, gyro_y * gyroFactor, gyro_z * gyroFactor);

			if (flightController != null) {
				flightController.setValue(ParametersEnum.ACCELEROMETERX, accelerometer.x());
				flightController.setValue(ParametersEnum.ACCELEROMETERY, accelerometer.y());
				flightController.setValue(ParametersEnum.ACCELEROMETERZ, accelerometer.z());
				flightController.setValue(ParametersEnum.GYROX, gyro.x());
				flightController.setValue(ParametersEnum.GYROY, gyro.y());
				flightController.setValue(ParametersEnum.GYROZ, gyro.z());
			}

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	/**
	 * Provides the values from the accelerometer
	 * @return
	 */
	public Value3D getAccelerometer() {
		return accelerometer;
	}

	/**
	 * Provides the values from the gyroscope
	 * @return the gyro
	 */
	public Value3D getGyro() {
		return gyro;
	}

	/**
	 * Provides the temperature 
	 * @return the temperature in celsius
	 */
	public float getTemperature() {
		return temperature;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Gyro: ");
		sb.append(getGyro());
		sb.append(System.lineSeparator());
		sb.append("Accelerometer: ");
		sb.append(getAccelerometer());
		sb.append(System.lineSeparator());
		sb.append("Temperature: ");
		sb.append(getTemperature());
		sb.append(System.lineSeparator());
		return sb.toString();
	}
}
