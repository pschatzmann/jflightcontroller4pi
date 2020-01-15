package ch.pschatzmann.jflightcontroller4pi.tests;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;
import java.util.Timer;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.integration.DatagramReader;
import ch.pschatzmann.jflightcontroller4pi.integration.DatagramWriter;
import ch.pschatzmann.jflightcontroller4pi.integration.FieldDefinitions;
import ch.pschatzmann.jflightcontroller4pi.integration.IFieldDefinitions;
import ch.pschatzmann.jflightcontroller4pi.integration.Utils;
import ch.pschatzmann.jflightcontroller4pi.loop.ControlLoopWithTimers;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;
import ch.pschatzmann.jflightcontroller4pi.tuning.FlightgearLauncher;

public class TestFlightGear {
	static FlightgearLauncher fgl = new FlightgearLauncher();
	
	@BeforeClass
	public static void startup() {
		fgl.start();
		
	}
	
	
	@Test
	public void testReceive() {
		FlightController ctl = new FlightController();
		ctl.setControlLoop(new ControlLoopWithTimers(ctl));
		IFieldDefinitions def = new FieldDefinitions(
				Arrays.asList(ParametersEnum.SENSORROLL, ParametersEnum.SENSORPITCH, ParametersEnum.SENSORYAW,
						ParametersEnum.SENSORSPEED, ParametersEnum.SENSORHEADING, ParametersEnum.SENSORPRESSURE));

		DatagramReader r = new DatagramReader(def, 7001);
		ctl.addDevices(Arrays.asList(r));

		// stop after 10 seconds
		new Timer().schedule(Utils.timerTask(() -> ctl.shutdown()), 10000);

		ctl.run();

		Assert.assertTrue(r.getRecordCount() > 0);
	}

	@Test
	public void testReceiveRawBlocking() throws IOException {
		byte[] buf = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		packet = new DatagramPacket(buf, buf.length);
		DatagramSocket socket = new DatagramSocket(7001);
		socket.receive(packet);
		String result = new String(packet.getData(), 0, packet.getLength());
		System.out.println("UPD data: " + result);
		socket.close();

		Assert.assertFalse(result.isEmpty());

	}

	@Test
	public void testReciveRawNonBlocking() throws IOException {
		int port = 7001;

		DatagramChannel channel = DatagramChannel.open();
		channel.socket().bind(new InetSocketAddress(port));
		System.out.println("udp connector port:" + port);
		// non-blocking
		channel.configureBlocking(false);
		channel.socket().setReceiveBufferSize(10240);

		ByteBuffer buffer = ByteBuffer.allocate(1024);

		boolean ok = false;
		for (int j = 0; j < 10; j++) {
			SocketAddress sa = channel.receive(buffer);
			if (sa != null) {
				buffer.flip();
				System.out.println(new String(buffer.array()));
				ok = true;
			} else {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				};
			}
		}
		channel.close();
		Assert.assertTrue(ok);

	}

	@Test
	public void testSend() {
		FlightController ctl = new FlightController();
		ctl.setControlLoop(new ControlLoopWithTimers(ctl));
		IFieldDefinitions def = new FieldDefinitions(Arrays.asList(ParametersEnum.AILERON, ParametersEnum.ELEVATOR,
				ParametersEnum.THROTTLE, ParametersEnum.RUDDER));

		ctl.addDevices(Arrays.asList(new DatagramWriter(def, 7000)));

		ctl.setValue(ParametersEnum.THROTTLE, 1.0);
		ctl.setValue(ParametersEnum.AILERON, 1.0);
		ctl.setValue(ParametersEnum.ELEVATOR, 1.0);
		ctl.setValue(ParametersEnum.RUDDER, 1.0);

		// stop after 10 seconds
		new Timer().schedule(Utils.timerTask(() -> ctl.shutdown()), 10000);

		ctl.run();
	}

	@Test
	public void testSendRaw() throws IOException {
		int port = 7000;
		String host = "pi.local";
		byte[] message = ("0.1,0.2,0.3,0.4\n").getBytes();
		InetAddress address = InetAddress.getByName(host);

		DatagramPacket packet = new DatagramPacket(message, message.length, address, port);

		DatagramSocket dsocket = new DatagramSocket(port);
		dsocket.send(packet);

		dsocket.close();

	}
	
	@Test
	public void testRestart() {
		Assert.assertTrue(fgl.start());
	}


}
