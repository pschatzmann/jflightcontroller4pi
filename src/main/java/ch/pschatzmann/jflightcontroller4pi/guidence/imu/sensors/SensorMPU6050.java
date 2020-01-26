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
 * MPU6050 Accelerometer and Gyroscope Sensor. We update the ACCELEROMETERx and
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
	private int rx_buffer[] = new int[10];
	private float accelFactor;
	private Value3D accelerometer = new Value3D();;
	private Value3D gyro = new Value3D();
	private float temperature;
	private double calibration[] = new double[10];
	private double frequency;

	
	@Override
	public void setup(FlightController flightController) {
		try {
			log.info("setup " + this.getName());
			this.flightController = flightController;
			// configure the MPU6050 (gyro/accelerometer)
			i2c.write((byte) 0x6B, (byte) 0x00); // exit sleep
			i2c.write((byte) 0x19, (byte) 109); // sample rate = 8kHz 110 =
												// 72.7Hz
			i2c.write((byte) 0x1B, (byte) 0x18); // gyro full scale = +/-
													// 2000dps
			i2c.write((byte) 0x1C, (byte) 0x08); // accelerometer full scale =
													// +/- 4g
			
			calibrate();
			
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	/**
	 * In the GY87 the magnetometer is not available automatically on the I2C
	 * bus. It must be activated by enabling the master mypass mode.
	 * 
	 * @throws IOException
	 */
	public void enableMagnetometer()  {
		try {
			log.info("enableMagnetometer");
			i2c.write((byte) 0x6A, (byte) 0x00); // disable i2c master mode
			i2c.write((byte) 0x37, (byte) 0x02); // enable i2c master bypass mode
		} catch(Exception ex) {
			log.error(ex.getMessage(),ex);
		}
	}

	@Override
	public void shutdown() {
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
	/**
	 * The sensor needs to sit horizontally to calibrate. Then the average of the x and y axis will need to be 0
	 * and the z axis will measure 1g.
	 */
	public void calibrate() {
		log.info("calibrate");
		int buffer[] = new int[10];
		int count=0;
		for (int j=0;j<100;j++) {
			try {
				Thread.sleep(100);
				i2c.read(0x3B, 10, buffer);
				calibration[0] += buffer[0];
				calibration[1] += buffer[1];
				calibration[2] += buffer[2];
				calibration[4] += buffer[4];
				calibration[5] += buffer[5];
				calibration[6] += buffer[6];
				count++;
			} catch (Exception e) {
				log.error(e.getMessage(),e);
			}
		}
		
		calibration[0] = calibration[0] / count;
		calibration[1] = calibration[1] / count;
		calibration[2] = (calibration[2] / count) - 8192.0f;  // target is 1g
		calibration[4] = calibration[4] / count;
		calibration[5] = calibration[5] / count;
		calibration[6] = calibration[6] / count;
		log.info("Accl Calibration totals x={} y={}, z={}", calibration[0],calibration[1],calibration[2]);
		log.info("Gyro Calibration result x={} y={}, z={}", calibration[4],calibration[5],calibration[6]);
		
	}

	@Override
	public void processInput() {
		try {
			i2c.read(0x3B, 10, rx_buffer);
			int accel_x = (int) (rx_buffer[0] - calibration[0]);
			int accel_y = (int) (rx_buffer[1] - calibration[1]);
			int accel_z = (int) (rx_buffer[2] - calibration[2]);
			int mpu_temp = rx_buffer[3];
			int gyro_x = (int) (rx_buffer[4] - calibration[4]);
			int gyro_y = (int) (rx_buffer[5] - calibration[5]);
			int gyro_z = (int) (rx_buffer[6] - calibration[6]);

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
	 * 
	 * @return
	 */
	public Value3D getAccelerometer() {
		return accelerometer;
	}

	/**
	 * Provides the values from the gyroscope
	 * 
	 * @return the gyro
	 */
	public Value3D getGyro() {
		return gyro;
	}

	/**
	 * Provides the temperature
	 * 
	 * @return the temperature in celsius
	 */
	public float getTemperature() {
		return temperature;
	}
	
	@Override
	public void setFrequency(double frequency) {
		this.frequency = frequency;
		
	}

	@Override
	public double getFrequency() {
		return this.frequency;
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
