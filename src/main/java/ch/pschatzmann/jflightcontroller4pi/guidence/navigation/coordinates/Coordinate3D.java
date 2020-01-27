package ch.pschatzmann.jflightcontroller4pi.guidence.navigation.coordinates;

/**
 * Geographical coordinates with latidude and longitude
 * 
 * @author pschatzmann
 *
 */
public class Coordinate3D implements ICoordinate {
	public Coordinate3DValue latitude; // latitude
	public Coordinate3DValue longitude;  // longitude

	public Coordinate3D() {
		this(0.0, 0.0);
	}

	/**
	 * Add latutude and longitude as 009° 07′ 11″ or 009 07 11
	 * @param lat
	 * @param lng
	 */
	public Coordinate3D(String lat, String lng){
		this.latitude = new Coordinate3DValue(lat);
		this.longitude = new Coordinate3DValue(lng);
	}

	public Coordinate3D(Coordinate3DValue lat, Coordinate3DValue lng){
		this.latitude = lat;
		this.longitude = lng;
	}
	
	public Coordinate3D(double lat, double lng) {
		this.latitude = new Coordinate3DValue(lat);
		this.longitude = new Coordinate3DValue(lng);
	}


	public String toString() {
		return longitude +"/"+latitude;
	}

	@Override
	public double getX() {
		return longitude.deg();
	}

	@Override
	public double getY() {
		return latitude.deg();
	}
}
