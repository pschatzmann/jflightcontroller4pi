package ch.pschatzmann.jflightcontroller4pi.tests;

import java.util.Arrays;
import java.util.Timer;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.devices.IDevice;
import ch.pschatzmann.jflightcontroller4pi.devices.IOutDevice;
import ch.pschatzmann.jflightcontroller4pi.devices.OutDevice;
import ch.pschatzmann.jflightcontroller4pi.integration.JMXParameterStore;
import ch.pschatzmann.jflightcontroller4pi.integration.Utils;
import ch.pschatzmann.jflightcontroller4pi.loop.ControlLoop;
import ch.pschatzmann.jflightcontroller4pi.loop.ControlLoopWithTimers;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;
import ch.pschatzmann.jflightcontroller4pi.protocols.NullDevice;

/**
 * Checks the basic functionality of the FlightController
 * 
 * @author pschatzmann
 *
 */

public class TestFlightController {
	static FlightController ctl;
	static OutDevice rudder;
	static OutDevice elevator;
	static OutDevice aileron;
	static OutDevice throttle;

	@BeforeClass
	public static void setup() {
		ctl = new FlightController();
		rudder = new OutDevice(ParametersEnum.RUDDER,  0.0, new NullDevice());
		elevator = new OutDevice(ParametersEnum.ELEVATOR,  0.0, new NullDevice());
		aileron = new OutDevice(ParametersEnum.AILERON,  0.0, new NullDevice());
		throttle = new OutDevice(ParametersEnum.THROTTLE,  -1.0, new NullDevice());		
	}
	
	@AfterClass
	public static void end() {
		ctl.shutdown();
	}


	@Test
	public void testDevices() {
		Assert.assertEquals(0, ctl.getDevices().size());		
		ctl.addDevices(Arrays.asList(rudder,elevator,aileron,throttle));
		Assert.assertEquals(4, ctl.getDevices().size());		
		ctl.removeDevices(Arrays.asList(rudder,elevator,aileron,throttle));
		Assert.assertEquals(0, ctl.getDevices().size());
	}
	
	@Test
	public void testControlLoopWithTimers() {
		ctl.setControlLoop(new ControlLoopWithTimers());
		Assert.assertTrue(ctl.getControlLoop() instanceof ControlLoopWithTimers);
		Assert.assertTrue(ctl.getControlLoop().isStopped());
		// schedule shutdown in 1 sec
		new Timer().schedule(Utils.timerTask(() -> ctl.shutdown()), 1000);
		// start control loop
		ctl.run();
		// run is executing until it is stopped by the timer
		Assert.assertTrue(ctl.getControlLoop().isStopped());
	}
	
	
	@Test
	public void testFrequencies() {
		FlightController fc = new FlightController();
		fc.setControlLoop(new ControlLoopWithTimers(true));
		fc.getControlLoop().setFrequency(100);
		
		TestOutputDevice d0 = new TestOutputDevice();
		TestOutputDevice d1 = new TestOutputDevice();
		d1.setFrequency(100);
		TestOutputDevice d2 = new TestOutputDevice();
		d2.setFrequency(50);
		
		TestOutputDevice d3 = new TestOutputDevice();
		d3.setFrequency(1);
		fc.addDevices(Arrays.asList(d0, d1, d2, d3));
		
		
		// schedule shutdown in 1 sec
		new Timer().schedule(Utils.timerTask(() -> fc.shutdown()), 3000);
		// start control loop
		fc.run();
		
		// run is executing until it is stopped by the timer
		Assert.assertEquals(300, d1.getCount(),1);
		Assert.assertEquals(150, d2.getCount(),1);
		Assert.assertEquals(3, d3.getCount(),1);
		Assert.assertEquals(300, d0.getCount(),1);
	}



	@Test
	public void testControlLoop() {
		ctl.setControlLoop(new ControlLoop());
		Assert.assertTrue(ctl.getControlLoop() instanceof ControlLoop);
		Assert.assertTrue(ctl.getControlLoop().isStopped());
		// schedule shutdown in 1 sec
		new Timer().schedule(Utils.timerTask(() -> ctl.shutdown()), 3000);
		// start control loop
		ctl.run();
		// run is executing until it is stopped by the timer
		Assert.assertTrue(ctl.getControlLoop().isStopped());
	}

	@Test
	public void testMode() {
		Assert.assertNotNull(ctl.getMode());		
	}

	@Test
	public void testParameters() {
		long start = System.currentTimeMillis();
		ctl.setValue(ParametersEnum.SENSORPITCH, 1.0);	
		long end = System.currentTimeMillis();
		Assert.assertEquals(1.0, ctl.getValue(ParametersEnum.SENSORPITCH).value,0.01);
		Assert.assertTrue(ctl.getValue(ParametersEnum.SENSORPITCH).timestamp>=start);
		Assert.assertTrue(ctl.getValue(ParametersEnum.SENSORPITCH).timestamp<=end);
	}

	@Test
	public void testParametersHistory() {
		ctl.setValue(ParametersEnum.SENSORPITCH, 1.0);	
		ctl.setValue(ParametersEnum.SENSORPITCH, 2.0);	

		Assert.assertEquals(2.0, ctl.getValue(ParametersEnum.SENSORPITCH).value,0.01);
		Assert.assertEquals(1.5, ctl.getParameterStore().getAvg(ParametersEnum.SENSORPITCH),0.01);
		Assert.assertEquals(2, ctl.getParameterStore().getHistory(ParametersEnum.SENSORPITCH).length);
	}
	
	
	class TestOutputDevice implements IOutDevice {
		private double frequency;
		long count = 0;

		@Override
		public void setup(FlightController flightController) {
		}

		@Override
		public void shutdown() {
		}

		@Override
		public String getName() {
			return "TestOutputDevice";
		}

		@Override
		public void setFrequency(double frequency) {
			this.frequency = frequency;
		}

		@Override
		public double getFrequency() {
			return this.frequency;
		}

		@Override
		public void processOutput() {
			count++;
		}
		
		public long getCount() {
			return count;
		}
		
	}


}
