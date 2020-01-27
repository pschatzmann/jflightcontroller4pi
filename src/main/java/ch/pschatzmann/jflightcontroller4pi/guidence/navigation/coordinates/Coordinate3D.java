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
	public double altitude; // in m

	public Coordinate3D() {
		this(0.0, 0.0, 0.0);
	}

	/**
	 * Add latutude and longitude as 009° 07′ 11″ or 009 07 11
	 * @param lat
	 * @param lng
	 * @param altidude
	 */
	public Coordinate3D(String lat, String lng, double altidude){
		this.latitude = new Coordinate3DValue(lat);
		this.longitude = new Coordinate3DValue(lng);
		this.altitude = altidude;
	}

	public Coordinate3D(Coordinate3DValue lat, Coordinate3DValue lng, double altidude){
		this.latitude = lat;
		this.longitude = lng;
		this.altitude = altidude;
	}
	
	public Coordinate3D(double lat, double lng, double altidude) {
		this.latitude = new Coordinate3DValue(lat);
		this.longitude = new Coordinate3DValue(lng);
		this.altitude = altidude;
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

	/**
	 * @return the altitude
	 */
	public double getAltitude() {
		return altitude;
	}

	/**
	 * @param altitude the altitude to set
	 */
	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}
}
