package ch.pschatzmann.jflightcontroller4pi.guidence.imu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.guidence.imu.sensors.SensorMPU6050;

/**
 * Converts a 3d Magnetometer output into the 360 Degrees Headings. In order to
 * improve the correctness you should set the Declination of your location first.
 * 
 * @author pschatzmann
 *
 */
public class Compass implements ICompass {
	private static final Logger log = LoggerFactory.getLogger(SensorMPU6050.class);
	// Magnetic declination in Sion, Switzerland
	double declination = 2.45;
	// Earth's magnetic field at its surface ranges from 25 to 65 microteslas
	// (0.25 to 0.65 gauss)
	private double factor = 0.5 / Math.pow(2, 16);

	public enum Heading {
		North, NorthWest, West, SouthWest, South, SouthEast, East, NorthEast
	}

	/**
	 * Determine the heading in degrees from a magnetometer
	 * 
	 * @param values
	 * @return
	 */
	@Override
	public double getDegree(Value3D values) {
		// Calculate the real Gauss value for the X and Y axes from the amount
		// of LSBs returned where the LSB value by default is 0.48828125 mG,
		// resulting in 2048 LSBs per Gauss.
		double directionDeg = 0;

		double xGaussData = values.x() * factor;
		double yGaussData = values.y() * factor;

		// Calculate the direction D by first checking to see if the X Gauss
		// data is equal to 0 to prevent divide by 0 zero errors in the future
		// calculations. If the X Gauss data is 0, check to see if the Y Gauss
		// data is less than 0. If Y is less than 0 Gauss, the direction D is 90
		// degrees; if Y is greater than or equal to 0 Gauss, the direction D is
		// 0 degrees.
		if (xGaussData == 0) {
			log.info("nothing calculated because xGaussData is 0");
		} else if (xGaussData > 0) {
			if (yGaussData < 0) {
				directionDeg = 90;
			} else {
				directionDeg = 0;
			}
		} else {
			// If the X Gauss data is not zero, calculate the arctangent of the
			// Y Gauss and X Gauss data and convert from polar coordinates to
			// degrees.
			directionDeg = (float) Math.atan((yGaussData / xGaussData) * (180.0 / Math.PI));

			// If the direction D is greater than 360 degrees, subtract 360
			// degrees from that value.
			if (directionDeg > 360) {
				directionDeg -= 360;
			} else {
				// if the direction D is less than 0 degrees, add 360 degrees to
				// that value.
				if (directionDeg < 360) {
					directionDeg += 360;
				}
			}
		}
		return directionDeg;
	}

	/**
	 * This function allows the user to set a degree of magnetic declination to
	 * be used as compenstion in heading calculation. Input Param: newDec, can
	 * be entered as a signed integer Return: nothing
	 */
	public void setDeclination(int newDec) {
		declination = newDec;
	}

	/**
	 * Determines the conversion factor from sensor to gauss
	 * 
	 * @return the factor
	 */
	public double getFactor() {
		return factor;
	}

	/**
	 * Defines the conversion factor from sensor to gauss
	 * 
	 * @param factor
	 *            the factor to set
	 */
	public void setFactor(double factor) {
		this.factor = factor;
	}

	/**
	 * Returns the magnetic declination
	 * @return
	 */
	public double getDeclination() {
		return declination;
	}

	/**
	 * Defines the magnetic declination
	 * @param declination
	 */
	public void setDeclination(double declination) {
		this.declination = declination;
	}

}
