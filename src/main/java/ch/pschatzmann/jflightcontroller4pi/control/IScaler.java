package ch.pschatzmann.jflightcontroller4pi.control;

import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

/**
 * Convert Input to Output
 * @author pschatzmann
 *
 */
public interface IScaler {	
     double scale(ParametersEnum par, double inputValue);
}
