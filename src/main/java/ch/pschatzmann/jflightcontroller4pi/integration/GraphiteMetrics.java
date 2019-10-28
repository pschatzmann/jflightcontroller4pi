package ch.pschatzmann.jflightcontroller4pi.integration;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.devices.IOutDevice;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParameterValue;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

/**
 * Report all parameter values to Graphite
 * 
 * @author pschatzmann
 *
 */
public class GraphiteMetrics implements IOutDevice {
	private FlightController flightController;
	private List<JFlightCarbonMetric> metrics = new ArrayList();;
	private List<JFlightCarbonMetric> updatedMetrics = new ArrayList();;
	private String host = "localhost";
	private int port = 2003;
	private Socket socket;
	private PrintWriter out;

	@Override
	public void setup(FlightController flightController) {
		try {
			this.flightController = flightController;

			socket = new Socket(host, port);
			out = new PrintWriter(socket.getOutputStream(), true);

			int len = ParametersEnum.values().length;
			for (int j = 0; j < len; j++) {
				metrics.add(j, new JFlightCarbonMetric("jflightcontroller." + ParametersEnum.values()[j].name()));
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void shutdown() {
		out.flush();
		out.close();
		try {
			socket.close();
		} catch (IOException e) {
		}
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public void processOutput() {
		for (ParametersEnum v : ParametersEnum.values()) {
			ParameterValue pv = flightController.getValue(v);
			if (pv.timestamp != pv.timestamp / 1000) {
				int pos = v.ordinal();
				JFlightCarbonMetric m = metrics.get(pos);
				m.setDoubleValue(pv.value);
				m.setTimestamp(pv.timestamp);
				out.write(m.toString());
				out.flush();
			}
		}
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	class JFlightCarbonMetric {
		long timestamp;
		String value;
		String name;

		JFlightCarbonMetric(String name) {
			this.name = name;
		}

		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;

		}

		public String getMetricName() {
			return name;
		}

		public String getValue() {
			return this.value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public void setDoubleValue(double doubleValue) {
			this.value = String.valueOf(doubleValue);
		}

		public String toString() {
			StringBuffer sb = new StringBuffer(name);
			sb.append(" ");
			sb.append(this.getValue());
			sb.append(" ");
			sb.append(timestamp / 1000);
			sb.append(System.lineSeparator());
			return sb.toString();
		}

	}

}
