package ch.pschatzmann.jflightcontroller4pi.tests;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.devices.IRecalculate;
import ch.pschatzmann.jflightcontroller4pi.devices.OutDevice;
import ch.pschatzmann.jflightcontroller4pi.loop.ControlLoop;
import ch.pschatzmann.jflightcontroller4pi.modes.FlightMode;
import ch.pschatzmann.jflightcontroller4pi.modes.ManualModeRule;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;
import ch.pschatzmann.jflightcontroller4pi.protocols.NullDevice;

/**
 * Checks the basic functionality of the FlightController. After updating a value we need to give
 * the event loop some time to process the new output
 * 
 * @author pschatzmann
 *
 */

public class TestFlightControllerManual {
	private static Logger log = LoggerFactory.getLogger(TestFlightControllerManual.class);
	private FlightController ctl;
	private OutDevice rudder;
	private OutDevice elevator;
	private OutDevice aileron;
	private OutDevice throttle;
	int sleep = 200;

	@Before
	public void setup() {
		ctl = new FlightController();
		ctl.setControlLoop(new ControlLoop());
		
		rudder = new OutDevice(ParametersEnum.RUDDER,  0.0, new NullDevice());
		elevator = new OutDevice( ParametersEnum.ELEVATOR,  0.0, new NullDevice());
		aileron = new OutDevice( ParametersEnum.AILERON,  0.0, new NullDevice());
		throttle = new OutDevice( ParametersEnum.THROTTLE, new NullDevice());
		throttle.setMinValue(0.0);
		throttle.setDefaultValue(0.0);
		
		List<IRecalculate> rules = new ArrayList<IRecalculate>();
		rules.add(new ManualModeRule(ctl, rudder,ParametersEnum.YAW));
		rules.add(new ManualModeRule(ctl, elevator,ParametersEnum.PITCH));
		rules.add(new ManualModeRule(ctl, aileron,ParametersEnum.ROLL ));
		rules.add(new ManualModeRule(ctl, throttle,ParametersEnum.SPEED));

		ctl.setMode(new FlightMode(rules));

		new Thread(() -> {
			ctl.run();
		}).start();

		ctl.sleep(1000);
	}

	
	@Test
	public void testBasicFlowAileron() {

		// check initial value
		Assert.assertEquals(0.0, ctl.getValue(ParametersEnum.AILERON).value, 0.001);
		Assert.assertEquals(50.0, ((NullDevice) (aileron.getPin())).getValue(), 0.001);

		// Update Aileron values
		ctl.setValue(ParametersEnum.ROLL, -1.0);
		ctl.sleep(sleep);
		Assert.assertEquals(-1.0, ctl.getValue(ParametersEnum.ROLL).value, 0.001);
		Assert.assertEquals(-1.0, ctl.getValue(ParametersEnum.AILERON).value, 0.001);
		Assert.assertEquals(0.0, ((NullDevice) (aileron.getPin())).getValue(), 0.001);

		ctl.setValue(ParametersEnum.ROLL, 1.0);
		ctl.sleep(sleep);
		Assert.assertEquals(1.0, ctl.getValue(ParametersEnum.ROLL).value, 0.001);
		Assert.assertEquals(1.0, ctl.getValue(ParametersEnum.AILERON).value, 0.001);
		Assert.assertEquals(100.0, ((NullDevice) (aileron.getPin())).getValue(), 0.001);

		ctl.setValue(ParametersEnum.ROLL, 0.0);
		ctl.sleep(sleep);
		Assert.assertEquals(0.0, ctl.getValue(ParametersEnum.ROLL).value, 0.001);
		Assert.assertEquals(0.0, ctl.getValue(ParametersEnum.AILERON).value, 0.001);
		Assert.assertEquals(50, ((NullDevice) (aileron.getPin())).getValue(), 0.001);

		ctl.shutdown();

	}

	@Test
	public void testBasicFlowThrottle() {

		// check initial value
		Assert.assertEquals(0.0, ctl.getValue(ParametersEnum.THROTTLE).value, 0.001);
		Assert.assertEquals(0.0, ((NullDevice) (throttle.getPin())).getValue(), 0.001);

		ctl.setValue(ParametersEnum.SPEED, 1.0);
		ctl.sleep(sleep);
		Assert.assertEquals(1.0, ctl.getValue(ParametersEnum.SPEED).value, 0.001);
		Assert.assertEquals(1.0, ctl.getValue(ParametersEnum.THROTTLE).value, 0.001);
		Assert.assertEquals(100, ((NullDevice) (throttle.getPin())).getValue(), 0.001);

		ctl.setValue(ParametersEnum.SPEED, 0.0);
		ctl.sleep(sleep);
		Assert.assertEquals(0.0, ctl.getValue(ParametersEnum.SPEED).value, 0.001);
		Assert.assertEquals(0.0, ctl.getValue(ParametersEnum.THROTTLE).value, 0.001);
		Assert.assertEquals(0, ((NullDevice) (throttle.getPin())).getValue(), 0.001);

		
		ctl.shutdown();

	}

	@Test
	public void testBasicFlowRudder() {

		// check initial value
		Assert.assertEquals(0.0, ctl.getValue(ParametersEnum.RUDDER).value, 0.001);
		Assert.assertEquals(50.0, ((NullDevice) (rudder.getPin())).getValue(), 0.001);

		ctl.setValue(ParametersEnum.YAW, 1.0);
		ctl.sleep(sleep);
		Assert.assertEquals(1.0, ctl.getValue(ParametersEnum.YAW).value, 0.001);
		Assert.assertEquals(1.0, ctl.getValue(ParametersEnum.RUDDER).value, 0.001);
		Assert.assertEquals(100, ((NullDevice) (rudder.getPin())).getValue(), 0.001);

		ctl.shutdown();

	}

	@Test
	public void testBasicFlowElevator() throws InterruptedException {

		// check initial value
		Assert.assertEquals(0.0, ctl.getValue(ParametersEnum.ELEVATOR).value, 0.001);
		Assert.assertEquals(50.0, ((NullDevice) (elevator.getPin())).getValue(), 0.001);

		ctl.setValue(ParametersEnum.PITCH, 1.0);
		ctl.sleep(sleep);
		Assert.assertEquals(1.0, ctl.getValue(ParametersEnum.PITCH).value, 0.001);
		Assert.assertEquals(1.0, ctl.getValue(ParametersEnum.ELEVATOR).value, 0.001);
		Assert.assertEquals(100.0, ((NullDevice) (elevator.getPin())).getValue(), 0.001);

		ctl.shutdown();

	}


}
