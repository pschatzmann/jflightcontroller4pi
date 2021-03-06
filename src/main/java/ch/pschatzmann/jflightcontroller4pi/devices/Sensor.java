package ch.pschatzmann.jflightcontroller4pi.devices;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.data.IData;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;
import ch.pschatzmann.jflightcontroller4pi.protocols.IPinIn;
import ch.pschatzmann.jflightcontroller4pi.protocols.NullDevice;

/**
 * Generic implementation for a sensor: It needs to be configured by defining the input processor and
 * the input hardware (pin) / protocol.
 * 
 * @author pschatzmann
 *
 */
public class Sensor implements ISensor {
    private static final Logger log = LoggerFactory.getLogger(Sensor.class);
	private String name;
	private IPinIn in = new NullDevice();
	private FlightController flightController;
	private IInputProcessor inputProcessor = new InputProcessor();
	private char delimiter = ',';
	private double frequency;

	public Sensor() {
	}

	public Sensor(List<ParametersEnum> inputParameters, IPinIn in) {	
		this.inputProcessor = new InputProcessor(inputParameters);
		this.in = in;
	}

	public Sensor(List<ParametersEnum> inputParameters, IPinIn in, char delimiter) {	
		this.inputProcessor = new InputProcessor(inputParameters);
		((InputProcessor)this.inputProcessor).setDelimiter(delimiter);
		this.in = in;
	}

	@Override
	public void setup(FlightController flightController) {
		this.flightController = flightController;
	}
	
	@Override
	public void processInput() {
		// get the input from the sensors
		IData input = in.getValues();
		// process the input
		inputProcessor.processInput(flightController, input);
		// release the memory
		if (input!=null) {
			input.close();
		}
	}

	public IPinIn getIn() {
		return in;
	}

	public void setIn(IPinIn in) {
		this.in = in;
	}

	public String getName() {
		if (name == null) {
			name = this.getClass().getSimpleName();
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public FlightController getFlightController() {
		return flightController;
	}

	public void setFlightController(FlightController flightController) {
		this.flightController = flightController;
	}

	public IInputProcessor getInputProcessor() {
		return inputProcessor;
	}

	public void setInputProcessor(IInputProcessor inputProcessor) {
		this.inputProcessor = inputProcessor;
	}

	@Override
	public void shutdown() {
		log.info("shutdown");
		in.shutdown();
	}
	
	public char getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(char delimiter) {
		this.delimiter = delimiter;
	}

	@Override
	public String toString() {
		return this.getName();
	}
	
	@Override
	public void setFrequency(double frequency) {
		this.frequency = frequency;
		
	}

	@Override
	public double getFrequency() {
		return this.frequency;
	}


}
