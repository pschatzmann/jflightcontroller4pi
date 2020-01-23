package ch.pschatzmann.jflightcontroller4pi.guidence.imu.sensors;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.control.IFilter;
import ch.pschatzmann.jflightcontroller4pi.control.MedianFilter;
import ch.pschatzmann.jflightcontroller4pi.devices.IMUDevice;
import ch.pschatzmann.jflightcontroller4pi.devices.ISensor;
import ch.pschatzmann.jflightcontroller4pi.integration.I2C;
import ch.pschatzmann.jflightcontroller4pi.integration.I2C.Type2Byte;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

/**
 * Barometric pressure sensor. This is used to update the current altitude and
 * temperature parameter values. .
 * https://cdn-shop.adafruit.com/datasheets/BST-BMP180-DS000-09.pdf
 * http://wmrx00.sourceforge.net/Arduino/BMP085-Calcs.pdf
 * 
 * @author pschatzmann
 *
 */
public class SensorBMP180 implements ISensor {
	private static final Logger log = LoggerFactory.getLogger(SensorBMP180.class);
	final static int AC1 = 0;
	final static int AC2 = 1;
	final static int AC3 = 2;
	final static int AC4 = 3;
	final static int AC5 = 4;
	final static int AC6 = 5;
	final static int VB1 = 6;
	final static int VB2 = 7;
	final static int MB = 8;
	final static int MC = 9;
	final static int MD = 10;

	private I2C i2c = new I2C(0x77); // (Accelerometers/Gyroscope)
	private FlightController flightController;
	private int values[] = new int[11];
	private double value[] = new double[1];
	private double md, mc, a, b1, c3, c4, c6, c5, p0, p1, p2, cx0, cx1, cx2, cy0, cy1, cy2;
	private double baselinePressure;
	private double baro_temp_c, pressure_pa;
	private boolean setupTemp = true;
	private IFilter medianFilter = new MedianFilter(1); // no filtering

	@Override
	public void setup(FlightController flightController) {
		try {
			log.info("setup " + this.getName());
			setupTemp = true;
			this.flightController = flightController;
			// configure the BMP180 (barometer)
			i2c.write(0xe0, (byte) 0xb6); // reset
			byte[] bytes = new byte[22];
			i2c.read(0xAA, 22, bytes);
			// convert to ints
			i2c.toIntArray(bytes, values, 11);
			// special logic for unsigned values
			values[AC4] = i2c.toIntUnsigned(bytes[6], bytes[7]);
			values[AC5] = i2c.toIntUnsigned(bytes[8], bytes[9]);
			values[AC6] = i2c.toIntUnsigned(bytes[10], bytes[11]);
			logPoly();

			// Compute floating-point polynominals:
			c3 = 160.0 * Math.pow(2, -15) * values[AC3];
			c4 = Math.pow(10, -3) * Math.pow(2, -15) * values[AC4];
			b1 = Math.pow(160, 2) * Math.pow(2, -30) * values[VB1];
			c5 = (Math.pow(2, -15) / 160.0) * values[AC5];
			c6 = values[AC6];
			mc = (Math.pow(2, 11) / Math.pow(160, 2)) * values[MC];
			md = (double) values[MD] / 160.0;
			cx0 = values[AC1];
			cx1 = 160.0 * Math.pow(2, -13) * values[AC2];
			cx2 = Math.pow(160, 2) * Math.pow(2, -25) * values[VB2];
			cy0 = (double) c4 * Math.pow(2.0, 15);
			cy1 = (double) c4 * c3;
			cy2 = (double) c4 * b1;
			p0 = (3791.0 - 8.0) / 1600.0;
			p1 = 1.0 - 7357.0 * Math.pow(2, -20);
			p2 = 3038.0 * 100.0 * Math.pow(2, -36);
			logFactors();

			calibrate();
			if (this.flightController != null) {
				log.info("The current baseline presure is set to {}", baselinePressure);
				flightController.setValue(ParametersEnum.PRESSUREBASELINE, baselinePressure);
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	private void logPoly() {
		log.info("AC1: {}", values[AC1]);
		log.info("AC2: {}", values[AC2]);
		log.info("AC3: {}", values[AC3]);
		log.info("AC4: {}", values[AC4]);
		log.info("AC5: {}", values[AC5]);
		log.info("AC6: {}", values[AC6]);
		log.info("VB1: {}", values[VB1]);
		log.info("VB2: {}", values[VB2]);
		log.info("MB: {}", values[MB]);
		log.info("MC: {}", values[MC]);
		log.info("MD: {}", values[MD]);
	}

	private void logFactors() {
		log.info("c3: {}", c3);
		log.info("c4: {}", c4);
		log.info("c5: {}", c5);
		log.info("c6: {}", c6);
		log.info("b1: {}", b1);
		log.info("mc: {}", mc);
		log.info("md: {}", md);

		log.info("x0: {}", cx0);
		log.info("x1: {}", cx1);
		log.info("x2: {}", cx2);
		log.info("y0: {}", cy0);
		log.info("y1: {}", cy1);
		log.info("y2: {}", cy2);
		log.info("p0: {}", p0);
		log.info("p1: {}", p1);
		log.info("p2: {}", p2);

	}

	public void calibrate() {
		log.info("calibrate");
		try {
			calculateBaseline(100);
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
	}
	
	/**
	 * Calculates the average of 100 measurements and records the result as
	 * baseline.
	 * 
	 * @throws IOException
	 */
	public double calculateBaseline(int count) throws IOException {
		setupTemp = true;
		float total = 0.0f;
		float countF = 0;
		for (int j = 0; j < count; j++) {
			double p = getValues()[0];
			if (Double.isFinite(p)) {
				total += p;
				countF += 1;
			}
			try {
				Thread.sleep(100);
			} catch(Exception ex) {}
		}
		baselinePressure = total / countF;
		return baselinePressure;
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
			if (flightController != null) {
				flightController.setValue(ParametersEnum.SENSORPRESSURE, values[0]);
				flightController.setValue(ParametersEnum.TEMPERATURE, values[1]);
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	

	public double[] getValues() throws IOException {
		if (setupTemp) {
			log.info("Determining temperature");
			setupTemp = false;
			// start conversion of the temperature sensor
			i2c.write(0xF4, (byte) 0x2E);
			i2c.sleep(5);
			int[] tu = new int[1];
			i2c.read(0xF6, 1, tu, Type2Byte.Unsigned);
	
			// extract the raw value
			double a = c5 * (tu[0] - c6);
			baro_temp_c = (double) (a + (mc / (a + md)));
			log.info("Temperature is {}", baro_temp_c);
			
		}

		// start conversion of the pressure sensor
		i2c.write(0xF4, (byte) 0xB4); // 0x34 | 1<<6);
		i2c.sleep(12);
		double pu = i2c.read3(0xF6);

		double s = baro_temp_c - 25.0;
		double x = (cx2 * Math.pow(s, 2)) + (cx1 * s) + cx0;
		double y = (cy2 * Math.pow(s, 2)) + (cy1 * s) + cy0;
		double z = (pu - x) / y;

		// convert the pressure reading
		double pressure = (double) ((p2 * Math.pow(z, 2)) + (p1 * z) + p0);
		
		// filter value if necessary
		this.medianFilter.add(pressure);
		pressure_pa = this.medianFilter.getValue();
		
		double result[] = { pressure_pa, baro_temp_c };
		return result;
	}

	/**
	 * Provides the pressure from the last processInput() call
	 * 
	 * @return pressure in pascal
	 */
	public double getPressure() {
		return this.pressure_pa;
	}
	
	/**
	 * Returns the baseline pressure (which was meausured on the startup
	 * @return
	 */
	public double getBaselinePressure() {
		return this.baselinePressure;
	}

	/**
	 * Provides temperature from the last processInput() call
	 * 
	 * @return temperature in celsius
	 */
	public double getTemperature() {
		return this.baro_temp_c;
	}

	/**
	 * @return the medianFilter
	 */
	public IFilter getFilter() {
		return medianFilter;
	}

	/**
	 * @param medianFilter the medianFilter to set
	 */
	public void setFilter(IFilter medianFilter) {
		this.medianFilter = medianFilter;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Pressure: ");
		sb.append(getPressure());
		sb.append(System.lineSeparator());
		sb.append("Temperature: ");
		sb.append(getTemperature());
		sb.append(System.lineSeparator());
		return sb.toString();
	}

}
