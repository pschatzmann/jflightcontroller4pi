package ch.pschatzmann.jflightcontroller4pi.guidence.imu.sensors;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.control.IFilter;
import ch.pschatzmann.jflightcontroller4pi.devices.ISensor;
import ch.pschatzmann.jflightcontroller4pi.guidence.imu.Value3D;
import ch.pschatzmann.jflightcontroller4pi.integration.DatagramReader;
import ch.pschatzmann.jflightcontroller4pi.integration.I2C;
import ch.pschatzmann.jflightcontroller4pi.integration.I2C.Type2Byte;
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
	// we will report the sensor data in gauss dependent on range 2GA->1.22; 8G->4.35
	private double factor = 1.0; // / 4.35;  
	private Value3D magnetometer = new Value3D();
	private double frequency;
	// by default we measure 10 times


	@Override
	public void setup(FlightController flightController)  {
		try {
			log.info("setup "+this.getName());
			this.flightController = flightController;
			// reset
			i2c.write(0xA,(byte)0x80); 
			
			// Oversampling: 512 - 00 / 256 - 01 / 128 - 10 / 64 - 11
			// Range: range 2G - 00 / 8G - 01  
			// Output data rate: 10Hz - 00 / 50Hz 01 / 100Hz - 10 /  200Hz - 11
			// Mode: continues read - 01 / standby - 00
			i2c.write(0x09, (byte)0b11010001); // control register 1
		} catch(Exception ex) {
			log.error(ex.getMessage(), ex);
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
			setValues();

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


	public void setValues() throws IOException {
		byte status[] = new byte[1];
		// check status bit
		i2c.read(0x06, 1, status);
		if ((status[0] & 0b00000001) == 1 && (status[0] & 0b00000010) == 0) {
			int valuesRaw[] = new int[3];
			i2c.read(0x00, 3, valuesRaw, Type2Byte.SignedReverse);
			double x = (double)valuesRaw[0] * factor;
			double y = (double)valuesRaw[1] * factor;
			double z = (double)valuesRaw[2] * factor;
			magnetometer.set(x, y, z);

			log.debug("SensorQMC5883 new data");
		} else {
			log.debug("SensorQMC5883 data not ready");
		}
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
		sb.append("Magnetometer: ");
		sb.append(getMagnetometer());
		sb.append(System.lineSeparator());
		return sb.toString();
	}

}
