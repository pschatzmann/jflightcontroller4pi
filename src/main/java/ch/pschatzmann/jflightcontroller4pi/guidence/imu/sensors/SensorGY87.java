package ch.pschatzmann.jflightcontroller4pi.guidence.imu.sensors;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.devices.ISensor;
import ch.pschatzmann.jflightcontroller4pi.guidence.imu.Value3D;
import ch.pschatzmann.jflightcontroller4pi.integration.I2C;

/**
 * 10 DOF (degree of freedom) boards and all sensor handling is by i2c bus It
 * consists of a BMP180,HMC5883 and MPU6050.
 * 
 * In my case the HMC5883 was replaced by a QMC5883! The magnetometer must be
 * seperatly enabled on the mpu6050 in order to make it available ion the i2c
 * bus.
 * 
 * This will provide all information that the IMU is needing to do it's job.
 * 
 * @author pschatzmann
 *
 */
public class SensorGY87 implements ISensor {
	private static final Logger log = LoggerFactory.getLogger(SensorGY87.class);
	private SensorBMP180 bmp180 = new SensorBMP180();
	private SensorQMC5883 qmc5883 = new SensorQMC5883();
	private SensorMPU6050 mpu6050 = new SensorMPU6050();
	
	public SensorGY87(){
	}

	@Override
	public void setup(FlightController flightController) {
		log.info("setup "+this.getName());
		mpu6050.setup(flightController);
		mpu6050.enableMagnetometer();
		bmp180.setup(flightController);
		qmc5883.setup(flightController);
	}

	@Override
	public void shutdown() {
		log.info("shutdown "+this.getName());
		mpu6050.shutdown();
		qmc5883.shutdown();
		bmp180.shutdown();
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public void processInput() {
		log.debug("processInput");
		mpu6050.processInput();
		bmp180.processInput();
		qmc5883.processInput();
	}
	
	public Value3D getAccelerometer() {
		return mpu6050.getAccelerometer();
	}

	public Value3D getGyro() {
		return mpu6050.getGyro();
	}

	public double getTemperature() {
		return bmp180.getTemperature();
	}
	
	public double getPressure() {
		return bmp180.getPressure();
	}
	
	public Value3D getMagnetometer() {
		return qmc5883.getMagnetometer();
	}
	
	/**
	 * @return the bmp180
	 */
	public SensorBMP180 getBmp180() {
		return bmp180;
	}

	/**
	 * @return the qmc5883
	 */
	public SensorQMC5883 getQmc5883() {
		return qmc5883;
	}

	/**
	 * @return the mpu6050
	 */
	public SensorMPU6050 getMpu6050() {
		return mpu6050;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(System.lineSeparator());
		sb.append(mpu6050);
		sb.append(qmc5883);
		sb.append(bmp180);
		return sb.toString();
	}

}
