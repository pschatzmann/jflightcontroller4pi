package ch.pschatzmann.jflightcontroller4pi.protocols;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import ch.pschatzmann.jflightcontroller4pi.data.DataFactory;
import ch.pschatzmann.jflightcontroller4pi.data.IData;
import ch.pschatzmann.jflightcontroller4pi.devices.GameConsole;

/**
 * I2C is a useful bus that allows data exchange between microcontrollers and
 * peripherals with a minimum of wiring. It potentially allows for many devices,
 * as long as their addresses don't conflict.
 * 
 * Pinout: https://github.com/uraimo/SwiftyGPIO/wiki/GPIO-Pinout
 * 
 * @author pschatzmann
 *
 */

public class InputFromPiI2C implements IPinIn {
    private static final Logger log = LoggerFactory.getLogger(InputFromPiI2C.class);
	private I2CDevice device;
	private byte dataAddresses[] = {(byte) 0x8C, (byte) 0x8E};
	private I2CBus i2c;

	public InputFromPiI2C(int bus, byte deviceNo, byte[]addresses ) {
		try {
			this.dataAddresses = addresses;
			
			// get the I2C bus to communicate on
			i2c = I2CFactory.getInstance(bus);

			device = i2c.getDevice(deviceNo);						

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public IData getValues() {
		try {
			IData str = DataFactory.instance();
			for (byte data : dataAddresses) {
				if (data!=dataAddresses[0]) {
					str.append(',');
				}
				int data0 = device.read(data);
				str.append((String.format("0x%02x", data0)));
			}
			return str;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void shutdown() {
		try {
			i2c.close();
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
	}

	public I2CDevice getDevice() {
		return device;
	}

	public void setDevice(I2CDevice device) {
		this.device = device;
	}

	public byte[] getDataAddresses() {
		return dataAddresses;
	}

	public void setDataAddresses(byte[] dataAddresses) {
		this.dataAddresses = dataAddresses;
	}

}
