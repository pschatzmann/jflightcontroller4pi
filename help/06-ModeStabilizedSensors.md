# Stabilized Mode

## Sensors

The goal is get the following information about the state of the airplane:

	SENSORROLL 
	SENSORPITCH 
	SENSORYAW
	SENSORHEADING 
	SENSORALTITUDE 
	SENSORSPEED 



## Input Sensors 

In order to achieve this we need the follwoing sensors:

- Gyroscope
- Accelerometer 
- Compass
- Barometer


As input example we use the following chip

_CJMCU 10DOF 9 Axis MPU9250 + BMP180 Sensor Module Gyro Acceleration + Barometric Height Sensor_

- 3-axis gyroscope + 3-axis accelerometer + 3-axis magnetic field + barometric altitude sensor
- Module uses imported chips: integrated high-precision 9-axis high precision pressure MPU9250 chip + BMP180 chips
- Power supply: 3.3v
- Communication: SPI / IIC

This sensor is providing following parameters and writes them into the parameter store:

	GYROX, 
	GYROY, 
	GYROZ, 
	ACCELEROMETERX, 
	ACCELEROMETERY, 
	ACCELEROMETERZ, 
	MAGNETOMETERX, 
	MAGNETOMETERY, 
	MAGNETOMETERZ,


### Implementation

	<bean id="imu" class="ch.pschatzmann.jflightcontroller4pi.devices.Sensor" >
	    <property name="flightController" ref="flightController"/>
	  	<property name="name" value="gyroscope-accelerometer-compass-altitude"/>
	  	<property name="in">
			<ref bean="pinI2C0"/>
		</property>
		
	  	<property name="inputProcessor">
	  		<bean id="imu" class="ch.pschatzmann.jflightcontroller4pi.devices.InputProcessor">
	  			<property name "inputParameters">
	  				<list>
	  					<value>GYROX</value>
	  					<value>GYROY</value>
	  					<value>GYROZ</value>
	  					<value>ACCELEROMETERX</value>
	  					<value>ACCELEROMETERX</value>
	  					<value>ACCELEROMETERX</value>
	  					<value>MAGNETOMETERX</value>
	  					<value>MAGNETOMETERY</value>
	  					<value>MAGNETOMETERZ</value>
	  					<value>SENSORALTITUDE</value>
	  				</list>
	  			</property>
	  		</bean>
	  	</property>		
	</bean>


And finally we need to configure the related Input Pins to use I2C0

	<bean id="pinI2C0" class="ch.pschatzmann.jflightcontroller4pi.protocols.InputFromPiI2C">
	  	<property name="bus" value="0"/>
	</bean>


## Inertial Measurement Unit (IMU) 

We implemented a software IMU which reports a body's specific force, angular rate, and the orientation of the body. The IMU is calculating the following output parameters with the help of the input from the sensors:

	SENSORROLL 
	SENSORPITCH 
	SENSORYAW
	SENSORHEADING 
	SENSORALTITUDE 
	SENSORSPEED 


### Implementation

Finally we can define our IMU (device) in XML and make it available to the stabilizedMode

	<bean id="imu" class="ch.pschatzmann.jflightcontroller4pi.guidence.imu.IMUDevice" >
	    <property name="flightController" ref="flightController"/>
	</bean>
	
	<!-- Leveled Mode -->
	<bean id="stabilizedMode" class="ch.pschatzmann.jflightcontroller4pi.modes.FlightMode" >
	    <property name="flightController" ref="flightController"/>
	    <property name="name" value="Leveled" /> 
	  	<property name="rules">
	  		<list>
            	<ref bean="leveledModeRuleRudder" />
            	<ref bean="leveledModeRuleElevator" />                
            	<ref bean="leveledModeRuleAileron" />  
            	<ref bean="acroModeRuleThrottle" />                              
        	</list>
	  	</property>	 
	  	<property name="devices">
	  		<list>
            	<ref bean="imu" />
        	</list>
	  	</property>	 
	</bean>


## Simulation

Since we do this the first time, we do not want to do this exercise with a real plane: We can use our integration into Flightgear instead to validate if we can implement a stable, working system.

We use the Cessna 172P for our simulation. In real life things tend to get quite complicated but we try to simplify with the assumption that a model airplane is good natured and has much bigger safety limits. 

The take off will not be automatic. It will be up to the pilot to raise the elevator when the take off speed has been reached. We also do not implement any automatic stall avoidance, detection or recovery. 

 
	