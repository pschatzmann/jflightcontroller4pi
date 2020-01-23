package ch.pschatzmann.jflightcontroller4pi.control;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.devices.GameConsole;

/**
 * Removes outliers which are deviating more then n * stdDev
 * 
 * @author pschatzmann
 *
 */
public class OutlierFilter implements IFilter {
	private static final Logger log = LoggerFactory.getLogger(OutlierFilter.class);
	private List<Double> values = new ArrayList();
	private int nstdDev;
	private int size;

	public OutlierFilter(int size, int nStdDev) {
		this.size = size;
		this.nstdDev = nStdDev;
	}

	@Override
	public void add(double value) {
		if (values.size()>=size-1) {
			OptionalDouble avg = values.stream().mapToDouble(d -> d).average();
			double stdDev = stddev(values);
			if (Math.abs(value - avg.getAsDouble()) <= stdDev * nstdDev) {
				values.add(0,value);
			} else {
				if (Double.isFinite(value)) {
					values.add(value);			
				}
				log.info("Outliner ignored {} with avg {} and stddev {}", value, avg.getAsDouble(),stdDev);
			}
		} else {
			if (Double.isFinite(value)) {
				values.add(0, value);
			}
		}

		if (values.size() == size) {
			values.remove(size - 1);
		}

	}

	public static double stddev(List<Double> nums) {
		double sum = 0.0, standardDeviation = 0.0;
		int length = nums.size();
		for (double num : nums) {
			sum += num;
		}
		double mean = sum / length;
		for (double num : nums) {
			standardDeviation += Math.pow(num - mean, 2);
		}
		return Math.sqrt(standardDeviation / length);
	}

	@Override
	public double getValue() {
		return values.get(0);
	}

}
