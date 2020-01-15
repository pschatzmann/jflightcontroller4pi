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
		byte[] id = new byte[3];
		i2c.read((byte)0x10, 3, id);  
		if (id[0]=='H' && id[1]=='4' && id[2]=='3' ){
			i2c.write((byte) 0x09, (byte)0b10000101); // control register 1
		} else {
			log.error("The device is not a QMC5883");
		}
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
		i2c.read((byte)0x00, 3, values);
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
