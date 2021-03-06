# Auto Mode

In Auto Mode we want to be able to feed a list of waypoints to our flight controller and from there it will manage all the flight parameters automatically. 

In order to achieve this, we need one more sensor: A GPS so that we can find out our current position.

## GPS

_Beitian BN-880Q GPS+GLONASS Dual GPS Antenna Module FLASH TTL Level 9600bps for FPV Airplane RC Racing Drone_

- Output protocol:	NMEA-0183 or UBX, default NMEA-0183 protocol
- NMEA sentences: RMC, VTG, GGA, GSA, GSV, GLL
- Update frequency	: 1Hz-10Hz, default 1Hz
- Baud rate: 4800bps-921600bps, default 9600bps

Fortunately we have an Input Processor which handles NMEA, so we can define this device as follows:

	<bean id="gps" class="ch.pschatzmann.jflightcontroller4pi.devices.Sensor" >
	    <property name="flightController" ref="flightController"/>
	  	<property name="name" value="GPS"/>
	  	<property name="in">
			<ref bean="pinI2C1"/>
		</property>
		
	  	<property name="inputProcessor" ref="ch.pschatzmann.jflightcontroller4pi.guidence.navigation.GPSInputProcessor"/>		
	</bean>

And we need to configure the related Input Pins to use I2C1

- Pins PI Zero:
	1. SDA. -> SDA1 (I2C1 pin 3)
	2. GND, ground. (pin 9)
	3. TX, the data output of the module.
	4. RX, the data input of the module.
	5. VCC, 3.0V--5.5V. (pin 2)
	6. SCL -> SDA1 (I2C1 pin 5)
	
	<bean id="pinI2C1" class="ch.pschatzmann.jflightcontroller4pi.protocols.InputFromPiI2C">
	  	<property name="bus" value="1"/>
	</bean>
	
	