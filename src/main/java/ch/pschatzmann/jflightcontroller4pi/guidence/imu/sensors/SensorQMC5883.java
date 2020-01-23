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
	private double valuesRaw[] = new double[3];
	private double values[] = new double[3];
	// we will report the sensor data in gauss dependent on range 2GA->1.22; 8G->4.35
	private double factor = 1.0 / 4.35;  
	private Value3D magnetometer = new Value3D();
	// by default we measure 10 times
	private int numberOfMeasurements = 10;


	@Override
	public void setup(FlightController flightController)  {
		try {
			log.info("setup "+this.getName());
			this.flightController = flightController;
			// Oversampling: 512 - 00 / 256 - 01 / 128 - 10 / 64 - 11
			// Range: range 2G - 00 / 8G - 01  
			// Output data rate: 10Hz - 00 / 50Hz 01 / 100Hz - 10 /  200Hz - 11
			// Mode: continues read - 01 / standby - 00
			i2c.write((byte) 0x09, (byte)0b11011101); // control register 1
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
			double values[] = getValues();
			magnetometer.set(values[0], values[1], values[2]);

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

	/**
	 * We calculate the avarage by repeating the reading getNumberOfMeasurements times.
	 * @return
	 * @throws IOException
	 */
	public double[] getValues() throws IOException {
		values[0]=0;
		values[1]=0;
		values[2]=0;
		for (int j=0;j<this.getNumberOfMeasurements();j++) {
			getValuesRaw();
			values[0]+=valuesRaw[0];
			values[1]+=valuesRaw[1];
			values[2]+=valuesRaw[2];
			i2c.sleep(11);
		}
		values[0]=values[0]/this.getNumberOfMeasurements()*factor;
		values[1]=values[1]/this.getNumberOfMeasurements()*factor;
		values[2]=values[2]/this.getNumberOfMeasurements()*factor;
		return values;
	}

	
	public double[] getValuesRaw() throws IOException {
		byte status[] = new byte[1];
		// check status bit
		i2c.read(0x06, 1, status);
		if ((status[0] & 1) == 1) {
			i2c.read(0x00, 3, valuesRaw, false);
		} else {
			log.info("SensorQMC5883 data not ready");
		}
		return valuesRaw;
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
	
	/**
	 * @return the numberOfMeasurements
	 */
	public int getNumberOfMeasurements() {
		return numberOfMeasurements;
	}

	/**
	 * @param numberOfMeasurements the numberOfMeasurements to set
	 */
	public void setNumberOfMeasurements(int numberOfMeasurements) {
		this.numberOfMeasurements = numberOfMeasurements;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Magnetometer: ");
		sb.append(getMagnetometer());
		sb.append(System.lineSeparator());
		return sb.toString();
	}

}
