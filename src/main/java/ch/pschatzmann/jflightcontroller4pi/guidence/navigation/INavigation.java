package ch.pschatzmann.jflightcontroller4pi.guidence.navigation;

import ch.pschatzmann.jflightcontroller4pi.guidence.navigation.coordinates.ICoordinate;

/**
 * Navigation Interface to claculate heading and distances between coordinates
 * 
 * @author pschatzmann
 *
 */
public interface INavigation {
	/**
	 * Determines the distance (in km) between two coordinates
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	double getDistance(ICoordinate start, ICoordinate end);

	/**
	 * Determines the Bearing in degrees between two coordinates
	 * 
	 * @return bearing in deg
	 */
	double getHeading(ICoordinate start, ICoordinate end);

	/**
	 * Determines the difference in altidude in meters between two coordinates
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	double getAltitudeDifference(ICoordinate start, ICoordinate end);

	/**
	 * Calculates the result coordinate from a start coordinate, the bearing and
	 * distance
	 * 
	 * @param src
	 * @param bearing
	 *            in deg
	 * @param distance
	 *            in km
	 * @param dst
	 */
	void navigate(ICoordinate src, double bearing, double distance, ICoordinate dst);

	/**
	 * Creates an new empty coordinate position
	 * 
	 * @return
	 */

	public ICoordinate newCoordinate();

	/**
	 * Creates a copy of the indicated coordinate position
	 * 
	 * @param copySource
	 * @return
	 */

	public ICoordinate newCoordinate(ICoordinate copySource);

	/**
	 * Checks if the coordinates are equal (allowing the indicated difference
	 * 
	 * @param coordinate
	 * @param distanceInMeter
	 * @return
	 */
	default public boolean equals(ICoordinate coordinate, ICoordinate coordinate2, int distanceInMeter) {
		double meters = Math.abs(this.getDistance(coordinate, coordinate2) * 1000.0);
		return meters <= distanceInMeter;
	}

}