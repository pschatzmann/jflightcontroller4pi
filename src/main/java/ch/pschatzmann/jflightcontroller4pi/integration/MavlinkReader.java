package ch.pschatzmann.jflightcontroller4pi.integration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.devices.ISensor;
import io.dronefleet.mavlink.Mavlink2Message;
import io.dronefleet.mavlink.MavlinkConnection;
import io.dronefleet.mavlink.MavlinkMessage;
import io.dronefleet.mavlink.common.GlobalPositionInt;
import io.dronefleet.mavlink.common.Heartbeat;
import io.dronefleet.mavlink.common.MavAutopilot;
import io.dronefleet.mavlink.common.MavModeFlag;
import io.dronefleet.mavlink.common.MavState;
import io.dronefleet.mavlink.common.MavSysStatusSensor;
import io.dronefleet.mavlink.common.MavType;
import io.dronefleet.mavlink.common.SysStatus;

/**
 * Simple Mavlink command handler
 * 
 * @author pschatzmann
 *
 */
public class MavlinkReader implements ISensor {
	private static final Logger log = LoggerFactory.getLogger(MavlinkReader.class);
	private FlightController flightController;
	private MavlinkConnection connection;
	private String secretKey;
	private int port = 5760;
	private ByteBuffer buffer = ByteBuffer.allocate(1023);
	private ServerSocketChannel ssc;
	private SocketChannel sc;
	int systemId = 255;
	int componentId = 0;

	@Override
	public void setup(FlightController flightController) throws IOException {
		this.flightController = flightController;
		ssc = ServerSocketChannel.open();
		ssc.socket().bind(new InetSocketAddress(port));
		ssc.configureBlocking(false);
		log.info("Mavlink is available on port {}", port);

	}

	@Override
	public void shutdown() {
		try {
			if (ssc != null)
				ssc.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public void processInput() {
		try {
			if (sc == null || !sc.isConnected()) {
				connection = null;
				sc = ssc.accept();
				if (sc != null) {
					connection = MavlinkConnection.create(Channels.newInputStream(sc), Channels.newOutputStream(sc));
					System.out.println("Mavlink connected");
					
					sendHeatBeat();
					
					SysStatus status = SysStatus.builder()
							.onboardControlSensorsPresent(MavSysStatusSensor.MAV_SYS_STATUS_SENSOR_3D_ACCEL)
							.onboardControlSensorsPresent(MavSysStatusSensor.MAV_SYS_STATUS_SENSOR_3D_GYRO)
							.onboardControlSensorsPresent(MavSysStatusSensor.MAV_SYS_STATUS_SENSOR_3D_MAG)
							.onboardControlSensorsPresent(MavSysStatusSensor.MAV_SYS_STATUS_SENSOR_ABSOLUTE_PRESSURE)
							.build();
					connection.send2(systemId, componentId, status);

					status = SysStatus.builder()
							.onboardControlSensorsEnabled(MavSysStatusSensor.MAV_SYS_STATUS_SENSOR_3D_ACCEL)
							.onboardControlSensorsEnabled(MavSysStatusSensor.MAV_SYS_STATUS_SENSOR_3D_GYRO)
							.onboardControlSensorsEnabled(MavSysStatusSensor.MAV_SYS_STATUS_SENSOR_3D_MAG)
							.onboardControlSensorsEnabled(MavSysStatusSensor.MAV_SYS_STATUS_SENSOR_ABSOLUTE_PRESSURE)
							.build();
					connection.send2(systemId, componentId, status);

					
					GlobalPositionInt gps  = GlobalPositionInt.builder().lat(462000000).lon(72500000).alt(503000).build();
					connection.send2(systemId, componentId, gps);

				}
			}

			if (connection != null) {
				sendHeatBeat();
				processMessage();
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	private void sendHeatBeat() {
		Heartbeat heartbeat = Heartbeat.builder()
		          .type(MavType.MAV_TYPE_GENERIC)
		          .baseMode(MavModeFlag.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED)
		          .autopilot(MavAutopilot.MAV_AUTOPILOT_GENERIC)
		          .systemStatus(MavState.MAV_STATE_ACTIVE)
		          .mavlinkVersion(3)
		          .build();

		// Write an unsigned heartbeat
		try {
			connection.send2(systemId, componentId, heartbeat);
		} catch (IOException e) {
			log.error("Could not send via mavlink", e);
		}

	}

	private void processMessage() {
		log.info("processMessage");
		MavlinkMessage message;
		while ((message = next()) != null) {
			log.info(message.toString());

			if (message instanceof Mavlink2Message) {
				// This is a Mavlink2 message.
				Mavlink2Message message2 = (Mavlink2Message) message;

				if (message2.isSigned()) {
					// This is a signed message. Let's validate its
					// signature.
					if (message2.validateSignature(secretKey.getBytes())) {
						// Signature is valid.
					} else {
						// Signature validation failed. This message is
						// suspicious and
						// should not be handled. Perhaps we should log
						// this
						// incident.
					}
				} else {
					// This is an unsigned message.
				}
			} else {
				// This is a Mavlink1 message.
			}

			// When a message is received, its payload type isn't
			// statically
			// available.
			// We can resolve which kind of message it is by its
			// payload,
			// like
			// so:
			if (message.getPayload() instanceof Heartbeat) {
				// This is a heartbeat message
				MavlinkMessage<Heartbeat> heartbeatMessage = (MavlinkMessage<Heartbeat>) message;
			}
		}
	}

	/**
	 * Returns the next mavlink message
	 * 
	 * @return
	 */
	protected MavlinkMessage next() {
		if (connection != null) {
			try {
				return connection.next();
			} catch (IOException e) {
				log.error("next has failed", e);
			}
		}
		return null;
	}

	/**
	 * @return the secretKey
	 */
	public String getSecretKey() {
		return secretKey;
	}

	/**
	 * @param secretKey
	 *            the secretKey to set
	 */
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
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

}
