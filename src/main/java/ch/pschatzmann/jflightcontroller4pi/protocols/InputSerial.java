package ch.pschatzmann.jflightcontroller4pi.protocols;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;

import ch.pschatzmann.jflightcontroller4pi.data.DataOfString;
import ch.pschatzmann.jflightcontroller4pi.data.IData;
import ch.pschatzmann.jflightcontroller4pi.navigation.GPS;

/**
 * IPinIn implementation which handles a serial input.
 * 
 * @author pschatzmann
 *
 */

public class InputSerial implements IPinIn {
	private static Logger log = LoggerFactory.getLogger(InputSerial.class);
	private ConcurrentLinkedQueue<String> data = new ConcurrentLinkedQueue();
	private Serial serial;
	
	public InputSerial(SerialConfig config) throws IOException{
         serial = SerialFactory.createInstance();
         
         serial.open(config);

        // create and register the serial data listener
        serial.addListener(new SerialDataEventListener() {
            @Override
            public void dataReceived(SerialDataEvent event) {

                // NOTE! - It is extremely important to read the data received from the
                // serial port.  If it does not get read from the receive buffer, the
                // buffer will continue to grow and consume memory.
            	
                try {
                	String str = event.getAsciiString();
                	if (str!=null && !str.isEmpty()) {
                		data.add(str);
                	}
                } catch (Exception e) {
                    log.error("Could not receive serial data", e);
                }
            }
        });
	}

	@Override
	public IData getValues() {
		IData result=null; 
		String str = data.poll();
		if (str!=null) {
			result = new DataOfString();
			((DataOfString)result).setString(str);
		}
		return result;
	}

	@Override
	public void shutdown() {
		if (!SerialFactory.isShutdown())
			SerialFactory.shutdown();
	}

}
