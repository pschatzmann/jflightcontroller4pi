package ch.pschatzmann.jflightcontroller4pi.imu;

/**
 * 3 D value: Accelerometer and Gyro and Magnetometers are providing their
 * output in values for the x, y and z axis. We use this 3 dimensions to
 * represent this output in one data structure;
 * 
 * @author pschatzmann
 *
 */
public class Value3D {
	long timestamp;
	double vector[] = new double[3];

	public Value3D() {
	}

	public Value3D(float x, float y, float z) {
		vector[0] = x;
		vector[1] = y;
		vector[2] = z;
	}

	public Value3D(float in[]) {
		vector[0] = in[0];
		vector[1] = in[1];
		vector[2] = in[2];
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

}
