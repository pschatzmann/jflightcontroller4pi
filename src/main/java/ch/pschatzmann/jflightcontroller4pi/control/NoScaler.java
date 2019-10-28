package ch.pschatzmann.jflightcontroller4pi.control;

import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

/**
 * Provide the Input to the Output w/o scaling
 * @author pschatzmann
 *
 */
public class NoScaler implements IScaler {
	@Override
	public double scale(ParametersEnum par, double inputValue) {
		return inputValue;
	}
}
