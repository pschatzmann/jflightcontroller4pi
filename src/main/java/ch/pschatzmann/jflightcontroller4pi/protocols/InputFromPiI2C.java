package ch.pschatzmann.jflightcontroller4pi.protocols;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import ch.pschatzmann.jflightcontroller4pi.data.DataFactory;
import ch.pschatzmann.jflightcontroller4pi.data.IData;

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

public class InputFromPiI2C implements IPwmIn {
	private I2CDevice device;
	private int bus = I2CBus.BUS_0;
	private byte deviceAddess = 0x39;
	private byte controlAddress = (byte) 0x80;
	private byte powerUp = (byte) 0x03;
	private byte powerDown = (byte) 0x00;
	private byte dataAddresses[] = {(byte) 0x8C, (byte) 0x8E};


	public InputFromPiI2C(String pinName) {
		try {

			// get the I2C bus to communicate on
			I2CBus i2c = I2CFactory.getInstance(bus);

			// create an I2C device for an individual device on the bus that you want to
			// communicate with in this example we will use the default address for the TSL2561 chip which is
			// 0x39.
			I2CDevice device = i2c.getDevice(deviceAddess);
						

			// next we want to start taking light measurements, so we need to power up the
			// sensor
			device.write(controlAddress, powerUp);
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
			device.write(controlAddress, powerDown);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public I2CDevice getDevice() {
		return device;
	}

	public void setDevice(I2CDevice device) {
		this.device = device;
	}

	public int getBus() {
		return bus;
	}

	public void setBus(int bus) {
		this.bus = bus;
	}

	public byte getDeviceAddess() {
		return deviceAddess;
	}

	public void setDeviceAddess(byte deviceAddess) {
		this.deviceAddess = deviceAddess;
	}

	public byte getControlAddress() {
		return controlAddress;
	}

	public void setControlAddress(byte controlAddress) {
		this.controlAddress = controlAddress;
	}

	public byte getPowerUp() {
		return powerUp;
	}

	public void setPowerUp(byte powerUp) {
		this.powerUp = powerUp;
	}

	public byte getPowerDown() {
		return powerDown;
	}

	public void setPowerDown(byte powerDown) {
		this.powerDown = powerDown;
	}

	public byte[] getDataAddresses() {
		return dataAddresses;
	}

	public void setDataAddresses(byte[] dataAddresses) {
		this.dataAddresses = dataAddresses;
	}

}
