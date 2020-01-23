package ch.pschatzmann.jflightcontroller4pi.guidence.imu;

/**
 * The compass must provide the Degree  
 * @author pschatzmann
 *
 */
public interface ICompass {
	public double getDegree(Value3D magnetometer);

}
