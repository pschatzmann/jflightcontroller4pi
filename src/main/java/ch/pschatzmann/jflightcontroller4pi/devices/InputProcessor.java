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
 * Default implementation of an imput processor where we can specify the
 * parameter names
 * 
 * @author pschatzmann
 *
 */
public class InputProcessor implements IInputProcessor {
	private static final Logger log = LoggerFactory.getLogger(InputProcessor.class);
	@SuppressWarnings("unchecked")
	private List<ParametersEnum> inputParameters = Collections.EMPTY_LIST;

	public InputProcessor(List<ParametersEnum> inputParameters) {
		this.inputParameters = inputParameters;
	}

	public InputProcessor() {
	}

	@Override
	public void processInput(FlightController flightController, IData input, char delimiter) {
		if (log.isDebugEnabled()) log.debug(input.toString());
		double values[] = input.splitDouble(delimiter);
		if (values.length == inputParameters.size()) {
			for (int j = 0; j < values.length; j++) {
				flightController.setValue(inputParameters.get(j), values[j]);
			}
		} else {
			log.warn("Input inconsitent: we exptected {} paramameters: '{}'", inputParameters.size(),input.toString());
		}
	}

//	public void processInput(FlightController flightController, String input, char delimiter) {
//		log.debug(input);
//		String values[] = input.split(String.valueOf(delimiter));
//		if (values.length == inputParameters.size()) {
//			for (int j = 0; j < values.length; j++) {
//				if (!values[j].isEmpty()) {
//					flightController.setValue(inputParameters.get(j), Double.parseDouble(values[j]));
//				}
//			}
//		} else {
//			log.warn("Input inconsitent: we exptected {} paramameters" ,inputParameters.size());
//		}
//	}

	public Collection<ParametersEnum> getInputParameters() {
		return inputParameters;
	}

	public void setInputParameters(Collection<ParametersEnum> inputParameters) {
		this.inputParameters = new ArrayList<ParametersEnum>(inputParameters);
	}


}
