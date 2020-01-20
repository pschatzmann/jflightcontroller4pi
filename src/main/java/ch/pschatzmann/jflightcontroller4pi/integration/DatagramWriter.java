package ch.pschatzmann.jflightcontroller4pi.integration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.data.DataFactory;
import ch.pschatzmann.jflightcontroller4pi.data.IData;
import ch.pschatzmann.jflightcontroller4pi.devices.IOutDevice;

/**
 * Write values from the ParameterStore to an UDP Port (e.g. for JSBSim). This functionality
 * is driven by the output processing which is defined in the control loop.
 * 
 * @author pschatzmann
 *
 */
public class DatagramWriter implements IOutDevice {
    private static final Logger log = LoggerFactory.getLogger(DatagramWriter.class);
	private InetSocketAddress ip;
	private DatagramChannel channel;
	private byte[] buffer = new byte[2048];
	private ByteBuffer buf = ByteBuffer.wrap(buffer);
	private IFieldDefinitions outputFields = new FieldDefinitions();
	private FlightController flightController;
	private long processedRecords = 0;
	private String delimiter = ",";
	private String destinationHost = "localhost";
	private int port = 5001;
	private boolean active = true;

	/**
	 * Default Constructor used by Spring
	 */
	public DatagramWriter(){
	}
	
	/**
	 * Constructor which provides all dependent objects
	 * @param outputFields
	 * @param port
	 */
	public DatagramWriter(IFieldDefinitions outputFields, int port) {
		this.outputFields = outputFields;
		this.port = port;
	}
	
	public DatagramWriter(IFieldDefinitions outputFields, DatagramChannel dsocket, int port){
		this.outputFields = outputFields;
		this.channel = dsocket;
		this.port = port;
	}
	
	/**
	 * Setup Reader and read the values in a separate Thread
	 * 
	 **/
	@Override
	public void setup(FlightController flightController) {
		log.info("setup");;
		this.flightController = flightController;
		try {
			if (ip==null) {
		        ip = new InetSocketAddress(destinationHost,port); 				
			}
			if (channel==null) {
				this.channel = DatagramChannel.open();
				this.channel.configureBlocking(false);
			}
			
			log.info("The datagram writer has been started on port {}", this.port);

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public FlightController getFlightController() {
		return flightController;
	}

	public void setFlightController(FlightController flightController) {
		this.flightController = flightController;
	}

	/**
	 * Determines the data from the Store and sends it out via a socket
	 */
	@Override
	public void processOutput() {
		try {
			if (active) {
				IData outLine = getOutputLine();
				if (!outLine.isEmpty()) {
					buf.clear();
					buf.put(outLine.getBytes());
					buf.flip();
	
					if (channel.send(buf, ip)>0) {
						if (log.isDebugEnabled()) log.debug("processing output: {}",outLine.toString());					
					}
	
					processedRecords++;
				}
				outLine.close();
			}
		} catch (Exception ex) {
			log.error(ex.getLocalizedMessage(), ex);
		}
	}

	protected IData getOutputLine() {
		IData result = DataFactory.instance();

		List<FieldDefinition> fields = outputFields.getFieldDefinitions();
		FieldDefinition last = fields.get(fields.size()-1);
		// push the data to the ParametersStore
		for (FieldDefinition def : fields) {
			double value = flightController.getValue(def.getInputField()).value;
			double scaledValue = def.getScaler().scale(def.getInputField(), value);
			result.append(scaledValue);
			if (def!=last) {
				result.append(delimiter);
			}
		}
		return result;
	}

	/**
	 * Determines the fields which need to be processed
	 * @return
	 */
	public IFieldDefinitions getFieldDefinitions() {
		return outputFields;
	}

	/**
	 * Defines the fields which need to be processed
	 */
	public void setFieldDefinitions(IFieldDefinitions inputFields) {
		this.outputFields = inputFields;
	}

	/**
	 * Determines the UDP port
	 * @return
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Defines the UDP port
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}
	
	public String getHost() {
		return destinationHost;
	}

	public void setHost(String destinationHost) {
		this.destinationHost = destinationHost;
	}

	/**
	 * Provides the number of sent records/lines
	 * @return
	 */
	public long getRecordCount() {
		return processedRecords;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	@Override
	public void shutdown() {
		log.info("shutdown");
		try {
			if (channel!=null) {
				channel.close();
			}
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
		this.channel = null;
	}
	
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}


}
