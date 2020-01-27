package ch.pschatzmann.jflightcontroller4pi.guidence.navigation.coordinates;

public interface ICoordinate {
	/**
	 * Returns the position of the x axes (or longitude)
	 * @return
	 */
	double getX();

	/**
	 * Returns the position of the y axes (or latitude)
	 * @return
	 */
	double getY();
	
	
	/**
	 * Returns the altitude in m
	 * @return
	 */
	double getAltitude();

}
