package ch.pschatzmann.jflightcontroller4pi.integration;

import ch.pschatzmann.jflightcontroller4pi.control.IScaler;
import ch.pschatzmann.jflightcontroller4pi.control.NoScaler;
import ch.pschatzmann.jflightcontroller4pi.control.Scaler;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

/**
 * Field Definition for input and output fields
 * 
 * @author pschatzmann
 *
 */
public class FieldDefinition {
	private static final IScaler NOSCALER = new NoScaler();
	private ParametersEnum inputField;
	private IScaler scaler = NOSCALER;

	public FieldDefinition() {
	}

	public FieldDefinition(ParametersEnum inputField, double inputFieldMin, double inputFieldMax) {
		this.inputField = inputField;
		this.scaler = new Scaler(inputFieldMin,inputFieldMax,-1,1);
	}

	public FieldDefinition(ParametersEnum inputField) {
		this.inputField = inputField;
		this.scaler = NOSCALER;
	}

	public ParametersEnum getInputField() {
		return inputField;
	}

	public void setInputField(ParametersEnum inputField) {
		this.inputField = inputField;
	}

	public IScaler getScaler() {
		return scaler;
	}

	public void setScaler(IScaler scaler) {
		this.scaler = scaler;
	}


}
