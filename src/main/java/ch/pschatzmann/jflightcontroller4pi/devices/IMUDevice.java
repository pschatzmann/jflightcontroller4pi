package ch.pschatzmann.jflightcontroller4pi.devices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.guidence.imu.Compass;
import ch.pschatzmann.jflightcontroller4pi.guidence.imu.ICompass;
import ch.pschatzmann.jflightcontroller4pi.guidence.imu.IIMU;
import ch.pschatzmann.jflightcontroller4pi.guidence.imu.IMU;
import ch.pschatzmann.jflightcontroller4pi.guidence.imu.IMUResult;
import ch.pschatzmann.jflightcontroller4pi.guidence.imu.Quaternion;
import ch.pschatzmann.jflightcontroller4pi.guidence.imu.SimpleIMU;
import ch.pschatzmann.jflightcontroller4pi.guidence.imu.Value3D;
import ch.pschatzmann.jflightcontroller4pi.guidence.imu.Velocity;
import ch.pschatzmann.jflightcontroller4pi.guidence.imu.sensors.SensorGY87;
import ch.pschatzmann.jflightcontroller4pi.guidence.navigation.CompassNavigation;
import ch.pschatzmann.jflightcontroller4pi.guidence.navigation.coordinates.ICoordinate;
import ch.pschatzmann.jflightcontroller4pi.guidence.imu.Compass.Heading;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParameterValue;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

/**
 * Integration device for the Inertial measurement unit (IMU) into our Flight
 * Controller to determine the pitch, roll, yaw and heading. Estimates the
 * speed; Estimates the position; Determines altidude above ground from the
 * measured air pressure difference.
 * 
 * The processing is running with it's own timer where the frequency is defined in the IMU.
 * 
 * 
 * @author pschatzmann
 *
 */
public class IMUDevice implements IOutDevice {
	private static Logger log = LoggerFactory.getLogger(IMUDevice.class);
	private FlightController flightController;
	private Value3D gyro = new Value3D();
	private Value3D accelerometer = new Value3D();
	private Value3D magnetometer = new Value3D();
	private IIMU imu = new SimpleIMU();
	private ICompass compass = new Compass();
	private Velocity speed = new Velocity();
	private CompassNavigation navigation = new CompassNavigation();
	private List<Map<String,Number>> history = new Vector();
	private long historySize = 0;
	private float sampleFreq = 200.0f;
	private double frequency;
	
	/**
	 * Default constructor
	 */
	public IMUDevice() {
	}
	
	/**
	 * Determine the roll pitch and yaw from the gyro,accelerometer and
	 * magnetometer
	 * 
	 * @return
	 */
	public IMUResult getResult() {
		return this.getImu().getResult(gyro.x(), gyro.y(), gyro.z(), accelerometer.x(), accelerometer.y(), accelerometer.z(), magnetometer.x(),
				magnetometer.y(), magnetometer.z());
	}

	/**
	 * Determines the direction in 360 degrees from the magnetometer
	 * 
	 * @return
	 */
	public double getDirection() {
		return compass.getDegree(this.magnetometer);
	}

	@Override
	public void setup(FlightController flightController) {
		log.info("setup");;
		if (flightController==null) {
			log.error("The flight controller must not be null");
			return;
		}

		
		this.imu.setSampleFreq(this.getSampleFreq());
		this.flightController = flightController;
		this.flightController.setImu(this);
		this.history.clear();
		
	}

	@Override
	public void shutdown() {
		log.info("shutdown");
		if (this.getFlightController()==null) {
			log.error("The IMUDevice has not been set up");
			return;
		}
		this.getFlightController().setImu(null);
	}


	/**
	 * Reads the sensor data and stores the calculated IMU data in the parameters
	 */

	@Override
	public void processOutput() {

		if (this.getFlightController()==null) {
			log.error("The IMUDevice has not been set up. The flight controller is null");
			return;
		}
		log.debug("processOutput");
		// load latest values from the parameters
		readParameters(gyro, ParametersEnum.GYROX, ParametersEnum.GYROY, ParametersEnum.GYROZ);
		readParameters(accelerometer, ParametersEnum.ACCELEROMETERX, ParametersEnum.ACCELEROMETERY, ParametersEnum.ACCELEROMETERZ);
		readParameters(magnetometer, ParametersEnum.MAGNETOMETERX, ParametersEnum.MAGNETOMETERY, ParametersEnum.MAGNETOMETERZ);

		// Calculate values
		IMUResult q = this.getResult();

		// Update the new IMU parameters
		double pitch = Math.toDegrees(q.getPitch());
		double roll = Math.toDegrees(q.getRoll());
		double yaw = Math.toDegrees(q.getYaw());
		setParameter(ParametersEnum.SENSORPITCH, pitch);
		setParameter(ParametersEnum.SENSORROLL,roll);
		setParameter(ParametersEnum.SENSORYAW, yaw);
		double direction = this.getDirection();
		setParameter(ParametersEnum.SENSORHEADING, direction);

		// estimate for velocity
		speed.recordAccelerometer(flightController, accelerometer);
		setParameter(ParametersEnum.SENSORSPEED, speed.getSpeed());

		// calculate the position
		navigation.recordHeading(this.getDirection(), speed.getSpeed());
		
		ICoordinate pos = navigation.toPosition3D(navigation.getPosition());
		setParameter(ParametersEnum.IMULATITUDE, pos.getX());
		setParameter(ParametersEnum.IMULONGITUDE, pos.getY());

		// calculate altitude above ground
		ParameterValue pressure = flightController.getValue(ParametersEnum.SENSORPRESSURE);
		ParameterValue base = flightController.getValue(ParametersEnum.PRESSUREBASELINE);
		double altitude = 0;
		if (base != null) {
			if (pressure != null) {
				altitude = altitude(pressure.value, base.value);
				setParameter(ParametersEnum.ALTITUDE, altitude);
			}
		}
		
		// make history available to analyse the data
		recordHistory(pitch, roll, yaw, direction, pos, pressure, altitude);
		
	}

	protected void recordHistory(double pitch, double roll, double yaw, double direction, ICoordinate pos, ParameterValue pressure, double altitude) {
		// record history
		if (this.getHistorySize()>0) {
			Map<String, Number> rec = new TreeMap();
			rec.put("TIME", System.currentTimeMillis());
			rec.put(ParametersEnum.GYROX.name(), gyro.x());
			rec.put(ParametersEnum.GYROY.name(), gyro.y());
			rec.put(ParametersEnum.GYROZ.name(), gyro.z());
			rec.put(ParametersEnum.ACCELEROMETERX.name(), accelerometer.x());
			rec.put(ParametersEnum.ACCELEROMETERY.name(), accelerometer.y());
			rec.put(ParametersEnum.ACCELEROMETERZ.name(), accelerometer.z());
			rec.put(ParametersEnum.MAGNETOMETERX.name(), magnetometer.x());
			rec.put(ParametersEnum.MAGNETOMETERY.name(), magnetometer.y());
			rec.put(ParametersEnum.MAGNETOMETERZ.name(), magnetometer.z());
			rec.put(ParametersEnum.SENSORPRESSURE.name(), pressure.value);
			
			rec.put(ParametersEnum.IMULATITUDE.name(), pos.getX());
			rec.put(ParametersEnum.IMULONGITUDE.name(), pos.getY());
			
			rec.put(ParametersEnum.ALTITUDE.name(), altitude);
			
			
			rec.put(ParametersEnum.SENSORPITCH.name(), pitch);
			rec.put(ParametersEnum.SENSORROLL.name(), roll);
			rec.put(ParametersEnum.SENSORYAW.name(), yaw);
			rec.put(ParametersEnum.SENSORHEADING.name(), direction);

			// adds the value to the histry
			this.history.add(rec);
			// limit the max number of records
			if (this.history.size()>=this.getHistorySize()) {
				this.history.remove(0);
			}
		}
	}

	/**
	 * Update the parameter in the parmeter Store
	 * 
	 * @param par
	 * @param value
	 */
	private void setParameter(ParametersEnum par, double value) {
		flightController.setValue(par, value);
	}

	/**
	 * Reads the sensor values from the parameter store
	 * 
	 * @param dev
	 * @param x
	 * @param y
	 * @param z
	 */
	private void readParameters(Value3D dev, ParametersEnum x, ParametersEnum y, ParametersEnum z) {
		double v[] = dev.vector();
		try {
			v[0] = flightController.getValue(x).value;
			v[1] = flightController.getValue(y).value;
			v[2] = flightController.getValue(z).value;
		} catch(Exception ex) {
			// ignore NPE
		}
	}

	public FlightController getFlightController() {
		return flightController;
	}

	public void setFlightController(FlightController flightController) {
		this.flightController = flightController;
	}

	public IIMU getImu() {
		return this.imu;
	}

	public void setImu(IIMU imu) {
		this.imu = imu;
	}

	/**
	 * The compass heading can then be determined by the direction value D: If D
	 * is greater than 337.25 degrees or less than 22.5 degrees – North If D is
	 * between 292.5 degrees and 337.25 degrees – North-West If D is between
	 * 247.5 degrees and 292.5 degrees – West If D is between 202.5 degrees and
	 * 247.5 degrees – South-West If D is between 157.5 degrees and 202.5
	 * degrees – South If D is between 112.5 degrees and 157.5 degrees –
	 * South-East If D is between 67.5 degrees and 112.5 degrees – East If D is
	 * between 0 degrees and 67.5 degrees – North-East
	 * 
	 */
	public Heading getHeading() {
		double degree = this.getDirection();
		// If D is greater than 337.25 degrees or less than 22.5 degrees – North
		if (degree > 337.25 || degree < 22.5)
			return Heading.North;
		// If D is between 292.5 degrees and 337.25 degrees – North-West
		if (degree >= 292.5 || degree < 337.25)
			return Heading.NorthWest;
		// If D is between 247.5 degrees and 292.5 degrees – West
		if (degree >= 247.5 || degree < 292.5)
			return Heading.West;
		// If D is between 202.5 degrees and 247.5 degrees – South-West
		if (degree >= 202.5 || degree < 247.5)
			return Heading.SouthWest;
		// If D is between 157.5 degrees and 202.5 degrees – South
		if (degree >= 157.5 || degree < 202.5)
			return Heading.SouthWest;
		// If D is between 112.5 degrees and 157.5 degrees – South-East
		if (degree >= 112.5 || degree < 157.5)
			return Heading.SouthWest;
		// If D is between 67.5 degrees and 112.5 degrees – East
		if (degree >= 167.5 || degree < 112.5)
			return Heading.SouthWest;
		// If D is between 0 degrees and 67.5 degrees – North-East
		if (degree >= 0 || degree < 67.5)
			return Heading.SouthWest;

		// this can not happen if the ranges above are correct
		throw new RuntimeException("Heading could not be determined");
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Given a pressure P (mb) taken at a specific altitude (meters), return the
	 * equivalent pressure (mb) at sea level. This produces pressure readings
	 * that can be used for weather measurements.
	 */

	public static double sealevel(double pressure, double altitude) {
		return (pressure / Math.pow(1 - (altitude / 44330.0), 5.255));
	}

	/**
	 * Given a pressure measurement P (mb) and the pressure at a baseline P0
	 * (mb), return altitude (meters) above baseline.
	 * 
	 * @param P
	 * @param P0
	 * @return
	 */
	public static double altitude(double P, double P0) {
		return (44330.0 * (1 - Math.pow(P / P0, 1 / 5.255)));
	}
	
	/**
	 * @return the history
	 */
	public List<Map<String, Number>> getHistory() {
		return new ArrayList(history);
	}

	/**
	 * @param history the history to set
	 */
	public void setHistory(List<Map<String, Number>> history) {
		this.history = history;
	}
	
	
	public void clearHistory() {
		this.history.clear();
	}

	/**
	 * @return the historySize
	 */
	public long getHistorySize() {
		return historySize;
	}

	/**
	 * @param historySize the historySize to set
	 */
	public void setHistorySize(long historySize) {
		this.historySize = historySize;
	}

	/**
	 * @return the sampleFreq
	 */
	public float getSampleFreq() {
		return sampleFreq;
	}

	/**
	 * @param sampleFreq the sampleFreq to set
	 */
	public void setSampleFreq(float sampleFreq) {
		this.sampleFreq = sampleFreq;
	}
	
	@Override
	public void setFrequency(double frequency) {
		this.frequency = frequency;
		
	}

	@Override
	public double getFrequency() {
		return this.frequency;
	}


	/**
	 * Prints all the relevant values 
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getName());
		if (flightController!=null) {
			sb.append(": ");
			sb.append(flightController.getValue(ParametersEnum.SENSORPITCH).value);
			sb.append("/");
			sb.append(flightController.getValue(ParametersEnum.SENSORROLL).value);
			sb.append("/");
			sb.append(flightController.getValue(ParametersEnum.SENSORYAW).value);
			sb.append(" ");
			sb.append(flightController.getValue(ParametersEnum.SENSORHEADING).value);
			sb.append(" ");
			sb.append(flightController.getValue(ParametersEnum.SENSORSPEED).value);
			sb.append(" ");
			sb.append(flightController.getValue(ParametersEnum.IMULATITUDE).value);
			sb.append("/");
			sb.append(flightController.getValue(ParametersEnum.IMULONGITUDE).value);
			sb.append(" ");
			sb.append(flightController.getValue(ParametersEnum.ALTITUDE).value);
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}


}
