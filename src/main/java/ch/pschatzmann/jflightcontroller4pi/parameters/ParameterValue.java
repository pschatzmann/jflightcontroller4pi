package ch.pschatzmann.jflightcontroller4pi.parameters;

/**
 * Individual double value which was determined for the specified timestamp
 * 
 * @author pschatzmann
 *
 */
public class ParameterValue {
	public long timestamp;
	public double value;
	
	public boolean isEmpty() {
		return timestamp==0;
	}
	
	@Override
	public String toString() {
		return Double.toString(value);
	}
}
