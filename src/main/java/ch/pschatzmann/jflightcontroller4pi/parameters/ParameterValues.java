package ch.pschatzmann.jflightcontroller4pi.parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Actual ParameterValue and the history for a single parameter. We can register lambda
 * functions which need to execute when a value is added. The data is stored in
 * a Ring Buffer which is implemented on top of an array.
 * 
 * The adding and retrieving of the last value is very efficient. After the
 * data structure has been initialized, we do not create any new Java Objects in 
 * order to minimize the need for Garbage Collection.
 * 
 * @author pschatzmann
 *
 */

public class ParameterValues {
	private ParameterValue history[];
	private int actualPos = -1;
	private int actualSize;
	private int maxHistorySize;
	private List<Consumer<ParameterValue>> lambdas = new ArrayList<Consumer<ParameterValue>>();

	public ParameterValues(int maxHistorySize) {
		this.maxHistorySize = maxHistorySize;
		history = new ParameterValue[maxHistorySize];
		for (int j = 0; j < maxHistorySize; j++) {
			history[j] = new ParameterValue();
		}
	}

	/**
	 * Gets the last (most recent) recorded value
	 * 
	 * @return
	 */
	synchronized public ParameterValue getValue() {
		return history[actualPos<0?0:actualPos];
	}

	/**
	 * Gets the second last recorded value
	 * @return
	 */
	synchronized public ParameterValue getPriorValue() {
		int pos = actualPos-1;
		if (pos==-1) {
			pos = maxHistorySize-1;
		}
		return history[pos];
	}

	
	/**
	 * record a new value
	 * 
	 * @param timeStamp
	 * @param value
	 */
	synchronized public void addValue(long timeStamp, double value) {
		actualPos++;
		if (actualPos >= maxHistorySize) {
			actualPos = 0;
		}
		if (actualSize < maxHistorySize) {
			actualSize++;
		}
		// update value
		ParameterValue pv = history[actualPos];
		pv.timestamp = timeStamp;
		pv.value = value;

		lambdas.forEach(lambda -> {
			lambda.accept(pv);
		});

	}

	/**
	 * Register a function which will be executed when a value is added
	 * 
	 * @param lambda
	 */
	public void register(Consumer<ParameterValue> lambda) {
		lambdas.add(lambda);
	}

	/**
	 * Determines the History
	 * 
	 * @return
	 */
	synchronized public ParameterValue[] getHistory() {
		ParameterValue[] result = new ParameterValue[actualSize];
		int pos = actualPos;
		for (int j = actualSize-1; j >= 0; j--) {
			result[j] = history[pos];
			pos--;
			if (pos < 0) {
				pos = pos + actualSize;
			}
		}
		return result;
	}

	/**
	 * Provides the number of available recorded values
	 * @return
	 */
	public int size() {
		return this.actualSize;
	}

	/**
	 * Checks if we have any values recorded
	 * @return
	 */
	public boolean isEmpty() {
		return this.actualSize == 0;
	}
	
	/**
	 * Calculates the average
	 * @return
	 */
	public double getAvg() {
		ParameterValue[] history = this.getHistory();
		double total = Arrays.asList(history).stream().mapToDouble(r -> r.value).sum();
		return total / history.length;
	}

}
