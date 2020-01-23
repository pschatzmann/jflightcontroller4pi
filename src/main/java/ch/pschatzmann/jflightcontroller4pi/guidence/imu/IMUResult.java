package ch.pschatzmann.jflightcontroller4pi.guidence.imu;

public class IMUResult {
	private double roll;
	private double pitch;
	private double yaw;

	public IMUResult(double pitch, double roll, double yaw){
		this.roll = roll;
		this.pitch = pitch;
		this.yaw = yaw;
	}

	/**
	 * @return the roll
	 */
	public double getRoll() {
		return roll;
	}

	/**
	 * @param roll the roll to set
	 */
	public void setRoll(double roll) {
		this.roll = roll;
	}

	/**
	 * @return the pitch
	 */
	public double getPitch() {
		return pitch;
	}

	/**
	 * @param pitch the pitch to set
	 */
	public void setPitch(double pitch) {
		this.pitch = pitch;
	}

	/**
	 * @return the yaw
	 */
	public double getYaw() {
		return yaw;
	}

	/**
	 * @param yaw the yaw to set
	 */
	public void setYaw(double yaw) {
		this.yaw = yaw;
	}
}
