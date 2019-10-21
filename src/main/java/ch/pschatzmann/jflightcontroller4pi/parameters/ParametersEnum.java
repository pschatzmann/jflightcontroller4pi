package ch.pschatzmann.jflightcontroller4pi.parameters;

/**
 * Enum with all supported parameters.
 * All supported parameters need to be identified by this enum which is used as key to look
 * up the value in the ParameterStore
 */
public enum ParametersEnum {
	// standardized input controls -1.0 >= x <= 1.0
	ROLL, PITCH, YAW, SPEED, 
	// control planes
	RUDDER, AILERON, ELEVATOR, THROTTLE, MODE,
	// imu
	SENSORROLL, SENSORPITCH, SENSORYAW, SENSORSPEED, SENSORHEADING, SENSORALTITUDE, // Values from Sensors
	GYROX, GYROY, GYROZ, 
	ACCELEROMETERX, ACCELEROMETERY, ACCELEROMETERZ, 
	MAGNETOMETERX, MAGNETOMETERY, MAGNETOMETERZ,
	// targets from Autopilot
	AUTODIRECTION, AUTOALTITUDE,
	// gps
	GPSALTITUDE, GPSLONGITUDE, GPSLATITUDE, GPSQUALITY, GPSSPEED, GPSHEADING
	
}
