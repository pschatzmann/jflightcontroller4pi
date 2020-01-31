package ch.pschatzmann.jflightcontroller4pi.parameters;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.guidence.navigation.coordinates.Coordinate2D;
import ch.pschatzmann.jflightcontroller4pi.guidence.navigation.coordinates.Coordinate3D;

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
	
	/**
	 * Converts the parameters to a map
	 * @return
	 */
	public Map<String,Double> toMap() {
		Map<String,Double> result = new TreeMap();
		for (ParametersEnum v : ParametersEnum.values()) {
			ParameterValue pv = this.getValue(v);
			if (pv!=null) {
				result.put(v.name(), pv.value);
			}
		}
		return result;
	}
	
	/**
	 * Returns the history as list of map
	 * @param parametersEnum
	 * @return
	 */
	public List<Map> toHistoryMap(ParametersEnum parametersEnum) {
		List result = new ArrayList();
		for (ParameterValue pv : getHistory(parametersEnum)) {
			Map rec = new HashMap();
			rec.put("TIME", pv.timestamp);
			rec.put(parametersEnum.name(), pv.value);
			result.add(rec);
		}		
		return result;
	}
	
	/**
	 * Get 3D GPS Coordinates from Parameter store
	 */
	
	public Coordinate3D getCoordinate3DGPS() {
		return new Coordinate3D(this.doubleValue(ParametersEnum.GPSLATITUDE),this.doubleValue(ParametersEnum.GPSLONGITUDE),this.doubleValue(ParametersEnum.GPSALTITUDE));
	}
	/**
	 * Get 3D IMU Coordinates from Parameter store
	 * @return
	 */
	public Coordinate3D getCoordinate3DIMU() {
		return new Coordinate3D(this.doubleValue(ParametersEnum.IMULATITUDE),this.doubleValue(ParametersEnum.IMULONGITUDE),this.doubleValue(ParametersEnum.ALTITUDE));
	}

	/**
	 * Get 2D IMU Coordinates from Parameter store
	 * @return
	 */
	public Coordinate2D getCoordinate2DIMU() {
		return new Coordinate2D(this.doubleValue(ParametersEnum.IMULATITUDE),this.doubleValue(ParametersEnum.IMULONGITUDE),this.doubleValue(ParametersEnum.ALTITUDE));
	}

	
	private double doubleValue(ParametersEnum par) {
		ParameterValue pv = this.getValue(par);
		return pv == null ? Double.NaN : pv.value;
	}

	public String toString() {
		return this.getClass().getSimpleName();
	}


}
