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
 * Simple Loop based Control. The speed of the processing is controled with the help
 * of a Thread sleep which is calculated from the indicated frequency.
 * 
 * @author pschatzmann
 *
 */
public class ControlLoop implements IControlLoop {
    private static final Logger log = LoggerFactory.getLogger(ControlLoop.class);
	private FlightController controller;
	private boolean active = false;
	private double frequency = 200;
	private long count = 0;
	private boolean blocking = true;

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
		if (this.isBlocking()) {
			doRun();
		} else {
			// if this needs to be non blocking we need to run the loop in it's own thread
			new Thread() {
			  public void run() {
				  doRun();
			  }
			}.start();
		}
	}


	private void doRun() {
		this.active = true;
		IFlightMode mode = controller.getMode();
		if (mode!=null) {
			mode.setup(this.controller);
		} else {
			log.info("No flight mode!");			
		}
		log.info("The Flight Controller is running...");

		while (active) {
			try {
				processSensors();
				processOut();
				
				if (getSleepMs()>0) {
					long sleep = getSleepMs();
					Thread.sleep(sleep);	
				}
				count++;
				if ((System.currentTimeMillis()/1000) % 60 == 0) {
					log.info("Number of loops per sec: "+ count / 10.0);
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
			.filter(d -> FrequencyCheck.isRelevantForProcessing(getFrequency(),d.getFrequency(), count))
			.forEach(sensor -> ((ISensor)sensor).processInput());
	}
	
	protected void processOut() {
		this.controller.getDevices().stream()
			.filter(d -> d instanceof IOutDevice)
			.filter(d -> FrequencyCheck.isRelevantForProcessing(getFrequency(),d.getFrequency(), count))
			.forEach(sensor -> ((IOutDevice)sensor).processOutput());
	}

	
	@Override
	public void stop()  {
		active = false;
	}

	@Override
	public boolean isStopped() {
		return !this.active;
	}

	@Override
	public void setFrequency(double frequency) {
		this.frequency = frequency;
		
	}

	@Override
	public double getFrequency() {
		return this.frequency;
	}
	
	protected int getSleepMs() {
		return  this.frequency>0 ? (int)(1000.0 / this.frequency): 0;
	}


	@Override
	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
		
	}

	@Override
	public boolean isBlocking() {
		return blocking;
	}


}
