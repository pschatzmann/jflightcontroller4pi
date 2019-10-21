package ch.pschatzmann.jflightcontroller4pi.integration;

import java.util.List;
import java.util.stream.Collectors;

import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

/**
 * Field definitions from a list of ParametersEnum w/o support for scaling
 * 
 * @author pschatzmann
 *
 */
public class FieldDefinitions implements IFieldDefinitions {
	private List<ParametersEnum> fields;

	public FieldDefinitions(){	
	}
	
	public FieldDefinitions(List<ParametersEnum> l) {
		this.setFieldNames(l);
	}
	
	@Override
	public List<FieldDefinition> getFieldDefinitions() {
		List<FieldDefinition> result = fields.stream().map(f -> new FieldDefinition(f)).collect(Collectors.toList());
		return result;
	}

	@Override
	public List<ParametersEnum> getFieldNames() {
		return fields;
	}

	public void setFieldNames(List<ParametersEnum> fields) {
		this.fields = fields;
	}
}
