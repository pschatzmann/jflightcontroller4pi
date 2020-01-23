package ch.pschatzmann.jflightcontroller4pi.tests;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.control.AvgFilter;
import ch.pschatzmann.jflightcontroller4pi.control.IFilter;
import ch.pschatzmann.jflightcontroller4pi.control.MedianFilter;
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
	
	@Test
	public void testCast() {
		Assert.assertEquals(2, (int)2.0);
		Assert.assertEquals(2.0, (float)2,0.001);
	}
	
	@Test
	public void testMedianFilter() {
		IFilter f = new MedianFilter(5);
		Assert.assertEquals(0, f.getValue(),0.001);
		f.add(10.0);
		Assert.assertEquals(10.0, f.getValue(),0.001);

		f.add(1.0);
		f.add(2.0);
		Assert.assertEquals(2.0, f.getValue(),0.001);

		f.add(20.0);
		f.add(30.0);
		f.add(40.0);
		Assert.assertEquals(20.0, f.getValue(),0.001);
		
	}
	
	@Test
	public void testAvgFilter() {
		IFilter f = new AvgFilter(5);
		Assert.assertEquals(0, f.getValue(),0.001);
		f.add(10.0);
		Assert.assertEquals(10.0, f.getValue(),0.001);

		f.add(1.0);
		f.add(2.0);
		Assert.assertEquals(13.0/3.0, f.getValue(),0.001);

		f.add(20.0);
		f.add(30.0);
		f.add(40.0);
		Assert.assertEquals(93.0/5.0, f.getValue(),0.001);
		
	}

}
