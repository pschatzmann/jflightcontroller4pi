package ch.pschatzmann.jflightcontroller4pi.data;

/**
 * Payload - Sensors are producing data at quite a big rate. We can leave the cleanup up to 
 * Java itself (garbage collection with DataOfString) or we can try to manage it ourself
 * with the Data class.
 * 
 * 
 * @author pschatzmann
 *
 */
public interface IData {

	@Override
	String toString();

	void append(double scaledValue);

	void append(String delimiter);

	boolean isEmpty();

	byte[] getBytes();

	void close();

	void setBytes(byte[] array);

	double[] splitDouble(char delimiter);
	
	int length();

}
