package ch.pschatzmann.jflightcontroller4pi.imu;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParameterValue;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

/**
 * We try to estimate the speed with the help of the accelerometer. Please note
 * that errors are cumulating up quite quickly.
 * 
 * @author pschatzmann
 *
 */
public class Velocity {
	int cycle = 0;
	int resetCycle = 100;
	double currentSpeed = 0;
	long lastTime;
	
	public Velocity() {		
	}

	public void recordAccelerometer(FlightController flightController, Value3D v) {
		// reset the speed to the one calculated from the GPS
		if (cycle++%resetCycle==0) {
			ParameterValue pv = flightController.getValue(ParametersEnum.GPSSPEED);
			if (!pv.isEmpty()) {
				currentSpeed = pv.value;
			}
		}
		// calculate speed in time difference  m/sec^2 * sec => m/sec
		double vx = v.x() * (v.timestamp - lastTime) / 1000.0 ;
		double vy = v.y() * (v.timestamp - lastTime) / 1000.0 ;
		double vz = v.z() * (v.timestamp - lastTime) / 1000.0 ;
		
		// calculate the total speed
		currentSpeed += Math.sqrt(Math.pow(vx, 2)+Math.pow(vy, 2)+Math.pow(vz, 2));
		
		// save new estimate
		flightController.setValue(ParametersEnum.SPEED,currentSpeed);
		
		lastTime = v.timestamp;
	}
	
	/**
	 * Returns the estimated speed
	 * @return
	 */
	public double getSpeed() {
		return this.currentSpeed;
	}

	public int getResetCycle() {
		return resetCycle;
	}

	public void setResetCycle(int resetCycle) {
		this.resetCycle = resetCycle;
	}
	

}
