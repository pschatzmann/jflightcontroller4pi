package ch.pschatzmann.jflightcontroller4pi.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Filter which provides the Median values
 * 
 * @author pschatzmann
 *
 */
public class MedianFilter implements IFilter {
	private int size=0;
	private List<Double> values = new ArrayList();
	
	public MedianFilter(int size){
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
		int pos = values.size()/2;
		Object sorted[] = values.toArray();
		Arrays.sort(sorted);
		return sorted.length>0 ? (double) sorted[pos] : 0.0;
	}

}
