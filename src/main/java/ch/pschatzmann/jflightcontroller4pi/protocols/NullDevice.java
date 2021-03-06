package ch.pschatzmann.jflightcontroller4pi.protocols;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.control.IScaler;
import ch.pschatzmann.jflightcontroller4pi.control.NoScaler;
import ch.pschatzmann.jflightcontroller4pi.data.DataFactory;
import ch.pschatzmann.jflightcontroller4pi.data.IData;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

/**
 * Dummy input device which generates 0 as values. This can be used e.g. in unit tests.
 * 
 * @author pschatzmann
 *
 */
public class NullDevice implements IPinIn, IPinOut {
    private static final Logger log = LoggerFactory.getLogger(NullDevice.class);
	private IData value;
	private double doubleValue;
	private IScaler scaler = new NoScaler();
	
	@Override
	public IData getValues() {
		return value;
	}

	@Override
	public void shutdown() {
		value = null;
	}

	@Override
	public void setValue(double value) {
		this.doubleValue = scaler.scale(ParametersEnum.PIN,value);
		this.value = DataFactory.instance();
		this.value.append(String.valueOf(doubleValue));
		log.debug("-> "+this.value);
	}
	
	@Override
	public double getValue() {
		return doubleValue;
	}
	
	boolean isEmpty() {
		return value == null;
	}

	public IScaler getScaler() {
		return scaler;
	}

	public void setScaler(IScaler scaler) {
		this.scaler = scaler;
	}

}
