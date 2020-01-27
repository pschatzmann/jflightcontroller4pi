package ch.pschatzmann.jflightcontroller4pi.guidence.navigation;

import ch.pschatzmann.jflightcontroller4pi.guidence.navigation.coordinates.Coordinate2D;
import ch.pschatzmann.jflightcontroller4pi.guidence.navigation.coordinates.ICoordinate;

/**
 * Simplified Math to calculate distances, headings and coordinates. Because we just deal with
 * short distances we can use the math in 2 dimensional space. The 2D coordinates are specified as 
 * x and y distances in km.
 * 
 * @author pschatzmann
 *
 */
public class Navigation2D implements INavigation {

	/**
	 * Determines the Bearing between two coordinates
	 * @return bearing in deg
	 */
	@Override
	public double getHeading(ICoordinate src, ICoordinate dst) {
		Coordinate2D start = (Coordinate2D) src;
		Coordinate2D end = (Coordinate2D) dst;
	    double result = Math.toDegrees(getHeadingRad(start, end));
	    return result >= 0.0 ? result : result+360; 
	}
	
	/**
	 * Determines the Bearing between two coordinates in rad
	 * @param src
	 * @param dst
	 * @return bearing in rad
	 */
	public double getHeadingRad(ICoordinate srcI, ICoordinate dstI) {
		Coordinate2D src = (Coordinate2D) srcI;
		Coordinate2D dst = (Coordinate2D) dstI;
	    double dx = dst.x - src.x;
	    double dy = dst.y - src.y;
	    return Math.atan2(Math.toRadians(dy), Math.toRadians(dx));
	}
	
	/**
	 * Determines the distance between two coordinates
	 * @param src
	 * @param dst
	 * @return distance in km
	 */
	public double getDistance(ICoordinate srcI, ICoordinate dstI) {
		Coordinate2D src = (Coordinate2D) srcI;
		Coordinate2D dst = (Coordinate2D) dstI;
	    double dx = dst.x - src.x;
	    double dy = dst.y - src.y;
		return Math.sqrt(Math.pow(dx,2) + Math.pow(dy,2));
	}
	
	/**
	 * Calculates the result coordinate from a start coordinate, the bearing and distance
	 * @param src
	 * @param bearing in deg
	 * @param distance in km
	 * @param dst
	 */
	@Override
	public void navigate(ICoordinate srcI, double bearing, double distance, ICoordinate dstI) {
		Coordinate2D src = (Coordinate2D) srcI;
		Coordinate2D dst = (Coordinate2D) dstI;
	    double x = distance * Math.cos(Math.toRadians(bearing));
	    double y = distance * Math.sin(Math.toRadians(bearing));
	    dst.x = x + src.x;
	    dst.y = y + src.y;
	};
	
	@Override
	public ICoordinate newCoordinate() {
		return new Coordinate2D();		
	}

	@Override
	public ICoordinate newCoordinate(ICoordinate copySource) {
		return new Coordinate2D(copySource);		
	}

	@Override
	public double getAltitudeDifference(ICoordinate start, ICoordinate end) {
		return end.getAltitude() - start.getAltitude();
	}
}
