package ch.pschatzmann.jflightcontroller4pi.guidence.imu;

/**
 * 3 D value: Accelerometer and Gyro and Magnetometers are providing their
 * output in values for the x, y and z axis. We use this 3 dimensions to
 * represent this output in one data structure.
 * 
 * @author pschatzmann
 *
 */
public class Value3D {
	long timestamp;
	double vector[] = new double[3];

	public Value3D() {
	}

	public Value3D(double x, double y, double z) {
		set(x, y, z);
	}

	/**
	 * Updates the x, y and z value
	 * @param x
	 * @param y
	 * @param z
	 */
	public void set(double x, double y, double z) {
		vector[0] = x;
		vector[1] = y;
		vector[2] = z;
	}

	public Value3D(double in[]) {
		set(in[0],in[1],in[2]);
	}

	public double[] vector() {
		return vector;
	}

	public double x() {
		return vector[0];
	}

	public double y() {
		return vector[1];
	}

	public double z() {
		return vector[2];
	}
	

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.x());
		sb.append("/");
		sb.append(this.y());
		sb.append("/");
		sb.append(this.z());
		return sb.toString();
	}

}
