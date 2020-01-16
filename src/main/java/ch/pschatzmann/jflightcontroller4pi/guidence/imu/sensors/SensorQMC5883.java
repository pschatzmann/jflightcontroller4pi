package ch.pschatzmann.jflightcontroller4pi.guidence.imu.sensors;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.devices.ISensor;
import ch.pschatzmann.jflightcontroller4pi.guidence.imu.Value3D;
import ch.pschatzmann.jflightcontroller4pi.integration.DatagramReader;
import ch.pschatzmann.jflightcontroller4pi.integration.I2C;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

/**
 * 3-Axis Digital Compass IC.
 * We update the MAGNETOMETERx parameter values.
 * 
 * https://img.filipeflop.com/files/download/Datasheet-QMC5883L-1.0%20.pdf
 * 
 * @author pschatzmann
 *
 */
public class SensorQMC5883 implements ISensor {
	private static final Logger log = LoggerFactory.getLogger(SensorQMC5883.class);
	private I2C i2c = new I2C(0x0D); // magnetic sensing
	private FlightController flightController;
	private int values[] = new int[3];
	// we will report the raw sensor data
	private double factor = 1;
	private Value3D magnetometer = new Value3D();;


	@Override
	public void setup(FlightController flightController) throws IOException {
		log.info("setup "+this.getName());
		this.flightController = flightController;
		// oversampling 256 - 01
		// range 8G - 01
		// 10 hz - 00
		// continues read - 01
		i2c.write((byte) 0x09, (byte)0b01010001); // control register 1
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
			getValues();
			magnetometer.set(values[0] * factor, values[1] * factor, values[2] * factor);

			// update parameters
			if (flightController!=null) {
				flightController.setValue(ParametersEnum.MAGNETOMETERX, magnetometer.x());
				flightController.setValue(ParametersEnum.MAGNETOMETERY, magnetometer.y());
				flightController.setValue(ParametersEnum.MAGNETOMETERZ, magnetometer.z());
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	public int[] getValues() throws IOException {
		byte status[] = new byte[1];
		// check status bit
		i2c.read(0x06, 1, status);
		if ((status[0] & 1) == 1) {
			i2c.read(0x00, 3, values, false);
		} else {
			log.info("SensorQMC5883 data not ready");
		}
		return values;
	}

	/**
	 * @return the factor
	 */
	public double getFactor() {
		return factor;
	}

	/**
	 * Defines the conversion factor so that we can report values in gauss
	 * @param factor
	 *            the factor to set
	 */
	public void setFactor(double factor) {
		this.factor = factor;
	}

	/**
	 * Returns the magnetometer readings multiplied by the factor
	 * @return the magnetometer
	 */
	public Value3D getMagnetometer() {
		return magnetometer;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Magnetometer: ");
		sb.append(getMagnetometer());
		sb.append(System.lineSeparator());
		return sb.toString();
	}

}
