package ch.pschatzmann.jflightcontroller4pi.guidence.imu;

/**
 * Simple implementation which calculates the pitch, roll and yaw from the accellerometer
 * @author pschatzmann
 *
 */
public class SimpleIMU implements IIMU {

	@Override
	public void setSampleFreq(float freq) {
	}

	@Override
	public IMUResult getResult(double gx, double gy, double gz, double ax, double ay, double az, double mx, double my, double mz) {
		double pitch =  Math.atan (ax/Math.sqrt(ay*ay + az*az)); // roll
		double roll = Math.atan (ay/Math.sqrt(ax*ax + az*az)); // pitch
		double yaw = Math.atan (az/Math.sqrt(ax*ax + az*az));
		return new IMUResult(pitch,roll,yaw);
	}

}
