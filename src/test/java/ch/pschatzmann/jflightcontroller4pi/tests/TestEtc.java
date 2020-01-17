package ch.pschatzmann.jflightcontroller4pi.tests;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.devices.OutDevice;
import ch.pschatzmann.jflightcontroller4pi.integration.DatagramReader;
import ch.pschatzmann.jflightcontroller4pi.integration.DatagramWriter;
import ch.pschatzmann.jflightcontroller4pi.loop.ControlLoopWithTimers;
import ch.pschatzmann.jflightcontroller4pi.loop.IControlLoop;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

public class TestEtc {

	/**
	 * Testing the remote control 
	 */
	
	@Test
	public void testReceive() {
		ApplicationContext context = new ClassPathXmlApplicationContext("config.xml");
		FlightController ctl = (FlightController) context.getBean("flightController");
		
		// select mode
		ctl.selectMode("stabilizedMode");
		
		// make sure that we do not get disturbed by Datagrams
		DatagramWriter w = (DatagramWriter) ctl.getDevice("DatagramWriter");
		w.setActive(false);

		DatagramReader r = (DatagramReader) ctl.getDevice("DatagramReader");
		r.setActive(true);
		
		// use nun blocking control loop for the junit test
		IControlLoop cl = new ControlLoopWithTimers(false);
		ctl.setControlLoop(cl);

		ctl.run();
		ctl.sleep(10000);
		ctl.stop();
		
		Assert.assertTrue(r.getRecordCount()>0);
		
	}
}
