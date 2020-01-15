package ch.pschatzmann.jflightcontroller4pi.guidence.navigation;

import ch.pschatzmann.jflightcontroller4pi.guidence.navigation.coordinates.Coordinate3D;
import ch.pschatzmann.jflightcontroller4pi.guidence.navigation.coordinates.ICoordinate;

/**
 * Math to calculate distances, headings and coordinates
 * 
 * @author pschatzmann
 *
 */
public class Navigation3D implements INavigation {
	static public final double R = 6371.0; // Earch radius in km



	/**
	 * Determines the Bearing between two coordinates
	 * @return bearing in deg
	 */
	@Override
	public double getHeading(ICoordinate start, ICoordinate end) {
	    return Math.toDegrees((getHeadingRad(start, end)));
	}
	
	/**
	 * Determines the Bearing between two coordinates in rad
	 * @param src
	 * @param dst
	 * @return bearing in rad
	 */
	public double getHeadingRad(ICoordinate srcI, ICoordinate dstI) {
		Coordinate3D src = (Coordinate3D) srcI;
		Coordinate3D dst = (Coordinate3D) dstI;

	    double srcLat = src.latitude.rad();
	    double dstLat = dst.latitude.rad();
	    double dLng = dst.longitude.rad() - src.longitude.rad();

	    return Math.atan2(Math.sin(dLng) * Math.cos(dstLat),
	            Math.cos(srcLat) * Math.sin(dstLat) - Math.sin(srcLat) * Math.cos(dstLat) * Math.cos(dLng));
	}
	
	/**
	 * Determines the distance between two coordinates
	 * @param src
	 * @param dst
	 * @return distance in km
	 */
	@Override
	public double getDistance(ICoordinate srcI, ICoordinate dstI) {
		Coordinate3D src = (Coordinate3D) srcI;
		Coordinate3D dst = (Coordinate3D) dstI;

		double destLat = dst.latitude.rad();
		double srcLat = src.latitude.rad();
		double dLat = src.latitude.rad()-dst.latitude.rad();
		double dLng = src.longitude.rad()-dst.longitude.rad();

		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		        Math.cos(destLat) * Math.cos(srcLat) *
		        Math.sin(dLng/2) * Math.sin(dLng/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

		double d = R * c;
		return d;
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
		Coordinate3D src = (Coordinate3D) srcI;
		Coordinate3D dst = (Coordinate3D) dstI;
	
		double bR = Math.toRadians(bearing);
        double lat1R = src.latitude.rad();
        double lon1R = src.longitude.rad();
        double dR = distance / R;

        double a = Math.sin(dR) * Math.cos(lat1R);
        double lat2 = Math.asin(Math.sin(lat1R) * Math.cos(dR) + a * Math.cos(bR));
        double lon2 = lon1R
                + Math.atan2(Math.sin(bR) * a, Math.cos(dR) - Math.sin(lat1R) * Math.sin(lat2));

        dst.latitude.setRad(lat2);
	    dst.longitude.setRad(lon2);
	    
	}

	@Override
	public ICoordinate newCoordinate() {
		return new Coordinate3D();
	}

	@Override
	public ICoordinate newCoordinate(ICoordinate copySource) {
		return new Coordinate3D(((Coordinate3D)copySource).latitude.deg(), ((Coordinate3D)copySource).longitude.deg());
	};

}
