package ch.pschatzmann.jflightcontroller4pi.devices;

/**
 * Recalculation of parameters which can be attached to (output) devices
 * 
 * @author pschatzmann
 *
 */
public interface IRecalculate {
	public IOutDeviceEx getDevice();

	public void recalculate();
}
