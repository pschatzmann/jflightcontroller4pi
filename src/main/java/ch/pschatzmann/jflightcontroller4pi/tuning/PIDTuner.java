package ch.pschatzmann.jflightcontroller4pi.tuning;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Formatter;
import java.util.Optional;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.devices.IOutDeviceEx;
import ch.pschatzmann.jflightcontroller4pi.devices.OutDevice;
import ch.pschatzmann.jflightcontroller4pi.imu.IMUDevice;
import ch.pschatzmann.jflightcontroller4pi.integration.DatagramReader;
import ch.pschatzmann.jflightcontroller4pi.integration.GraphiteMetrics;
import ch.pschatzmann.jflightcontroller4pi.modes.PIDModeRule;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;
import ch.pschatzmann.jflightcontroller4pi.protocols.NullDevice;

/**
 * We try to find the best P, I and D values for the Aileron and Elevator
 * 
 * @author pschatzmann
 *
 */

public class PIDTuner {
	private static Logger log = LoggerFactory.getLogger(PIDTuner.class);
	private PIDError tuneError;
	private FlightController ctl;
	private FlightgearLauncher laucher;
	private HeartBeatSensor heartBeatSensor;

	/**
	 * The PIDTuner starts here...
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		PIDTuner t = new PIDTuner();
		t.setup();

		PIDResult best = t.evaluate();
		System.out.println("Best result:" + best);
		System.exit(0);
	}

	protected PIDResult evaluate() {

		// deactivate elevator
		IOutDeviceEx elevator = ctl.getControlDevice(ParametersEnum.ELEVATOR);
		PIDModeRule ruleE = (PIDModeRule) elevator.getRecalculate();
		ruleE.setP(0);
		ruleE.setI(0.0);
		ruleE.setD(0.0);
		ruleE.setup();
		
		// optimize the aileron
		IOutDeviceEx aileron = ctl.getControlDevice(ParametersEnum.AILERON);
		PIDModeRule ruleA = (PIDModeRule) aileron.getRecalculate();
		PIDResult result = evaluateDevice(ruleA, ParametersEnum.ROLL);

		// use optimized aileron for further processing
		ruleA.setP(result.p);
		ruleA.setI(result.i);
		ruleA.setD(result.d);

		// optimize elevator
		result = evaluateDevice(ruleE, ParametersEnum.PITCH);
		 
		return result;
	}
	

	protected PIDResult evaluateDevice(PIDModeRule rule, ParametersEnum sensor) {

		// optimize p
		Optional<PIDResult> result = this.getRange(0.1, 1.0, 10).map(doubleVal -> {
			rule.setP(doubleVal);
			return rule;
		}).map(r -> new PIDResult(this, sensor, r)).min(Comparator.comparing(e -> e.error));

		// optimize i
		rule.setP(result.get().p);
		result = this.getRange(0.0, 0.1, 10).map(doubleVal -> {
			rule.setI(doubleVal);
			return rule;
		}).map(r -> new PIDResult(this, sensor, r)).min(Comparator.comparing(e -> e.error));

		// optimize d
		rule.setP(result.get().p);
		rule.setI(result.get().i);
		result = this.getRange(0.0, 0.1, 10).map(doubleVal -> {
			rule.setD(doubleVal);
			return rule;
		}).map(r -> new PIDResult(this, sensor, r)).min(Comparator.comparing(e -> e.error));

		return result.get();

	}

	protected Stream<Double> getRange(double from, double to, int steps) {
		return DoubleStream.iterate(from, n -> n + (to - from) / steps).limit(steps).boxed();
	}

	protected void setup() {
		ApplicationContext context = new ClassPathXmlApplicationContext("config.xml");
		ctl = (FlightController) context.getBean("flightController");
		laucher = (FlightgearLauncher) context.getBean("flightgearLauncher");
		heartBeatSensor = new HeartBeatSensor();
		ctl.addDevices(Arrays.asList(heartBeatSensor));
		ctl.selectMode("stabilizedMode");
		tuneError = new PIDError(ctl);

		new Thread(() -> {
			ctl.run();
		}).start();

	}

	protected Double eval(PIDModeRule rule, ParametersEnum par) {
		log.info("eval");

		// set target
		ctl.setValue(ParametersEnum.PITCH, 0);
		ctl.setValue(ParametersEnum.ROLL, 0);
		ctl.setValue(ParametersEnum.THROTTLE, 0.8);

		double error = Double.MAX_VALUE;
		while (true) {
			try {
				error = tryEval(par);
				break;
			} catch (Exception ex) {
				log.error("Eval has failed: " + ex.getMessage() + " - we retry...");
			}
		}

		StringBuilder sbuf = new StringBuilder();
		Formatter fmt = new Formatter(sbuf);
		fmt.format("error: %f <=  %s", error, rule.toString());
		System.out.println(sbuf.toString());
		fmt.close();

		return error;
	}

	protected double tryEval(ParametersEnum par) throws Exception {
		log.info("tryEval");
		// restart flightgear
		laucher.start();

		// reset the error
		ctl.sleep(1000);
		tuneError.reset();

		// evaluate for 1 minute
		for (int j = 0; j < 600; j++) {
			if (!heartBeatSensor.isActive()) {
				throw new Exception("We did not get any telemetry data: Flightgear might have crashed");
			}
			ctl.sleep(100);
		}
		log.info("Setting {} to {}", par.name(), -10);
		ctl.setValue(par, 10);
		for (int j = 0; j < 600; j++) {
			if (!heartBeatSensor.isActive()) {
				throw new Exception("We did not get any telemetry data: Flightgear might have crashed");
			}
			ctl.sleep(100);
		}

		double error = tuneError.getError(par);
		return error;
	}

	static class PIDResult {
		double p;
		double i;
		double d;
		double error;

		PIDResult(PIDTuner pidTuner, ParametersEnum sensor, PIDModeRule rule) {
			rule.setup();
			error = pidTuner.eval(rule, sensor);
			p = rule.getP();
			i = rule.getI();
			d = rule.getD();
			log.info("PIDResult: ", this.toString());
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("p=");
			sb.append(p);
			sb.append(" i=");
			sb.append(i);
			sb.append(" d=");
			sb.append(d);
			sb.append(" -> ");
			sb.append(error);
			return sb.toString();
		}
	}

}
