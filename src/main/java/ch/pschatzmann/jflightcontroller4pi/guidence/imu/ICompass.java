package ch.pschatzmann.jflightcontroller4pi.guidence.imu;

/**
 * The compassmust provide the Degree and 
 * @author pschatzmann
 *
 */
public interface ICompass {
	public double getDegree(Value3D magnetometer);

}
