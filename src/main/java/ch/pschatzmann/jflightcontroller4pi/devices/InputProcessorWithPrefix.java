package ch.pschatzmann.jflightcontroller4pi.devices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.data.IData;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

/**
 * input processor with the syntax [T|R|A|E]
 * 
 * @author pschatzmann
 *
 */
public class InputProcessorWithPrefix implements IInputProcessor {
	private static final Logger log = LoggerFactory.getLogger(InputProcessorWithPrefix.class);
	@SuppressWarnings("unchecked")
	private List<ParametersEnum> inputParameters = Collections.EMPTY_LIST;

	public InputProcessorWithPrefix(List<ParametersEnum> inputParameters) {
		this.inputParameters = inputParameters;
	}

	public InputProcessorWithPrefix() {
	}

	@Override
	public void processInput(FlightController flightController, IData input) {
		if (log.isDebugEnabled()) 
			log.debug(input.toString());
		String values[] = input.toString().split(":");
		double value = Double.valueOf(values[1]);
		if (values[0].equalsIgnoreCase("T")) {
			flightController.setValue(ParametersEnum.THROTTLE, value);
		} else if (values[0].equalsIgnoreCase("R")) {
			flightController.setValue(ParametersEnum.RUDDER, value);			
		} else if (values[0].equalsIgnoreCase("A")) {
			flightController.setValue(ParametersEnum.AILERON, value);
		} else if (values[0].equalsIgnoreCase("E")) {
			flightController.setValue(ParametersEnum.ELEVATOR, value);
		} else {
			log.debug("Input inconsitent: we got '{}'", input.toString());
		}
	}

	public Collection<ParametersEnum> getInputParameters() {
		return inputParameters;
	}

	public void setInputParameters(Collection<ParametersEnum> inputParameters) {
		this.inputParameters = new ArrayList<ParametersEnum>(inputParameters);
	}

	@Override
	public boolean isValid(IData input) {
		String firstChar = input.toString().substring(0, 1);
		boolean result = firstChar.matches("T|R|A|E");
		//if (log.isDebugEnabled()) {
			log.info("{} -> {}", input.toString(),result);
		//}
		return result;
	}


}
