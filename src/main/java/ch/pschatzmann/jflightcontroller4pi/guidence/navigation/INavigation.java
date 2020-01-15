package ch.pschatzmann.jflightcontroller4pi.guidence.navigation;

import ch.pschatzmann.jflightcontroller4pi.guidence.navigation.coordinates.ICoordinate;

public interface INavigation {
	/**
	 * Determines the distance between two coordinates
	 * @param start
	 * @param end
	 * @return
	 */
	double getDistance(ICoordinate start, ICoordinate end);

	/**
	 * Determines the Bearing between two coordinates
	 * @return bearing in deg
	 */
	double getHeading(ICoordinate start, ICoordinate end);

	/**
	 * Calculates the result coordinate from a start coordinate, the bearing and distance
	 * @param src
	 * @param bearing in deg
	 * @param distance in km
	 * @param dst
	 */
	void navigate(ICoordinate src, double bearing, double distance, ICoordinate dst);

	/**
	 * Creates an new empty coordinate position
	 * @return
	 */
	
	public ICoordinate newCoordinate();
	
	/**
	 * Creates a copy of the indicated coordinate position
	 * @param copySource
	 * @return
	 */

	public ICoordinate newCoordinate(ICoordinate copySource);

}