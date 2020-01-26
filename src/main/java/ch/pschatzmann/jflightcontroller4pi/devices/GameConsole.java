package ch.pschatzmann.jflightcontroller4pi.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.studiohartman.jamepad.ControllerAxis;
import com.studiohartman.jamepad.ControllerButton;
import com.studiohartman.jamepad.ControllerIndex;
import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;
import com.studiohartman.jamepad.ControllerUnpluggedException;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.control.IScaler;
import ch.pschatzmann.jflightcontroller4pi.control.NoScaler;
import ch.pschatzmann.jflightcontroller4pi.control.Scaler;
import ch.pschatzmann.jflightcontroller4pi.modes.IFlightMode;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParameterStore;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

/**
 * Feed Throttle, Rudder, Aleron and Elevator from a Game Console to the
 * ParameterStore. We also process the Buttons to control the Flight Modes.
 * 
 * @author pschatzmann
 *
 */

public class GameConsole implements ISensor {
    private static final Logger log = LoggerFactory.getLogger(GameConsole.class);
	private int sleepMs = 0;
	private ControllerManager controllers;
	private ControllerIndex currController;
	private IScaler lxScaler = new NoScaler();
	private IScaler lyScaler = new NoScaler();
	private IScaler rxScaler = new NoScaler();
	private IScaler ryScaler = new NoScaler();
	private ParametersEnum lxParameter = ParametersEnum.THROTTLE;
	private ParametersEnum lyParameter = ParametersEnum.RUDDER;
	private ParametersEnum rxParameter = ParametersEnum.AILERON;
	private ParametersEnum ryParameter = ParametersEnum.ELEVATOR;
			
	private IFlightMode offMode;
	private IFlightMode acroMode;
	private FlightController flightController;
	private double frequency;

	public GameConsole() {
	}

	/**
	 * Setup Input Thread
	 */
	@Override
	public void setup(FlightController flightController) {
		this.flightController = flightController;
		controllers = new ControllerManager();
		controllers.initSDLGamepad();

		ControllerState state = controllers.getState(0);
		if (state.isConnected) {
			currController = controllers.getControllerIndex(0);

			doVibrate(currController, 3000);
			//setupScalers(currController);

			log.info("Processing Input...");
			doVibrate(currController, 1000);
		} else {
			log.warn("There is no Game Controller available");
		}
	}

	/**
	 * Process the Input with the help of the ControllerManager. We need to setup
	 * the scales first before we can start with the processing
	 */
	@Override
	public void processInput() {
		if (currController!=null) {
			controllers.update();
			try {
				processInput(currController);
			} catch (Exception e) {
				log.error("Could not Process Input from GameConsole: " + e.getMessage());
			}
		}		
	}

	private void doVibrate(ControllerIndex currController, int ms) {
		try {
			currController.doVibration(1f, 1.f, ms);
		} catch (ControllerUnpluggedException e) {
			log.error(e.getMessage(),e);;
		}
	}

	protected void processInput(ControllerIndex currController) throws ControllerUnpluggedException {
		// define Modes
		if (currController.isButtonPressed(ControllerButton.START)) {
			flightController.setValue(ParametersEnum.MODE, 0.0);
		}
		if (currController.isButtonPressed(ControllerButton.A)) {
			flightController.setValue(ParametersEnum.MODE, 1.0);
		}
		if (currController.isButtonPressed(ControllerButton.B)) {
			flightController.setValue(ParametersEnum.MODE, 2.0);
		}

		// process left stick
		double valLX = currController.getAxisState(ControllerAxis.LEFTX);
		flightController.setValue(lxParameter, lxScaler.scale(ParametersEnum.THROTTLE, valLX));

		double valLY = currController.getAxisState(ControllerAxis.LEFTY);
		flightController.setValue(lyParameter, lyScaler.scale(ParametersEnum.RUDDER, valLY));

		// process right stick
		double valRX = currController.getAxisState(ControllerAxis.RIGHTX);
		flightController.setValue(rxParameter, rxScaler.scale(ParametersEnum.AILERON, valRX));

		double valRY = currController.getAxisState(ControllerAxis.RIGHTY);
		flightController.setValue(ryParameter, ryScaler.scale(ParametersEnum.ELEVATOR, valRY));
	}


	@Override
	public void shutdown() {
		log.info("shutdown");
		controllers.quitSDLGamepad();
	}

	/**
	 * Implement Switching of the Flight Mode
	 * 
	 * @author pschatzmann
	 *
	 */
	protected class ModeSwitch {

		ModeSwitch(ParameterStore parameterStore, IDevice controlDevice) {

			// register event handler
			parameterStore.register(ParametersEnum.MODE, value -> {
				int mode = (int) value.value;
				switch (mode) {
				case 0:
					flightController.setMode(offMode);
					break;
				case 1:
					flightController.setMode(acroMode);
					break;

				}
			});

			// per default we switch off the processing
			parameterStore.setValue(ParametersEnum.MODE, 0.0);
		}
	}

	public int getSleepMs() {
		return sleepMs;
	}

	public void setSleepMs(int sleepMs) {
		this.sleepMs = sleepMs;
	}

	public IFlightMode getOffMode() {
		return offMode;
	}

	public void setOffMode(IFlightMode offMode) {
		this.offMode = offMode;
	}

	public IFlightMode getAcroMode() {
		return acroMode;
	}

	public void setAcroMode(IFlightMode acroMode) {
		this.acroMode = acroMode;
	}

	public FlightController getFlightController() {
		return flightController;
	}

	public void setFlightController(FlightController flightController) {
		this.flightController = flightController;
	}
	
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * @return the lxScaler
	 */
	public IScaler getLxScaler() {
		return lxScaler;
	}

	/**
	 * @param lxScaler the lxScaler to set
	 */
	public void setLxScaler(IScaler lxScaler) {
		this.lxScaler = lxScaler;
	}

	/**
	 * @return the lyScaler
	 */
	public IScaler getLyScaler() {
		return lyScaler;
	}

	/**
	 * @param lyScaler the lyScaler to set
	 */
	public void setLyScaler(IScaler lyScaler) {
		this.lyScaler = lyScaler;
	}

	/**
	 * @return the rxScaler
	 */
	public IScaler getRxScaler() {
		return rxScaler;
	}

	/**
	 * @param rxScaler the rxScaler to set
	 */
	public void setRxScaler(IScaler rxScaler) {
		this.rxScaler = rxScaler;
	}

	/**
	 * @return the ryScaler
	 */
	public IScaler getRyScaler() {
		return ryScaler;
	}

	/**
	 * @param ryScaler the ryScaler to set
	 */
	public void setRyScaler(IScaler ryScaler) {
		this.ryScaler = ryScaler;
	}

	/**
	 * @return the lxParameter
	 */
	public ParametersEnum getLxParameter() {
		return lxParameter;
	}

	/**
	 * @param lxParameter the lxParameter to set
	 */
	public void setLxParameter(ParametersEnum lxParameter) {
		this.lxParameter = lxParameter;
	}

	/**
	 * @return the lyParameter
	 */
	public ParametersEnum getLyParameter() {
		return lyParameter;
	}

	/**
	 * @param lyParameter the lyParameter to set
	 */
	public void setLyParameter(ParametersEnum lyParameter) {
		this.lyParameter = lyParameter;
	}

	/**
	 * @return the rxParameter
	 */
	public ParametersEnum getRxParameter() {
		return rxParameter;
	}

	/**
	 * @param rxParameter the rxParameter to set
	 */
	public void setRxParameter(ParametersEnum rxParameter) {
		this.rxParameter = rxParameter;
	}

	/**
	 * @return the ryParameter
	 */
	public ParametersEnum getRyParameter() {
		return ryParameter;
	}

	/**
	 * @param ryParameter the ryParameter to set
	 */
	public void setRyParameter(ParametersEnum ryParameter) {
		this.ryParameter = ryParameter;
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
