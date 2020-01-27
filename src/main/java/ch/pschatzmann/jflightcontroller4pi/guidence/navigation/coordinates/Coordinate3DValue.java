package ch.pschatzmann.jflightcontroller4pi.guidence.navigation.coordinates;

/**
 * Helper class to deal with deg, min and sec and the conversion into decimal
 * values
 * 
 * @author pschatzmann
 *
 */
public class Coordinate3DValue {
	double deg;

	public Coordinate3DValue(double decimal) {
		setDeg(decimal);
	}

	public Coordinate3DValue(int deg, int min, int sec) {
		setDeg(deg,min,sec);
	}

	public Coordinate3DValue(String coordinate) {
		Coordinate3DValue result = null;
		if (coordinate.contains(".")) {
			setDeg(Double.parseDouble(coordinate));
		} else {
			String c = coordinate.replaceAll("'", " ");
			c = c.replaceAll("″", " ");
			c = c.replaceAll(" ", " ");
			c = c.replaceAll("′", " ");
			c = c.replaceAll("°", " ");
			c = c.replaceAll("  ", " ");
			String sa[] = c.split(" ");
			setDeg(value(sa,0),value(sa,1),value(sa,2));
		}
	}

	protected static int value(String[] sa, int i) {
		String str = sa[i].trim();
		return i < sa.length ? Integer.parseInt(str) : 0 ;
	}


	public void setDeg(double decimal) {
		this.deg = decimal;
	}
	
	public void setRad(double rad) {
		this.deg = Math.toDegrees(rad);
	}

	public void setDeg(int deg, int min, int sec) {
		this.deg = deg + min / 60.0 + sec / 3600.0;
	}

	public double deg() {
		return deg;
	}

	public double rad() {
		return Math.toRadians(deg);
	}

	public int[] getDegMinSec() {
		int result[] = new int[3];
		result[0] = (int) deg;
		result[1] = (int) (60 * (deg - result[0]));
		result[2] = (int) Math.round((3600 * ((deg - result[0])) - (60 * result[1])));
		return result;
	}

	public String toString() {
		return String.valueOf(deg);

	}

}
