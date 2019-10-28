package ch.pschatzmann.jflightcontroller4pi.tests;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;
import java.util.Timer;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.integration.DatagramReader;
import ch.pschatzmann.jflightcontroller4pi.integration.DatagramWriter;
import ch.pschatzmann.jflightcontroller4pi.integration.FieldDefinitions;
import ch.pschatzmann.jflightcontroller4pi.integration.IFieldDefinitions;
import ch.pschatzmann.jflightcontroller4pi.integration.Utils;
import ch.pschatzmann.jflightcontroller4pi.loop.ControlLoopWithTimers;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

public class TestDatagram {
	static FlightController ctl;
	static DatagramWriter w;
	static DatagramReader r;
	
	@BeforeClass
	public static void setup() throws IOException {
		Utils.sleep(5000);

		ctl = new FlightController();
		
		// setup reader and writer
		DatagramChannel channel = DatagramChannel.open();
		IFieldDefinitions def = new FieldDefinitions(Arrays.asList(ParametersEnum.RUDDER));
		
		w = new DatagramWriter(def, channel, 5000);
		w.setHost("localhost");
		r = new DatagramReader(def, channel, 5000);
		
		// add them as devices
		ctl.addDevices(Arrays.asList(w, r));
				
		// we stop the control loop after 5 seconds
		new Timer().schedule(Utils.timerTask(() -> ctl.shutdown()), 5000);
		
		// start a non blocking control loop
		ctl.setControlLoop(new ControlLoopWithTimers(ctl, false));
		ctl.setValue(ParametersEnum.RUDDER, 0.123);

		new Thread(() -> {
			ctl.run();
		}).start();
				

		// we give the processing some time
		Utils.sleep(7000);
	}
	
	@AfterClass
	public static void cleanup() throws IOException {
		ctl.shutdown();
	}


	@Test
	public void testValue() {
		Assert.assertEquals(0.123, ctl.getValue(ParametersEnum.RUDDER).value,0.0001);
	}

	@Ignore
	@Test
	public void testReceive() {
		System.out.println("Received: "+r.getRecordCount());
		Assert.assertTrue(r.getRecordCount() > 0);
	}

	@Test
	public void testSend() {
		System.out.println("Sent: "+w.getRecordCount());
		Assert.assertTrue(w.getRecordCount() > 0);
	}


}
