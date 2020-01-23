package ch.pschatzmann.jflightcontroller4pi.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Filter which provides the Avg value
 * 
 * @author pschatzmann
 *
 */
public class AvgFilter implements IFilter {
	private int size=0;
	private List<Double> values = new ArrayList();
	
	public AvgFilter(int size){
		this.size = size;
	}
	
	@Override
	public void add(double value) {
		if (Double.isFinite(value)) {
			values.add(value);
			if (values.size()>size) {
				values.remove(0);
			}
		}
	}
	
	@Override
	public double getValue() {
		OptionalDouble o =values.stream().mapToDouble(d -> d).average();
		return o.orElse(0.0);
	}

}
