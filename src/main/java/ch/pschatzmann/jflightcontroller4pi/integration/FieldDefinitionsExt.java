package ch.pschatzmann.jflightcontroller4pi.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

/**
 * Field Defintion where we can define the Parameter with a Scaler
 * 
 * @author pschatzmann
 *
 */
public class FieldDefinitionsExt implements IFieldDefinitions {
	List<FieldDefinition> fieldDefinitions = new ArrayList<FieldDefinition>();

	@Override
	public List<FieldDefinition> getFieldDefinitions() {
		return this.fieldDefinitions;
	}

	public void setFieldDefinitions(List<FieldDefinition> def) {
		this.fieldDefinitions = def;
	}

	@Override
	public List<ParametersEnum> getFieldNames() {
		return this.fieldDefinitions.stream().map(i -> i.getInputField()).collect(Collectors.toList());
	}

}
