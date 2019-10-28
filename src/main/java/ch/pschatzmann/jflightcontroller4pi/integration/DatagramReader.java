package ch.pschatzmann.jflightcontroller4pi.integration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.data.DataFactory;
import ch.pschatzmann.jflightcontroller4pi.data.IData;
import ch.pschatzmann.jflightcontroller4pi.devices.IInputProcessor;
import ch.pschatzmann.jflightcontroller4pi.devices.ISensor;
import ch.pschatzmann.jflightcontroller4pi.devices.InputProcessor;

/**
 * Read values from a UDP Port (e.g. from JSBSim) into the ParameterStore
 * 
 * @author pschatzmann
 *
 */
public class DatagramReader implements ISensor {
	private static final Logger log = LoggerFactory.getLogger(DatagramReader.class);
	private IFieldDefinitions inputFields = new FieldDefinitions();
	private FlightController flightController;
	private IInputProcessor inputProcessor;
	private char delimiter = ',';
	private DatagramChannel channel;
	private ByteBuffer buffer = ByteBuffer.allocate(1023);
	private int port = 5000;
	private boolean active = true;
	private long processedRecords = 0;;

	/**
	 * Constructor used by Spring
	 */
	public DatagramReader() {
	}

	/**
	 * Default Constructor
	 * 
	 * @param flightController
	 * @param inputFields
	 * @param port
	 */
	public DatagramReader(IFieldDefinitions inputFields, int port) {
		this.inputFields = inputFields;
		this.port = port;
	}

	public DatagramReader(IFieldDefinitions inputFields, DatagramChannel channel,
			int port) {
		this.inputFields = inputFields;
		this.channel = channel;
		this.port = port;
	}

	/**
	 * Setup Reader and read the values in a separate Thread *
	 */
	@Override
	public void setup(FlightController flightController) {
		this.flightController = flightController;
		try {
			if (this.inputProcessor == null) {
				this.inputProcessor = new InputProcessor(inputFields.getFieldNames());
			}

			if (this.channel == null) {
				channel = DatagramChannel.open();
				channel.socket().bind(new InetSocketAddress(port));
				channel.configureBlocking(false);
				channel.socket().setReceiveBufferSize(1024);

			} else {
				log.info("DatagramReader: the socket has already been set up");
			}
			// Create a packet to receive data into the buffer
			log.info("The datagram reader has been started on port {}", this.port);
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

	@Override
	public synchronized void processInput() {
		IData str = null;
		try {
			if (active && channel!=null) {
				SocketAddress sa = channel.receive(buffer);
				if (sa != null) {
					log.debug("processInput");
					buffer.flip();
	
					str = DataFactory.instance();
					str.setBytes(buffer.array());
					if (!str.isEmpty()) {
						processInputLine(str);
					}
	
					// release memory
					str.close();
				}
				processedRecords++;

			}

		} catch (java.nio.channels.AsynchronousCloseException e) {
			// dont try to use it any more
			this.channel = null;
		} catch (Exception e) {
			if (str!=null) {
				log.error(e.getMessage()+": '{}'", str.toString(), e);
			}
		}

	}

	protected void processInputLine(IData line) {
		try {
			inputProcessor.processInput(flightController, line, delimiter);
		} catch(Exception ex) {
			log.error("Could not process the following line: '{}'", line.toString(), ex);
		}
	}

	public IFieldDefinitions getFieldDefinitions() {
		return inputFields;
	}

	public void setFieldDefinitions(IFieldDefinitions inputFields) {
		this.inputFields = inputFields;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public IInputProcessor getInputProcessor() {
		return inputProcessor;
	}

	public void setInputProcessor(IInputProcessor inputProcessor) {
		this.inputProcessor = inputProcessor;
	}

	/**
	 * Provides the number of sent records/lines
	 * 
	 * @return
	 */
	public long getRecordCount() {
		return processedRecords;
	}

	public char getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(char delimiter) {
		this.delimiter = delimiter;
	}

	@Override
	public void shutdown() {
		log.info("shutdown");
		DatagramChannel c = this.channel;
		this.channel = null;
		
		if (c != null) {
			try {
				c.close();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
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
