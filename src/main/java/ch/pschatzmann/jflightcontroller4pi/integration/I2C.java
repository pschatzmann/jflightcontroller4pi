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
	public enum Type2Byte {Signed, Unsigned, SignedReverse}

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
			if (I2CFactory.getBusIds().length>0) {
				I2CBus i2c = I2CFactory.getInstance(I2CBus.BUS_1);
				device = i2c.getDevice(addr);
			} else {
				log.warn("No I2C bus is available");
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

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
	 * Reads signed 2 byte values
	 * 
	 * @param addr
	 * @param len
	 * @param buffer
	 * @throws IOException
	 */
	public void read(int addr, int len, int[] buffer) throws IOException {
		read(addr, len, buffer, Type2Byte.Signed);
	}

	/**
	 * Reads singed or unsigned 2 byte values
	 * 
	 * @param addr
	 * @param len
	 * @param buffer
	 * @param signed
	 * @throws IOException
	 */
	public void read(int addr, int len, int[] buffer, Type2Byte signed) throws IOException {
		byte byteBuffer[] = new byte[len * 2];
		read(addr, len * 2, byteBuffer);
		switch(signed) {
		case Signed: 
			toIntArray(byteBuffer, buffer, len);
			break;
		case SignedReverse: 
			toIntReversedArray(byteBuffer, buffer, len);
			break;
		case Unsigned:
			toUnsignedIntArray(byteBuffer, buffer, len);	
			break;
		}
	}
	
	public void toIntReversedArray(byte[] byteBuffer, int[] buffer, int len) {
		for (int i = 0; i < len; i++) {
			buffer[i] = (int) toIntReversed(byteBuffer[i * 2], byteBuffer[(i * 2) + 1]);
		}
	}

	/**
	 * Converts an array of bytes to an array of (signed) ints
	 * @param byteBuffer
	 * @param buffer
	 * @param intLen
	 */
	public void toIntArray(byte[] byteBuffer, int[] buffer, int intLen) {		
		for (int i = 0; i < intLen; i++) {
			buffer[i] = (int) toInt(byteBuffer[i * 2], byteBuffer[(i * 2) + 1]);
		}
	}
	
	public void toUnsignedIntArray(byte[] byteBuffer, int[] buffer, int intLen) {		
		for (int i = 0; i < intLen; i++) {
			buffer[i] = (int) toIntUnsigned(byteBuffer[i * 2], byteBuffer[(i * 2) + 1]);
		}
	}

	
	/**
	 * Converts 2 bytes to a int
	 * @param b1
	 * @param b2
	 * @return
	 */
	public int toInt(byte b1, byte b2) {
		//return (int) (((b1 << 8) | (b2)));
		return (b1 * 256) + (b2 & 0xFF);
	}
	
	private int toIntReversed(byte b1, byte b2) {
		return (b2 * 256) + (b1 & 0xFF);
	}


	/**
	 * Converts a 2 byte unsigned c int to a java int
	 * @param b1
	 * @param b2
	 * @return
	 */
	public int toIntUnsigned(byte b1, byte b2) {
		return ((b1 & 0xFF) * 256) + (b2 & 0xFF);
	}

	/**
	 * Provides array of 2 byte ints to doubles so that we can use the values
	 * directly in floating point operations 
	 * @param addr
	 * @param len
	 * @param buffer
	 * @throws IOException
	 */
	public void read(int addr, int len, double[] buffer, Type2Byte signed) throws IOException {
		byte byteBuffer[] = new byte[len * 2];
		read(addr, len * 2, byteBuffer);
		for (int i = 0; i < len; i++) {
			switch(signed) {
			case Unsigned:
				buffer[i] = (double) toInt(byteBuffer[i * 2], byteBuffer[(i * 2) + 1]);
				break;
			case Signed:
				buffer[i] = (double) toIntUnsigned(byteBuffer[i * 2], byteBuffer[(i * 2) + 1]);				
				break;
			case  SignedReverse: 
				buffer[i] = (double) toInt(byteBuffer[(i * 2)+1], byteBuffer[i * 2]);				
				break;

			}
		}
	}

	/**
	 * Reads 3 bytes from the indicated address and converts it into a float
	 * @param addr
	 * @return
	 * @throws IOException
	 */
	public double read3(int addr) throws IOException {
		byte byteBuffer[] = new byte[3];
		read(addr, 3, byteBuffer);
		//return (float) ((byteBuffer[0] << 16 | byteBuffer[1] << 8 | byteBuffer[2]) / 256.0);
		//log.info("read3: {} {} {}", byteBuffer[0],byteBuffer[1],byteBuffer[2]);
		return (double) ((((byteBuffer[0]& 0xFF) * 256.0) + (byteBuffer[1]& 0xFF) + ((double)(byteBuffer[2]& 0xFF) / 256.0)));
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
