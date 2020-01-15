package ch.pschatzmann.jflightcontroller4pi.integration;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

/**
 * Utility functions to write and read register values from i2c sensors
 * 
 * @author pschatzmann
 *
 */
public class I2C {
	private static final Logger log = LoggerFactory.getLogger(I2C.class);
	private I2CDevice device;

	/**
	 * Setup itc device at indicated bus and address
	 * 
	 * @param bus
	 * @param addr
	 */
	public I2C(int bus, int addr) {
		try {
			I2CBus i2c = I2CFactory.getInstance(bus);
			device = i2c.getDevice(addr);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	/**
	 * Setup itc device at indicated address at default bus (BUS_1)
	 * 
	 * @param addr
	 */
	public I2C(int addr) {
		try {
			I2CBus i2c = I2CFactory.getInstance(I2CBus.BUS_1);
			device = i2c.getDevice(addr);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

//	/**
//	 * Writes a single byte
//	 * 
//	 * @param b
//	 * @throws IOException
//	 */
//	public void write(byte b) throws IOException {
//		if (device != null) {
//			device.write(b);
//		}
//	}

	/**
	 * Writes a byte at the address
	 * 
	 * @param i
	 * @param j
	 * @throws IOException
	 */
	public void write(int addr, byte b2) throws IOException {
		if (device != null) {
			device.write(addr, b2);
		}
	}

	/**
	 * Reads an indicated number of bytes at the indicated address
	 * 
	 * @param addr
	 * @param len
	 * @param buffer
	 * @throws IOException
	 */
	public void read(int addr, int len, byte[] buffer) throws IOException {
		if (device != null) {
			device.read(addr, buffer, 0, len);
		}
	}

	/**
	 * Reads an indicated number of bytes (*2) at the indicated address and
	 * converts them to 2 byte shorts
	 * 
	 * @param addr
	 * @param len
	 * @param buffer
	 * @throws IOException
	 */
	public void read(int addr, int len, short[] buffer) throws IOException {
		byte byteBuffer[] = new byte[len * 2];
		read(addr, len * 2, byteBuffer);
		for (int i = 0; i < len; i++) {
			buffer[i] = (short) (((byteBuffer[i * 2] << 8) | (byteBuffer[(i * 2) + 1])));
		}
	}

	/**
	 * Java does not support unsigned ints. So we are forced to use int
	 * (constructed of 2 bytes) instead instead
	 * 
	 * @param addr
	 * @param len
	 * @param buffer
	 * @throws IOException
	 */
	public void read(int addr, int len, int[] buffer) throws IOException {
		byte byteBuffer[] = new byte[len * 2];
		read(addr, len * 2, byteBuffer);
		for (int i = 0; i < len; i++) {
			buffer[i] = (int) (((byteBuffer[i * 2] << 8) | (byteBuffer[(i * 2) + 1])));
		}
	}

	/**
	 * Java does not support unsigned ints. So we use doubles (constructed of 2
	 * bytes) instead instead
	 * 
	 * @param addr
	 * @param len
	 * @param buffer
	 * @throws IOException
	 */
	public void read(int addr, int len, double[] buffer) throws IOException {
		byte byteBuffer[] = new byte[len * 2];
		read(addr, len * 2, byteBuffer);
		for (int i = 0; i < len; i++) {
			buffer[i] = (double) (((byteBuffer[i * 2] << 8) | (byteBuffer[(i * 2) + 1])));
		}
	}

	/**
	 * Reads 3 bytes from the indicated address and converts it into a float
	 * @param addr
	 * @return
	 * @throws IOException
	 */
	public float read3(int addr) throws IOException {
		byte byteBuffer[] = new byte[3];
		read(addr, 3, byteBuffer);
		return (float) ((byteBuffer[0] << 16 | byteBuffer[1] << 8 | byteBuffer[2]) / 256.0);
	}

	/**
	 * Pause for the indicated number of milliseconds
	 * @param ms
	 */
	public void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ex) {
			log.error(ex.getMessage(), ex);
		}
	}

}
