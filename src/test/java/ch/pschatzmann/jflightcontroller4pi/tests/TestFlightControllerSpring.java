package ch.pschatzmann.jflightcontroller4pi.tests;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.devices.OutDevice;
import ch.pschatzmann.jflightcontroller4pi.integration.DatagramReader;
import ch.pschatzmann.jflightcontroller4pi.integration.DatagramWriter;
import ch.pschatzmann.jflightcontroller4pi.loop.ControlLoopWithTimers;
import ch.pschatzmann.jflightcontroller4pi.loop.IControlLoop;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;
import ch.pschatzmann.jflightcontroller4pi.protocols.NullDevice;

/**
 * Checks the basic functionality of the FlightController
 * 
 * @author pschatzmann
 *
 */

public class TestFlightControllerSpring {
	private static Logger log = LoggerFactory.getLogger(TestFlightControllerSpring.class);
	static FlightController ctl;
	static OutDevice rudder;
	static OutDevice elevator;
	static OutDevice aileron;
	static OutDevice throttle;
	int sleep = 500;
	
	@BeforeClass
	public static void setup() {
		@SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext("plane.xml");
		ctl = (FlightController) context.getBean("flightController");
		
		// select mode
		ctl.selectMode("manualMode");

		// determine control planes
		rudder = (OutDevice)ctl.getControlDevice(ParametersEnum.RUDDER);
		elevator = (OutDevice)ctl.getControlDevice(ParametersEnum.ELEVATOR);
		aileron = (OutDevice)ctl.getControlDevice(ParametersEnum.AILERON);
		throttle = (OutDevice)ctl.getControlDevice(ParametersEnum.THROTTLE);
		
		// make sure that we do not get disturbed by Datagrams
		DatagramReader r = (DatagramReader) ctl.getDevice("DatagramReader");
		r.setActive(false);
		DatagramWriter w = (DatagramWriter) ctl.getDevice("DatagramWriter");
		w.setActive(false);
		
		// use nun blocking control loop for the junit test
		IControlLoop cl = new ControlLoopWithTimers(false);
		ctl.setControlLoop(cl);

		new Thread(() -> {
			ctl.run();
		}).start();
		
		ctl.sleep(10000);
				
	}

	@AfterClass
	public static void cleanup() {
		if (ctl!=null)
			ctl.shutdown();
	}

	

	@Test
	public void testControls() {
		//Assert.assertEquals(8, ctl.getDevices().size());
		Assert.assertNotNull(ctl);
		Assert.assertNotNull(rudder);
		Assert.assertNotNull(elevator);
		Assert.assertNotNull(aileron);
		Assert.assertNotNull(throttle);
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
		Assert.assertTrue(ctl.getValue(ParametersEnum.AILERON).value <=  0.0);

		ctl.setValue(ParametersEnum.ROLL, 1.0);
		ctl.sleep(sleep);

		Assert.assertEquals(1.0, ctl.getValue(ParametersEnum.ROLL).value, 0.001);
		Assert.assertTrue(ctl.getValue(ParametersEnum.AILERON).value >=  0.0);

		ctl.setValue(ParametersEnum.ROLL, 0.0);
		ctl.sleep(sleep);
		Assert.assertEquals(0.0, ctl.getValue(ParametersEnum.ROLL).value, 0.01);
		Assert.assertEquals(0.0, ctl.getValue(ParametersEnum.AILERON).value, 0.01);

	}
	
	@Test
	public void testBasicFlowElevator() throws InterruptedException {
		// check initial value
		ctl.setValue(ParametersEnum.PITCH, 0.0);
		ctl.sleep(sleep);
		Thread.sleep(1000);
		Assert.assertEquals(0.0, ctl.getValue(ParametersEnum.ELEVATOR).value, 0.001);

		ctl.setValue(ParametersEnum.PITCH, 1.0);
		ctl.sleep(sleep);
		Thread.sleep(1000);
		Assert.assertEquals(1.0, ctl.getValue(ParametersEnum.PITCH).value, 0.001);
		Assert.assertTrue(ctl.getValue(ParametersEnum.ELEVATOR).value > 0);

	}

	@Test
	public void testBasicFlowRudder() {

		// check initial value
		Assert.assertEquals(0.0, ctl.getValue(ParametersEnum.RUDDER).value, 0.001);

		ctl.setValue(ParametersEnum.YAW, 1.0);
		ctl.sleep(sleep);
		Assert.assertEquals(1.0, ctl.getValue(ParametersEnum.YAW).value, 0.001);
		// no impact
		Assert.assertEquals(0.0, ctl.getValue(ParametersEnum.RUDDER).value, 0.001);

	}


	@Test
	public void testBasicFlowThrottleMin() {

		// min value 
		ctl.setValue(ParametersEnum.PITCH, -1.0);
		ctl.setValue(ParametersEnum.SPEED, -1.0);
		ctl.sleep(sleep);
		Assert.assertEquals(-1, ctl.getValue(ParametersEnum.SPEED).value, 0.001);
		Assert.assertEquals(0.2, ctl.getValue(ParametersEnum.THROTTLE).value, 0.001);

	}
	
	@Test
	public void testBasicFlowThrottleMax() {

		// max value 
		ctl.setValue(ParametersEnum.PITCH, 1.0);
		ctl.setValue(ParametersEnum.SPEED, 1.0);
		ctl.sleep(sleep);
		Assert.assertEquals(1.0, ctl.getValue(ParametersEnum.SPEED).value, 0.001);
		Assert.assertTrue(ctl.getValue(ParametersEnum.THROTTLE).value > 0.0);

	}
	
	@Test
	public void testBasicFlowLevelled() {

		// max value 
		ctl.setValue(ParametersEnum.PITCH, 0.0);
		ctl.setValue(ParametersEnum.SPEED, 0.0);
		ctl.sleep(sleep);
		Assert.assertEquals(0.0, ctl.getValue(ParametersEnum.SPEED).value, 0.001);

	}
	
	
	@Test
	public void testIMU() {
		ctl.setMode("stabilizedMode");
		Assert.assertNotNull(ctl.getDevice("IMUDevice"));

	}


}
