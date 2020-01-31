package ch.pschatzmann.jflightcontroller4pi.guidence.navigation;

import ch.pschatzmann.jflightcontroller4pi.guidence.navigation.coordinates.Coordinate2D;
import ch.pschatzmann.jflightcontroller4pi.guidence.navigation.coordinates.Coordinate3D;
import ch.pschatzmann.jflightcontroller4pi.guidence.navigation.coordinates.ICoordinate;

/**
 * We use the informaton from the IMU to determine the current location. This is just giving a very rough
 * estimate and is not reliable at all. However it can be used as a emergency procedure if the contact to the
 * remote control is lost to trigger a return to home which should bring the RC into reach again. 
 *  
 * @author pschatzmann
 *
 */

public class CompassNavigation {
	private double lastSpeed = 0;
	private long ms;
	private long startTime;
	private double totalDistanceKm;
	private INavigation navigation = new Navigation2D();
	private ICoordinate actualPos = navigation.newCoordinate();
	private ICoordinate homePos = navigation.newCoordinate();
	private Coordinate3D homePos3D = new Coordinate3D(46.2097, 7.2572, 0.0);

	/**
	 * Performs update with the info from the IMU
	 * @param bearing
	 * @param speedKmh
	 */
	public void recordHeading(double bearing, double speedKmh) {
		long timeMs = getTimeDifference();
		this.ms = System.currentTimeMillis();
		if (startTime==0) {
			startTime = this.ms;
		}
		double avgSpeed = getAvgSpeed(speedKmh);
		double timeH = msToH(timeMs) ;
		double distanceKm = avgSpeed * timeH;
		// we record the total distance since the start
		totalDistanceKm += distanceKm; 
		
		// finally we determine the new estimated position
		navigate(bearing, distanceKm);		
	}
	
	/**
	 * Determines the Bearing to return home
	 * @return
	 */
	public double getHomeBearing() {
		double result = navigation.getHeading(actualPos, homePos);
		return result > 0 ? result : result + 360.0;
	}
	

	/**
	 * Determines the Distance to return home
	 * @return distance in km
	 */
	public double getHomeDistance() {
		return navigation.getDistance(actualPos, homePos);
	}
	

	/**
	 * Determines the Altidude difference to return home
	 * @return distance in km
	 */
	public double getHomeAltidudeDifference() {
		return navigation.getAltitudeDifference(actualPos, homePos);
	}
	
	
	/**
	 * Determines the flight time to return to home in hours
	 * @return
	 */
	public double getHomeTimeH() {
		return  getHomeDistance() / getTotalAvgSpeed();
	}

	/**
	 * Determines the flight time to return to home in seconds
	 * @return
	 */
	public double getHomeTimeS() {
		return  getHomeTimeH() * 60 * 60;
	}
	
	/**
	 * Returns the coordinates of the current position in 2d coordinates 
	 * @return
	 */
	public ICoordinate getPosition() {
		return this.actualPos;
	}	
	
	/**
	 * Translates the 2D coordinates to 3D coordinates. 
	 * @return
	 */
	public Coordinate3D toPosition3D(ICoordinate pos) {
		ICoordinate result = pos;
		if (pos instanceof Coordinate2D) {
			double earthDiameter = 12742; // km = 360 deg
			double x = this.homePos3D.getX() + pos.getX()/12742.0/360.0;
			double y = this.homePos3D.getY() + pos.getY()/12742.0/360.0;
			result = new Coordinate3D(x,y, pos.getAltitude());
		}
		return (Coordinate3D) result;
	}	
	
	/**
	 * Defines the initial home coordinates
	 * @param lat
	 * @param lng
	 */
	public void setHomePosition(ICoordinate c) {
		if (c instanceof Coordinate3D) {
			this.homePos3D = (Coordinate3D) c;
		} else {
			this.homePos = c;
		}
		this.actualPos = navigation.newCoordinate(c);
	}
	
	/**
	 * Defines the current posision as home
	 */
	public void setHomePosition() {
		this.homePos = navigation.newCoordinate(this.actualPos);
	}

	
	protected double getAvgSpeed(double speed) {
		double result = (lastSpeed + speed) / 2.0;
		lastSpeed = speed;
		return result;
	}

	protected long getTimeDifference() {
		long time = System.currentTimeMillis();
		long result = time - ms;
		return startTime!=0 ? result : 0;
	}

	/**
	 * Updates the current posision
	 * @param bearing
	 * @param distance
	 */
	public void navigate(double bearing, double distance) {
		navigate(actualPos, bearing, distance);
	};

	/**
	 * 	Updates the coordinate using the start coordinate, the bearing and distance
	 * @param start
	 * @param bearing in deg
	 * @param distance in km
	 */
	public void navigate(ICoordinate coordinate, double bearing, double distanceKm) {
		navigation.navigate(coordinate, bearing, distanceKm, coordinate);
	}

	/**
	 * Calculates the total average speed since the start of the processing
	 * @return
	 */
	public double getTotalAvgSpeed() {
		// speed = distance (km) / time (h)
		double timeH = msToH(System.currentTimeMillis() - startTime);
		return totalDistanceKm / timeH;
	}
	
	/**
	 * Converts milliseconds to hours
	 * @param ms
	 * @return
	 */
	public static double msToH(long ms) {
		return ms / 1000.0 / 60.0 / 60.0;
	}

	/**
	 * Returns the implementation of the navigation api
	 * @return the navigation
	 */
	public INavigation getNavigation() {
		return navigation;
	}

	/**
	 * Defines the implementation of the navigation api. Per default we use the simplified
	 * 2d api.
	 * @param navigation the navigation to set
	 */
	public void setNavigation(INavigation navigation) {
		this.navigation = navigation;
		this.homePos = navigation.newCoordinate();
	}

	

}
