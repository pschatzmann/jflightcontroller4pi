package ch.pschatzmann.jflightcontroller4pi.devices;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.data.IData;

public interface IInputProcessor {
	void processInput(FlightController controller, IData input);
	boolean isValid(IData input);
}
