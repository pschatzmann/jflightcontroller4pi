package ch.pschatzmann.jflightcontroller4pi.integration;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.devices.IDevice;
import ch.pschatzmann.jflightcontroller4pi.devices.ISensor;
import ch.pschatzmann.jflightcontroller4pi.modes.IFlightMode;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParameterValue;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;
import io.dronefleet.mavlink.Mavlink2Message;
import io.dronefleet.mavlink.MavlinkConnection;
import io.dronefleet.mavlink.MavlinkMessage;
import io.dronefleet.mavlink.common.Attitude;
import io.dronefleet.mavlink.common.AutopilotVersion;
import io.dronefleet.mavlink.common.CommandAck;
import io.dronefleet.mavlink.common.CommandLong;
import io.dronefleet.mavlink.common.FollowTarget;
import io.dronefleet.mavlink.common.GlobalPositionInt;
import io.dronefleet.mavlink.common.GpsInput;
import io.dronefleet.mavlink.common.Heartbeat;
import io.dronefleet.mavlink.common.ManualControl;
import io.dronefleet.mavlink.common.MavAutopilot;
import io.dronefleet.mavlink.common.MavCmd;
import io.dronefleet.mavlink.common.MavEstimatorType;
import io.dronefleet.mavlink.common.MavModeFlag;
import io.dronefleet.mavlink.common.MavParamType;
import io.dronefleet.mavlink.common.MavProtocolCapability;
import io.dronefleet.mavlink.common.MavResult;
import io.dronefleet.mavlink.common.MavState;
import io.dronefleet.mavlink.common.MavSysStatusSensor;
import io.dronefleet.mavlink.common.MavType;
import io.dronefleet.mavlink.common.MissionCount;
import io.dronefleet.mavlink.common.MissionRequestList;
import io.dronefleet.mavlink.common.Odometry;
import io.dronefleet.mavlink.common.ParamRequestList;
import io.dronefleet.mavlink.common.ParamRequestRead;
import io.dronefleet.mavlink.common.ParamSet;
import io.dronefleet.mavlink.common.ParamValue;
import io.dronefleet.mavlink.common.ProtocolVersion;
import io.dronefleet.mavlink.common.RawImu;
import io.dronefleet.mavlink.common.SysStatus;
import io.dronefleet.mavlink.common.SystemTime;

/**
 * Simple Mavlink command handler which uses UDP to communicate
 * 
 * @author pschatzmann
 *
 */
public class MavlinkDevice implements IDevice {
	private static final Logger log = LoggerFactory.getLogger(MavlinkDevice.class);
	private static String uid = UUID.randomUUID().toString();
	private FlightController flightController;
	private int port = 14550; 
	private int systemId = 1; // this device
	private int componentId = 1;
	private MavlinkConnection connection;
	private boolean isArmed = false;
	private long lastHartBeat;
	private int version = 200;
	private long bootTime = System.currentTimeMillis();
	private boolean setup = true;
	private DatagramSocket socket;
	private UDPInputStream is;
	private UDPOutputStream out;
	private double frequency;

	@Override
	public void setup(FlightController flightController)  {
		log.info("setup");
		try {
			this.flightController = flightController;
			socket = new DatagramSocket(port);
			is = new UDPInputStream(socket);
			out = new UDPOutputStream(socket);
			connection = MavlinkConnection.create(is, out);
			log.info("Mavlink is available on port {}", port);
	
			// read and process messages
			new Thread() {
				public void run() {
					log.info("setting up Mavlink read thread");
					while (true) {
						MavlinkMessage message = next();
						if (message != null) {
							processMessage(message);
						}
					}
				};
			}.start();
	
			// send messages
			new Thread() {
				public void run() {
					log.info("setting up Mavlink write thread");
					while (true) {
						try {
							if (connection != null && is.getAddress()!=null) {
								out.setAddress(is.getAddress());
								log.info("send...");
								if (setup) {
									sendHeatBeat();
									sendStatus();
									setup = false;
								}
								sendIMU();
								sendHeatBeat();
							}
						} catch (Exception e) {
							log.error(e.getMessage(),e);
						}
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
					}
				}
	
			}.start();
		} catch(Exception ex) {
			log.error(ex.getMessage(),ex);
		}

	}


	protected void sendIMU() throws IOException {
		// Raw IMU values
		RawImu imu = RawImu.builder().xacc(value(ParametersEnum.ACCELEROMETERX)).yacc(value(ParametersEnum.ACCELEROMETERY))
				.zacc(value(ParametersEnum.ACCELEROMETERZ)).xgyro(value(ParametersEnum.GYROX)).ygyro(value(ParametersEnum.GYROY))
				.zgyro(value(ParametersEnum.GYROZ)).xmag(value(ParametersEnum.MAGNETOMETERX)).ymag(value(ParametersEnum.MAGNETOMETERY))
				.zmag(value(ParametersEnum.MAGNETOMETERZ)).temperature(value(ParametersEnum.TEMPERATURE)).build();
		connection.send2(systemId, componentId, imu);
		
		// IMU pitch/roll/yaw
		float pitch = (float) Math.toRadians(this.flightController.getValue(ParametersEnum.SENSORPITCH).value);
		float roll = (float) Math.toRadians(this.flightController.getValue(ParametersEnum.SENSORROLL).value);
		float yaw = (float) Math.toRadians(this.flightController.getValue(ParametersEnum.SENSORYAW).value);
		long time =   System.currentTimeMillis() - bootTime;
		Attitude att = Attitude.builder().pitch((float) pitch).roll(roll).yaw(yaw).timeBootMs(time).build();
		connection.send2(systemId, componentId, att);
		log.info("pitch: {} / roll: {} / yaw: {}",pitch, roll, yaw);				
		
	};

	protected int value(ParametersEnum p) {
		ParameterValue pv = this.flightController.getValue(p);
		return pv == null ? 0 : (int) pv.value;
	}

	protected void sendStatus() throws IOException {
		SysStatus status;
		if (this.flightController.getValue(ParametersEnum.GYROX) == null) {
			status = SysStatus.builder().onboardControlSensorsPresent(MavSysStatusSensor.MAV_SYS_STATUS_SENSOR_3D_ACCEL)
					.onboardControlSensorsPresent(MavSysStatusSensor.MAV_SYS_STATUS_SENSOR_3D_GYRO)
					.onboardControlSensorsPresent(MavSysStatusSensor.MAV_SYS_STATUS_SENSOR_3D_MAG)
					.onboardControlSensorsPresent(MavSysStatusSensor.MAV_SYS_STATUS_SENSOR_ABSOLUTE_PRESSURE).build();
			send(status);
		} else {
			status = SysStatus.builder().onboardControlSensorsEnabled(MavSysStatusSensor.MAV_SYS_STATUS_SENSOR_3D_ACCEL)
					.onboardControlSensorsEnabled(MavSysStatusSensor.MAV_SYS_STATUS_SENSOR_3D_GYRO)
					.onboardControlSensorsEnabled(MavSysStatusSensor.MAV_SYS_STATUS_SENSOR_3D_MAG)
					.onboardControlSensorsEnabled(MavSysStatusSensor.MAV_SYS_STATUS_SENSOR_ABSOLUTE_PRESSURE).build();
			send(status);
		}
		

	}

	protected void sendHeatBeat() {
		Heartbeat heartbeat = Heartbeat.builder().type(MavType.MAV_TYPE_VTOL_QUADROTOR.MAV_TYPE_GENERIC).baseMode(getMavModeFlag()).customMode(0)
				.autopilot(MavAutopilot.MAV_AUTOPILOT_GENERIC).systemStatus(getMavState()).mavlinkVersion(version).build();

		// Write an unsigned heartbeat
		try {
			connection.send2(systemId, componentId, heartbeat);
		} catch (IOException e) {
			log.error("Could not send via mavlink", e);
			close();
		}
	}
	
	protected void send(Object status) {
		try {
			connection.send2(systemId, componentId, status);
		} catch (IOException e) {
			log.error("Could not send message", e);
		}
	}


	protected MavModeFlag getMavModeFlag() {
		MavModeFlag result = null;
		result = MavModeFlag.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED;
		String name = this.flightController.getMode().getName();
		if (name.equalsIgnoreCase("stabilizedMode")) {
			result = MavModeFlag.MAV_MODE_FLAG_STABILIZE_ENABLED;
		}
		return isArmed() ? result.MAV_MODE_FLAG_SAFETY_ARMED : result;

	}

	protected MavState getMavState() {
		return MavState.MAV_STATE_ACTIVE;
	}

	protected void processMessage(MavlinkMessage message) {
		Object payload = getPayload(message);
		
		if (message.getOriginSystemId()==systemId) {
			log.warn("Message received from same system - this is ignored!");
			return;
		}
		
		
		if (payload instanceof SystemTime) {
			SystemTime time = (SystemTime) payload;
			log.info("time {}", time.timeUnixUsec());

		} else if (payload instanceof Heartbeat) {
			// This is a heartbeat message
			MavlinkMessage<Heartbeat> heartbeatMessage = (MavlinkMessage<Heartbeat>) message;
			lastHartBeat = System.currentTimeMillis();
		} else if (payload instanceof ParamRequestList) {
			log.debug("ParamRequestList");
			for (ParametersEnum p : ParametersEnum.values()) {
				ParameterValue pv = flightController.getParameterStore().getValue(p);
				if (pv != null) {
					ParamValue parValue = ParamValue.builder().paramId(p.name()).paramType(MavParamType.MAV_PARAM_TYPE_REAL64).paramValue((float)pv.value)
							.build();
					send(parValue);
					log.info("-> {}", p.name());
				}
			}
		} else if (payload instanceof ParamRequestRead) {
			log.debug("ParamRequestRead");
			ParamRequestRead ps = (ParamRequestRead) payload;			
			ParametersEnum par = ParametersEnum.valueOf(ps.paramId());
			// return the actual parameter value
			ParamValue parValue = ParamValue.builder().paramId(par.name()).paramType(MavParamType.MAV_PARAM_TYPE_REAL64).paramValue((float) this.flightController.getValue(par).value).build();
			send(parValue);
		} else if (payload instanceof ParamSet) {
			log.debug("ParamSet");
			ParamSet ps = (ParamSet) payload;			
			ParametersEnum par = ParametersEnum.valueOf(ps.paramId());
			boolean updated = true;
			if (par==ParametersEnum.FLIGHTMODE) {
				updated = this.setMode(ps.paramValue());	
			}
			// return the actual parameter value
			ParamValue parValue = ParamValue.builder().paramId(par.name()).paramType(MavParamType.MAV_PARAM_TYPE_REAL64).paramValue((float) this.flightController.getValue(par).value).build();
			send(parValue);
		} else if (payload instanceof MissionRequestList) {
			log.debug("MissionRequestList");
			MissionCount mc = MissionCount.builder().count(0).build();
			send(mc);
		} else if (payload instanceof ManualControl) {
			log.debug("ManualControl");
			ManualControl mc = (ManualControl) payload;
			if (isValid(mc.z()))
				flightController.setValue(ParametersEnum.THROTTLE, mc.z()/1000.0);
			if (isValid(mc.r()))
				flightController.setValue(ParametersEnum.RUDDER, mc.r()/1000.0);
			if (isValid(mc.x()))
				flightController.setValue(ParametersEnum.ELEVATOR, mc.x()/-1000.0);
			if (isValid(mc.y()))
				flightController.setValue(ParametersEnum.AILERON, mc.y()/1000.0);
			
		} else if (payload instanceof FollowTarget) {
			log.info("FollowTarget {} -> not implemente!",payload.toString());
			FollowTarget ft = (FollowTarget) payload;
			
			
	
		} else if (payload instanceof CommandLong) {
			CommandLong cmd = (CommandLong) payload;
			CommandAck ack;
			switch (cmd.command().entry()) {
			case MAV_CMD_COMPONENT_ARM_DISARM:
				log.info("MAV_CMD_COMPONENT_ARM_DISARM");
				boolean active = (cmd.param1() == 1.0f);
				this.setArmed(active);
				log.info("armed {}", active);
				ack = CommandAck.builder().command(MavCmd.MAV_CMD_COMPONENT_ARM_DISARM).result(MavResult.MAV_RESULT_ACCEPTED).build();
				send(ack);
				break;
			case MAV_CMD_REQUEST_PROTOCOL_VERSION:
				log.debug("MAV_CMD_REQUEST_PROTOCOL_VERSION");
				ack = CommandAck.builder().command(MavCmd.MAV_CMD_REQUEST_PROTOCOL_VERSION).result(MavResult.MAV_RESULT_UNSUPPORTED).build();
				send(ack);
				break;
			case MAV_CMD_REQUEST_AUTOPILOT_CAPABILITIES:
				log.info("MAV_CMD_REQUEST_AUTOPILOT_CAPABILITIES");
				float version = cmd.param1();
				ack = CommandAck.builder().command().result(MavResult.MAV_RESULT_UNSUPPORTED).build();
				send(ack);
				//AutopilotVersion apv = AutopilotVersion.builder().capabilities(MavProtocolCapability.MAV_PROTOCOL_CAPABILITY_COMPASS_CALIBRATION.MAV_PROTOCOL_CAPABILITY_FLIGHT_INFORMATION.MAV_PROTOCOL_CAPABILITY_FLIGHT_TERMINATION).uid2(uid.toString().getBytes()).build();
				//send(apv);
				break;
			default:
				log.info("CommandLong: {} {} {}", payload.getClass().getSimpleName(), cmd.command().entry(), payload.toString());
				break;
			}

		} else {
			log.info("Unhandled Message: {} {}", payload.getClass().getSimpleName(), message);
		}
	}

	private boolean isValid(int y) {
		return y >= -1000 && y <= 1000; 
	}


	private boolean setMode(float mode) {
		try {
			int pos = (int) (mode/1.4E-45f);
			IFlightMode fmode = this.flightController.getModes().get(pos);
			this.flightController.setMode(fmode);
			log.info("setMode {}",fmode.getName());
			return true;
		} catch(Exception ex) {
			log.error("Could not set mode",ex);
			return false;
		}
	}

	protected Object getPayload(MavlinkMessage message) {
		Object payload = message.getPayload();
		Mavlink2Message message2;
		if (message instanceof Mavlink2Message) {
			// This is a Mavlink2 message.
			message2 = (Mavlink2Message) message;
			payload = message2.getPayload();
		}
		return payload;
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
				log.error("Next has failed - we reset the connection", e);
				close();
			}
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
		}
		return null;
	}

	private void close() {
		log.info("close");
		try {
			if (socket!=null) {
				socket.close();
			}
		} catch (Exception e1) {
		}

		connection = null;
		setup = true;
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

	@Override
	public void shutdown() {
		log.info("shutdown");
		close();
		try {
			//serverSocket.close();
			socket.close();
		} catch (Exception e) {
		}

	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Checks if the vehicle is armed
	 * 
	 * @return the isArmed
	 */
	public boolean isArmed() {
		return isArmed;
	}

	/**
	 * Defines the armed status of the vehicle
	 * 
	 * @param isArmed
	 *            the isArmed to set
	 */
	public void setArmed(boolean isArmed) {
		this.isArmed = isArmed;
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
	public String toString() {
		return this.getName();
	}


}
