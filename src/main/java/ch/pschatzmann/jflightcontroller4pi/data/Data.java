package ch.pschatzmann.jflightcontroller4pi.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data class which is reusing the underlying byte buffer and does not need any
 * garbage collection.
 * 
 * This might be useful to be used by the DatagramWriter and DatagramReader and
 * by the Sensors which are constantly producing data.
 * 
 * We also need to make sure that the data is sent out is terminating with a
 * '\n' and that we recognize data w/o content correctly.
 * 
 * @author pschatzmann
 *
 */

public class Data implements IData {
	private static Logger log = LoggerFactory.getLogger(Data.class);
	private int size;
	private static List<Data> free = new ArrayList<Data>();
	private static int maxLength = 1024;
	private byte[] string = new byte[maxLength];
	private static double[] empty = {};

	@Override
	public byte[] getBytes() {
		return this.string;
	}

	/**
	 * make sure that all arrays are ended with \n
	 */

	@Override
	public void setBytes(byte[] b) {
		this.size = 0;
		this.append(b);
	}

	/**
	 * Provides the numbers which are contained in the string
	 * 
	 * @param delimiter
	 * @return
	 */
	@Override
	public double[] splitDouble(char delimiter, int expectedLen) {
		double[] result = empty;
		if (count(delimiter) + 1 == expectedLen) {
			result = new double[count(delimiter) + 1];
			String sa[] = this.toString().split(String.valueOf(delimiter));
			for (int j = 0; j < expectedLen; j++) {
				if (j < sa.length && !sa[j].trim().isEmpty()) {
					// filightgear sends numbers like 157!.8.0 which is not
					// good+
					String str = sa[j];
					int pos1 = str.lastIndexOf(".");
					int pos2 = str.indexOf(".");
					if (pos1 > pos2) {
						str = str.substring(0, pos2);
					}

					result[j] = Double.parseDouble(str);
				}
			}

		}
		return result;
	}

	/**
	 * Counts the number of the indicted characters in the string
	 * 
	 * @param c
	 * @return
	 */
	protected int count(char c) {
		int count = 0;
		for (int j = 0; j < size; j++) {
			if (string[j] == c) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Convenience Method to return a standard java String
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return new String(string, 0, size);
	}

	protected void append(byte c) {
		byte b = c;
		if (size + 1 >= string.length) {
			throw new RuntimeException("Please increase the maxLength in the XString class");
		}
		string[size] = b;
		size++;
		string[this.size] = '\n';

	}

	protected void append(char c) {
		byte b = (byte) c;
		append(b);
	}

	public void append(byte ba[]) {
		// remove leading spaces
		int start = 0;
		for (int j = 0; j < ba.length; j++) {
			if (ba[j] == ' ') {
				start = j + 1;
			} else {
				break;
			}
		}
		// copy characters before \n
		for (int j = start; j < ba.length; j++) {
			if (ba[j] == '\n' || ba[j] == '\r') {
				string[size] = '\n';
				return;
			}
			this.append(ba[j]);
		}
	}

	protected void append(IData s) {
		this.append(s.getBytes());
	}

	@Override
	public void append(String s) {
		this.append(s.getBytes());
	}

	@Override
	public boolean isEmpty() {
		if (this.size == 0)
			return true;
		for (int j = 0; j < 5; j++)
			if (string[j] != ' ')
				return false;

		return true;
	}

	protected String substring(int i) {
		return new String(this.string, i, size);
	}

	@Override
	public void append(double v) {
		this.append(String.valueOf(v));
	}

	/**
	 * Returns a new String (from the available strings)
	 * 
	 * @return
	 */
	protected static synchronized IData instance() {
		if (free.size() > 0) {
			Data s = free.remove(0);
			if (s != null) {
				s.size = 0;
				return s;
			}
		}
		log.debug("Creating new Data record");
		Data s = new Data();
		s.string = new byte[maxLength];
		s.size = 0;
		return s;

	}

	/**
	 * Releases the memory by putting it back to the available strings
	 */
	@Override
	public synchronized void close() {
		Arrays.fill(string, (byte) ' ');
		free.add(this);
	}

	@Override
	public int length() {
		return size;
	}

}
