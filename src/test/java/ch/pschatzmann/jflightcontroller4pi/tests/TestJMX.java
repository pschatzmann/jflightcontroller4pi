package ch.pschatzmann.jflightcontroller4pi.tests;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.integration.JMXParameterStore;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

public class TestJMX {
	

	@Test
	public void testJMX2() {
		FlightController ctl = new FlightController();
		ctl.setValue(ParametersEnum.RUDDER, 0.1);
		JMXParameterStore jmx = new JMXParameterStore();
		ctl.addDevices(Arrays.asList(jmx));

		Assert.assertEquals(0.1, ctl.getValue(ParametersEnum.RUDDER).value, 0.001);
		Assert.assertEquals(0.1, jmx.getControlRudder(), 0.001);
	}
	
}
