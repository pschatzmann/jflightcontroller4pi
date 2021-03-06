package ch.pschatzmann.jflightcontroller4pi.guidence.imu;

/**
 * A IMU must provide a Quaternion
 * @author pschatzmann
 *
 */
public interface IIMU {
	public IMUResult getResult(double gx, double gy, double gz, double ax, double ay, double az, double mx, double my,
			double mz);

	public void setSampleFreq(float freq);
}
