package ch.pschatzmann.jflightcontroller4pi.protocols;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.StopBits;

import ch.pschatzmann.jflightcontroller4pi.data.DataOfString;
import ch.pschatzmann.jflightcontroller4pi.data.IData;
import ch.pschatzmann.jflightcontroller4pi.guidence.navigation.GPS;
import ch.pschatzmann.jflightcontroller4pi.integration.LineSplitter;

/**
 * IPinIn implementation which handles a serial input. We do not reley on the internal 
 * serial buffer but handle the data ourselfs
 * 
 * @author pschatzmann
 *
 */

public class InputSerialWithEvents implements IPinIn {
	private static Logger log = LoggerFactory.getLogger(InputSerialWithEvents.class);
	private ConcurrentLinkedQueue<String> data = new ConcurrentLinkedQueue();
	private Serial serial;
	private SerialConfig serialConfig;
	private boolean isSetup = false;
	private int maxSize = 200;
	private MySerialDataEventListener listener = new MySerialDataEventListener();

	public InputSerialWithEvents() {
		try {
			log.info("InputSerial");
			serialConfig = new SerialConfig();
		} catch(Throwable th) {
			log.error("No Servical interface",th);
		}
	}

	public InputSerialWithEvents(SerialConfig config) throws IOException {
		this.serialConfig = config;
	}

	public void setup() {
		try {
			log.info("setup");
			if (serialConfig==null) {
				serialConfig = new SerialConfig();				
			}
			if (serial == null) {
				serial = SerialFactory.createInstance();
			}
			serial.open(serialConfig);
			serial.setBufferingDataReceived(false);
			// create and register the serial data listener
			
			// register event listener
			serial.addListener(listener);
			isSetup = true;
		} catch(Exception ex) {
			log.error("Could not setup InputSerial",ex);
		}
	}
	
	public boolean isSetup() {
		return this.isSetup;
	}
	
	public Serial getSerial() {
		return this.serial;
	}

	@Override
	public IData getValues() {
		if (!isSetup)
			this.setup();

		IData result = null;
		String str = data.poll();
		if (str != null) {
			result = new DataOfString();
			((DataOfString) result).setString(str);
		}
		return result;
	}

	@Override
	public void shutdown() {
		log.info("shutdown");
		serial.removeListener(this.listener);

		try {
			serial.close();
		} catch (Exception e) {
			log.error("Could not close serial",e);;
		}
		this.isSetup = false;
	}

	/**
	 * @return the serialConfig
	 */
	public SerialConfig getSerialConfig() {
		return serialConfig;
	}

	/**
	 * @param serialConfig the serialConfig to set
	 */
	public void setSerialConfig(SerialConfig serialConfig) {
		this.serialConfig = serialConfig;
	}

	/**
	 * @return the maxSize
	 */
	public int getMaxSize() {
		return maxSize;
	}

	/**
	 * @param maxSize the maxSize to set
	 */
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	/**
	 * @return the baud
	 */
	public int getBaud() {
		return serialConfig.baud().getValue();
	}

	/**
	 * @param baud the baud to set
	 */
	public void setBaud(int rate) {
		this.serialConfig.baud(Baud.getInstance(rate));
	}

	/**
	 * @return the device
	 */
	public String getDevice() {
		return serialConfig.device();
	}

	/**
	 * @param device the device to set
	 */
	public void setDevice(String device) {
		this.serialConfig.device(device);
	}
	
	/**
	 * Reads the data. Separate class so that we can easily add and remove it
	 * from the event listner.
	 * @author pschatzmann
	 *
	 */
	public class MySerialDataEventListener implements SerialDataEventListener {
		LineSplitter lineSplitter = new LineSplitter();
		@Override
		public void dataReceived(SerialDataEvent event) {
			try {
				String line = event.getAsciiString();				
				if (!StringUtils.isEmpty(line)) {
					for (String out : lineSplitter.split(line)) {
						data.add(out);		
						log.debug("-> {}",out);
						if (data.size()>maxSize) {
							data.remove();
						}
					}
				}
			} catch (Exception e) {
				log.error("Could not receive serial data", e);
			}
		}
	} 

}
