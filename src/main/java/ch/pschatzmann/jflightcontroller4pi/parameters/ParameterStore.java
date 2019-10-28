package ch.pschatzmann.jflightcontroller4pi.parameters;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Functionality to store and retrieve the parameter values. The ParameterValues are
 * held in a fixed array which provides fast access and we make sure that after the
 * data structure has been initialized there the memory requirements are fixed.
 * 
 * @author pschatzmann
 *
 */

public class ParameterStore {
    private static final Logger log = LoggerFactory.getLogger(ParameterStore.class);
	public static ParameterValue NOVALUE = new ParameterValue();
	private ParameterValues[] store;

	public ParameterStore(){
		this(20);
	}
	
	public ParameterStore(int historySize) {
		store = new ParameterValues[ParametersEnum.values().length];
		ParametersEnum parameterArray[] = ParametersEnum.values();
		for (int j=0;j<parameterArray.length;j++) {
			store[j] = new ParameterValues(historySize);
		}
	}

	public void setValue(ParametersEnum parametersEnum, double value) {
		if (parametersEnum!=null) {
			log.debug(parametersEnum+"->"+value);
			ParameterValues values = store[parametersEnum.ordinal()];
			values.addValue(System.currentTimeMillis(),value);
		}
	}

	public ParameterValue getValue(ParametersEnum parametersEnum) {
		if (parametersEnum==null) return NOVALUE;
		ParameterValues values = store[parametersEnum.ordinal()];
		return values.getValue();

	}

	public ParameterValue getPriorValue(ParametersEnum parametersEnum) {
		if (parametersEnum==null) return NOVALUE;
		ParameterValues values = store[parametersEnum.ordinal()];
		return values.getPriorValue();
	}

	public ParameterValue[] getHistory(ParametersEnum parametersEnum) {
		ParameterValues values = store[parametersEnum.ordinal()];
		return values.getHistory();
	}

	public void register(ParametersEnum parametersEnum, Consumer<ParameterValue> lambda) {
		ParameterValues values = store[parametersEnum.ordinal()];
		values.register(lambda);
	}

	public double getAvg(ParametersEnum parametersEnum) {
		ParameterValues values = store[parametersEnum.ordinal()];
		return values.getAvg();
		
	}


}
