package ch.pschatzmann.jflightcontroller4pi.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translates an input value from a defined input range to a output value (with a defined output range)
 * We support an asymmetric input range where the neutral inputNormal value is not in the middle.

 * @author pschatzmann
 *
 */
public class Scaler implements IScaler {
    private static final Logger log = LoggerFactory.getLogger(Scaler.class);
	double inputMin,  inputMax,  outputMin,  outputMax;
	
	public Scaler(){		
	}
	
	public Scaler(double inputMin, double inputMax, double outputMin, double outputMax) {
		this.inputMin = inputMin;
		this.inputMax = inputMax;
		this.outputMin = outputMin;
		this.outputMax = outputMax;
	}

//	public Scaler(double inputMin, double inputMax) {
//		this.inputMin = inputMin;
//		this.inputMax = inputMax;
//		this.outputMin = -1.0;
//		this.outputMax = 1.0;
//	}

    @Override
	public double scale(double inputValue) {
    	if (inputValue>inputMax) {
    		log.warn("Unexpected input value: {} - the value is larger then the max ({}) and will be adjusted", inputValue, inputMax);
    		inputValue = inputMax;
    	}
    	if (inputValue<inputMin) {
    		log.warn("Unexpected input value: {} - the value is smaller then the min ({}) and will be adjusted", inputValue, inputMin);
    		inputValue = inputMin;
    	}
    	
    	return (outputMax - outputMin) * (inputValue - inputMin) / (inputMax - inputMin) + outputMin;	
    }

	public double getInputMin() {
		return inputMin;
	}

	public void setInputMin(double inputMin) {
		this.inputMin = inputMin;
	}

	public double getInputMax() {
		return inputMax;
	}

	public void setInputMax(double inputMax) {
		this.inputMax = inputMax;
	}

	public double getOutputMin() {
		return outputMin;
	}

	public void setOutputMin(double outputMin) {
		this.outputMin = outputMin;
	}

	public double getOutputMax() {
		return outputMax;
	}

	public void setOutputMax(double outputMax) {
		this.outputMax = outputMax;
	}
}
