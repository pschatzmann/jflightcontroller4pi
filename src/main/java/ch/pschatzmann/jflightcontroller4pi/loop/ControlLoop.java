package ch.pschatzmann.jflightcontroller4pi.loop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.devices.IOutDevice;
import ch.pschatzmann.jflightcontroller4pi.devices.IOutDeviceEx;
import ch.pschatzmann.jflightcontroller4pi.devices.ISensor;
import ch.pschatzmann.jflightcontroller4pi.modes.IFlightMode;

/**
 * 
 * Simple Loop based Control. The speed of the processing can be controled with the help
 * of the sleep parameter which expects the value in milliseconds.
 * 
 * @author pschatzmann
 *
 */
public class ControlLoop implements IControlLoop {
    private static final Logger log = LoggerFactory.getLogger(ControlLoop.class);
	private FlightController controller;
	private int sleepMs = 100;
	private boolean active = false;
	
	/**
	 * Empty Constuctor used by Spring
	 */
	public ControlLoop() {}

	
	@Override
	public void setup(FlightController controller) {
		this.controller = controller;
	}

	/**
	 * Executes the control loop. We just process all input sensors.
	 */
	@Override
	public void run() {
		this.active = true;
		IFlightMode mode = controller.getMode();
		if (mode!=null) {
			mode.setup(this.controller);
		} else {
			log.info("No flight mode!");			
		}
		log.info("The Flight Controller is running...");

		int count = 0;
		while (active) {
			try {
				processSensors();
				processOut();
				
				if (sleepMs>0) {
					Thread.sleep(sleepMs);	
				}
				count++;
				if ((System.currentTimeMillis()/1000) % 10 == 0) {
					log.info("Number of loops per sec: "+ count / 10.0);
					count=0;
				}
			} catch (InterruptedException e) {
				// we do not expect this to happen. So we just abort all processing and
				// shut down
				throw new RuntimeException(e);
			}
		}
	}

	protected void processSensors() {
		this.controller.getDevices().stream()
			.filter(d -> d instanceof ISensor)
			.forEach(sensor -> ((ISensor)sensor).processInput());
	}
	
	protected void processOut() {
		this.controller.getDevices().stream()
			.filter(d -> d instanceof IOutDevice)
			.forEach(sensor -> ((IOutDevice)sensor).processOutput());
	}

	
	/**
	 * We can define a small sleeping period in the control loop in milliseconds
	 * @return
	 */
	public int getSleepMs() {
		return sleepMs;
	}

	/**
	 * Returns the sleeping period between the control loop cycles.
	 * @param sleepMs
	 */
	public void setSleepMs(int sleepMs) {
		this.sleepMs = sleepMs;
	}

	@Override
	public void stop()  {
		active = false;
	}

	@Override
	public boolean isStopped() {
		return !this.active;
	}

}
