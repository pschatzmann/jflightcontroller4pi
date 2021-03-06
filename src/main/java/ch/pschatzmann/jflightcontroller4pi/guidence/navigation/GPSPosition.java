package ch.pschatzmann.jflightcontroller4pi.guidence.navigation;

import ch.pschatzmann.jflightcontroller4pi.guidence.navigation.coordinates.Coordinate3D;

/**
 * Processing of GPS Sensor values
 * https://www.ridgesolutions.ie/index.php/2013/11/14/algorithm-to-calculate-speed-from-two-gps-latitude-and-longitude-points-and-time-difference/
 * 
 * @author pschatzmann
 *
 */
public class GPSPosition {
	private double timestamp; // : 235317.000
	private double longitude; // 4003.9040,N is latitude in degrees.decimal minutes, north
	private double latitude; // 10512.5792,W is longitude in degrees.decimal minutes, west
	private int quality;
	private double altitude;
	private double speed;

	public GPSPosition() {
	}

	public double getTime() {
		return timestamp;
	}

	public void setTime(double time) {
		this.timestamp = time;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		if (longitude!=null) {
			this.longitude = longitude;
		}
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		if (latitude!=null) {
			this.latitude = latitude;
		}
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int numberOfSatellites) {
		this.quality = numberOfSatellites;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	/**
	 * Calculates the speed in meters per second (mps)
	 * 
	 * @param prior
	 * @return
	 */
	public double getSpeed(GPSPosition prior) {
		double dist = getDistance(prior);
		double time_s = (this.timestamp - prior.timestamp) / 1000.0;
		return dist / time_s;
	}

	/**
	 * Convert mps to kilometers per hour
	 * 
	 * @param mps
	 * @return
	 */

	public double toKmh(double mps) {
		return (mps * 3600.0) / 1000.0;
	}

	/**
	 * Calculates the distance to a prior measurement in meters
	 * 
	 * @param prior
	 * @return
	 */
	public double getDistance(GPSPosition prior) {
		// Convert degrees to radians
		double lat1 = latitude * Math.PI / 180.0;
		double lon1 = longitude * Math.PI / 180.0;

		double lat2 = prior.getLatitude() * Math.PI / 180.0;
		double lon2 = prior.getLongitude() * Math.PI / 180.0;

		// radius of earth in metres
		double r = 6378100;

		// P
		double rho1 = r * Math.cos(lat1);
		double z1 = r * Math.sin(lat1);
		double x1 = rho1 * Math.cos(lon1);
		double y1 = rho1 * Math.sin(lon1);

		// Q
		double rho2 = r * Math.cos(lat2);
		double z2 = r * Math.sin(lat2);
		double x2 = rho2 * Math.cos(lon2);
		double y2 = rho2 * Math.sin(lon2);

		// Dot product
		double dot = (x1 * x2 + y1 * y2 + z1 * z2);
		double cos_theta = dot / (r * r);

		double theta = Math.acos(cos_theta);

		// Distance in Metres
		return r * theta;
	}

	/**
	 * Calculates the Bearing compared to a prior measurement in degrees
	 * 
	 * @param prior
	 * @return
	 */
	public double getDirection(GPSPosition prior) {
		double longDiff = this.getLongitude() - prior.getLongitude();
		double y = Math.sin(longDiff) * Math.cos(this.getLatitude());
		double x = Math.cos(prior.getLatitude()) * Math.sin(this.getLatitude())
				- Math.sin(prior.getLatitude()) * Math.cos(this.getLatitude()) * Math.cos(longDiff);
		return (Math.toDegrees((Math.atan2(y, x)) + 360) % 360);
	}
	
	/**
	 * Calculate the inclination angle in degrees
	 */
	public double getInclinationAngle(GPSPosition prior) {
		double distance = getDistance(prior);
		double deltaAltitude = prior.getAltitude() - this.getAltitude();
		return  Math.atan(deltaAltitude / distance);
	}
	

	/**
	 * Copy values
	 * @param fromValue
	 */
	public void setValue(GPSPosition fromValue) {
		this.timestamp = fromValue.timestamp;
		this.longitude = fromValue.longitude;
		this.latitude = fromValue.latitude;
		this.quality = fromValue.quality;
		this.altitude = fromValue.altitude;	
	}
	
	/**
	 * @return the speed (in Km/h)
	 */
	public double getSpeed() {
		return speed;
	}

	/**
	 * @param speed the speed to set (in Km/h)
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	
	
	public Coordinate3D getCoordinate3D() {
		return new Coordinate3D(this.longitude, this.latitude, this.altitude);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("time: ");
		sb.append(this.timestamp);
		sb.append(" long:");
		sb.append(longitude);
		sb.append(" lat:");
		sb.append(latitude);
		sb.append(" quality:");
		sb.append(quality);
		sb.append(" alt:");
		sb.append(altitude);
		sb.append(" speed:");
		sb.append(speed);
		return sb.toString();
	}


}
