package ch.pschatzmann.jflightcontroller4pi.guidence.navigation.coordinates;

/**
 * Simplified geographical coordinates with x and y position. E.g we set home as 0/0 and the actual
 * coordinates are measured in km along the x and y axis
 * 
 * @author pschatzmann
 *
 */
public class Coordinate2D implements ICoordinate {
	public double x; //in km 
	public double y; //in km

	public Coordinate2D() {
		this(0.0, 0.0);
	}

	public Coordinate2D(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Coordinate2D(ICoordinate source) {
		this.x = ((Coordinate2D)source).x;
		this.y = ((Coordinate2D)source).y;
	}

	public String toString() {
		return x +"/"+y;
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}
}
