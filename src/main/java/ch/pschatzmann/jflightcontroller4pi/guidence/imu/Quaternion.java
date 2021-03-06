package ch.pschatzmann.jflightcontroller4pi.guidence.imu;

/**
 * Quaternion to calculate the roll, pitch and yaw. see
 * https://en.wikipedia.org/wiki/Conversion_between_quaternions_and_Euler_angles
 * roll = Mathf.Atan2(2*y*w + 2*x*z, 1 - 2*y*y - 2*z*z); pitch =
 * Mathf.Atan2(2*x*w + 2*y*z, 1 - 2*x*x - 2*z*z); yaw = Mathf.Asin(2*x*y +
 * 2*z*w);
 *
 */
public class Quaternion {
	public float x, y, z, w;

	public Quaternion() {
	}

	public Quaternion(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getZ() {
		return z;
	}

	public void setZ(float z) {
		this.z = z;
	}

	public float getW() {
		return w;
	}

	public void setW(float w) {
		this.w = w;
	}

	protected float getRoll() {
		return (float) Math.atan2(2 * y * w + 2 * x * z, 1 - 2 * y * y - 2 * z * z);
	}

	protected float getPitch() {
		return (float) Math.atan2(2 * x * w + 2 * y * z, 1 - 2 * x * x - 2 * z * z);
	}

	protected float getYaw() {
		return (float) Math.asin(2 * x * y + 2 * z * w);
	}
	
	public IMUResult getResult() {
		return new IMUResult(getRoll(),getPitch(),getYaw());
	}

}
