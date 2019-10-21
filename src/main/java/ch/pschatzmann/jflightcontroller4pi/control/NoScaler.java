package ch.pschatzmann.jflightcontroller4pi.control;

/**
 * Provide the Input to the Output w/o scaling
 * @author pschatzmann
 *
 */
public class NoScaler implements IScaler {
	@Override
	public double scale(double inputValue) {
		return inputValue;
	}
}
