package ch.pschatzmann.jflightcontroller4pi.parameters;

/**
 * Enum with all supported parameters.
 * The supported parameters need to be identified by this enum which is used as key to look
 * up the value in the ParameterStore.
 * 
 * All sensor and control values are scaled between -1.0 and 1.0 with a normal value of 0.0.
 * 
 */
public enum ParametersEnum {
	// => Scaled Parameters
	ROLL, PITCH, YAW, SPEED, 
	// imu
	SENSORROLL, SENSORPITCH, SENSORYAW, SENSORSPEED, SENSORHEADING,  // Values from Sensors
	PRESSUREBASELINE, SENSORPRESSURE, ALTITUDE,
	// control planes
	RUDDER, AILERON, ELEVATOR, THROTTLE, MODE,
	// non Scaled Parameters
	GYROX, GYROY, GYROZ, 
	ACCELEROMETERX, ACCELEROMETERY, ACCELEROMETERZ, 
	MAGNETOMETERX, MAGNETOMETERY, MAGNETOMETERZ,
	IMULATITUDE, IMULONGITUDE,
	// targets from Autopilot
	AUTODIRECTION, AUTOALTITUDE,
	// gps
	GPSALTITUDE, GPSLONGITUDE, GPSLATITUDE, GPSQUALITY, GPSSPEED, GPSHEADING,
	// Other sensor data
	TEMPERATURE,
	// represent an individual pin
	PIN, NA,
	// flight mode
	FLIGHTMODE
	
}
