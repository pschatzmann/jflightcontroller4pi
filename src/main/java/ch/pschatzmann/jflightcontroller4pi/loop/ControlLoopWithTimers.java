package ch.pschatzmann.jflightcontroller4pi.loop;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.control.IFrequency;
import ch.pschatzmann.jflightcontroller4pi.devices.IOutDevice;
import ch.pschatzmann.jflightcontroller4pi.devices.ISensor;

/**
 * 
 * Control using Timers. We can control the speed by setting the inputTimerPeriod for the input processing
 *  and outputTimerPeriod for the (extended) output processing.
 *  
 * @author pschatzmann
 *
 */
public class ControlLoopWithTimers implements IControlLoop {
    private static final Logger log = LoggerFactory.getLogger(ControlLoopWithTimers.class);
	private FlightController controller;
	private TimerTask inputTask;
	private TimerTask outputTask;
	private TimerTask statisticsTask;
	private boolean active=false;
	private boolean blocking=true;
	private long outCount, inCount, statCountIn, statCountOut;
	private double frequency = 200;
	
	/**
	 * Empty Constructor used by Spring
	 */
	public ControlLoopWithTimers() {}

	
	/**
	 * Constructor for testing
	 * @param controller
	 * @param blocking
	 */
	public ControlLoopWithTimers(boolean blocking) {
		this.blocking = blocking;
	}
	
	/**
	 * Executes the control loop. We just process all input sensors.
	 */
	@Override
	public void run() {
		log.info("The Flight Controller is running...");
		active = true;
		
		this.processSensors();
		this.processOut();
		this.processStatistics();
		
		while (active && blocking) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected void processSensors() {
		inputTask = new TimerTask() {
			@Override
			public void run() {				
				inCount++;
				controller.getDevices().stream()
					.filter(d -> d instanceof ISensor)
					.filter(d -> FrequencyCheck.isRelevantForProcessing(getFrequency(),d.getFrequency(), inCount))
					.forEach(sensor -> ((ISensor) sensor).processInput());
				statCountIn++;
			}
		};
		Timer timer = new Timer("InputTimer");

		long delay = 0L;
		timer.scheduleAtFixedRate(inputTask, delay, (long) (1000l / this.frequency));

	}

	protected void processOut() {
		outputTask = new TimerTask() {
			@Override
			public void run() {
				outCount++;
				controller.getDevices().stream()
				.filter(d -> d instanceof IOutDevice)
				.filter(d -> FrequencyCheck.isRelevantForProcessing(getFrequency(),d.getFrequency(), outCount))
				.forEach(sensor -> ((IOutDevice) sensor).processOutput());
				statCountOut++;
			}
		};
		Timer timer = new Timer("OutputTimer");

		long delay = 0L;
		long waitMs = (long) (1000l / this.frequency);
		timer.scheduleAtFixedRate(outputTask, delay, waitMs);
	}
	
	protected void processStatistics() {
		int statisticsTimer = 60000; // 1 min
		statisticsTask = new TimerTask() {
			@Override
			public void run() {
				log.info("Input {} hz / Output {} hz", statCountIn / 60.0 ,statCountOut / 60.0 );
				// restart the statistics
				statCountIn = 0;
				statCountOut = 0;
			}
		};
		Timer timer = new Timer("StatisticsTimer");

		long delay = 0L;
		timer.scheduleAtFixedRate(statisticsTask, delay, statisticsTimer);
	}

	public FlightController getFlightController() {
		return controller;
	}

	public void setFlightController(FlightController controller) {
		this.controller = controller;
	}



	@Override
	public void stop() {
		inputTask.cancel();
		outputTask.cancel();
		statisticsTask.cancel();
		active = false;
	}

	@Override
	public boolean isStopped() {
		return !active;
	}

	public boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}

	@Override
	public void setup(FlightController controller) {
		this.controller = controller;
	}


	@Override
	public void setFrequency(double freq) {
		this.frequency = freq;
		
	}

	@Override
	public double getFrequency() {
		return this.frequency;
		
	}

}
