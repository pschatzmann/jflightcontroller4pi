package ch.pschatzmann.jflightcontroller4pi.protocols;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;

import ch.pschatzmann.jflightcontroller4pi.data.DataOfString;
import ch.pschatzmann.jflightcontroller4pi.data.IData;

public class InputSerial implements IPinIn {
	private ConcurrentLinkedQueue<String> data = new ConcurrentLinkedQueue();
	private  com.pi4j.io.serial.Serial serial;
	
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
                    data.add(event.getAsciiString());
                } catch (IOException e) {
                    e.printStackTrace();
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
