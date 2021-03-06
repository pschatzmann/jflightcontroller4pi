# Stabilized Mode

## Goal

In Manual Mode the user input was directly controlling the setting of the 
- Elevator
- Aileron
- Rudder and
- Motor Speed

It is quite difficult to fly like this however. So we would like to improve the usability with a new mode.

In **Stabilized Mode** we want that the user input is defining a stable state by providing the 

- Pitch (in degrees) -> Elevator / Motor Speed 
- Roll (in degrees) -> Aileron
- Throttle -> Motor Speed 

as input. So with all inputs in neutral position, the airplane should fly straight ahead in a constant
altitude with a constant speed. And we would just fly a stable curve by changing the roll.

So define the new mode in XML

	<!-- Stabilized Mode -->
	<bean id="stabilizedMode" class="ch.pschatzmann.jflightcontroller4pi.modes.FlightMode" >
	    <property name="flightController" ref="flightController"/>
	    <property name="name" value="Leveled" /> 
	  	<property name="rules">
	  		<list>
            	<ref bean="stabilizedModeRuleRudder" />
            	<ref bean="stabilizedModeRuleElevator" />                
            	<ref bean="stabilizedModeRuleAileron" />  
            	<ref bean="stabilizedModeRuleThrottle" />                              
        	</list>
	  	</property>	 
	  	<property name="devices">
	  		<list>
            	<ref bean="imu" />
        	</list>
	  	</property>	 
	</bean>

We will use multiple [PID controllers](https://en.wikipedia.org/wiki/PID_controller) to manage the plane. To start with a simple setup we just configure a proportional controller: 
	

## Control

### Elevator Control Loop

 - Control Input: Pitch (in degrees) - [Range -30 - 30 degrees] 
 - Measured Input: Pitch (in degrees) from Sensors
 - Output: Elevator (-1 to 1)
 
To avoid stalls because we climb too steep, we limit the elevation angle: an imput of 30 will need 
to result in a elevator setting of 1. So the proportinal (p) value needs to be  1/30 or 0.0333333!

	<bean id="stabilizedModeRuleElevator" class="ch.pschatzmann.jflightcontroller4pi.modes.PIDModeRule" >
	    <property name="flightController" ref="flightController"/>
	    <property name="device" ref="elevator"/>
	    <property name="desiredSetpoint" value="PITCH"/>
	    <property name="measuredVariable" value="SENSORPITCH"/>
	    <property name="pid" >
	    	<bean id="pidElevator" class="ch.pschatzmann.jflightcontroller4pi.control.PIDController" >
			    <property name="p" value="0.0333"/>
			    <property name="i" value="0.0"/>
			    <property name="d" value="0.0"/>
			</bean>
	    </property>
	</bean>
	

### Turns 
 
In 3 Channel Mode Airplanes the turns are usually done with the help of the Rudder.  
In real life however, this is mainly done with the **ailerons** and the rudder is just used to coordinate the turn in the air.

In our simplified implementation we will just use the ailerons:

 - Control Input: Roll (in degrees) - [Range -30 -30 degrees]
 - Measured Input: Roll (in degrees) from Sensors
 - Output: Aileron (-1 to 1)

In order to avoid unstable turns we limit the roll angle.

	<bean id="stabilizedModeRuleAileron" class="ch.pschatzmann.jflightcontroller4pi.modes.PIDModeRule" >
	    <property name="flightController" ref="flightController"/>
	    <property name="device" ref="aileron"/>
	    <property name="desiredSetpoint" value="ROLL"/>
	    <property name="measuredVariable" value="SENSORROLL"/>
	    <property name="pid" >
	    	<bean id="pidAileron" class="ch.pschatzmann.jflightcontroller4pi.control.PIDController" >
			    <property name="p" value="0.0333"/>
			    <property name="i" value="0.0"/>
			    <property name="d" value="0.0"/>
			</bean>
	    </property>
	</bean>

We are not changing the rudder: For the completeness sake we define the rule for the rudder, but keep the p, i and d at 0. 
	
	<bean id="stabilizedModeRuleRudder" class="ch.pschatzmann.jflightcontroller4pi.modes.PIDModeRule" >
	    <property name="flightController" ref="flightController"/>
	    <property name="device" ref="rudder"/>
	    <property name="desiredSetpoint" value="YAW"/>
	    <property name="measuredVariable" value="SENSORYAW"/>	    
	    <property name="pid" >
	    	<bean id="pidRudder" class="ch.pschatzmann.jflightcontroller4pi.control.PIDController" >
			    <property name="p" value="0.0"/>
			    <property name="i" value="0.0"/>
			    <property name="d" value="0.0"/>
			</bean>
	    </property>
	    
	</bean>

### Speed Control Loop
 
For the speed we have a small problem because we do not have any reliable way to measure the actual speed. Therefore we will just combine the input from the requested user speed with the requested pitch
in order to determine a combined throttle value: We want the throttle never to be lower den 0.2 to
prevent to starve the motor.

We want to give the YAW more weight then the SPEED because we want to drive the process from the yaw and give the user just the possibility to add some optional correction.

	<bean id="stabilizedModeRuleThrottle" class="ch.pschatzmann.jflightcontroller4pi.modes.MixingModeRule" >
	    <property name="flightController" ref="flightController"/>
	    <property name="device" ref="throttle"/>
	    <property name="mixerComponents">
	  		<list>
				<bean id="inputYaw" class="ch.pschatzmann.jflightcontroller4pi.control.MixerComponent" >
				    <property name="parameter" value="YAW"/>
				    <property name="weight" value="3"/>
				    <property name="scaler">
						<bean class="ch.pschatzmann.jflightcontroller4pi.control.Scaler" >
					   	    <property name="inputMin" value="-1.0"/>
		   					<property name="inputMax" value="1.0"/>
		    					<property name="outputMin" value="0.2"/>
		    					<property name="outputMax" value="1.0"/>
				    		</bean>
				    </property>
				</bean>
				<bean id="inputSpeed" class="ch.pschatzmann.jflightcontroller4pi.control.MixerComponent" >
				    <property name="parameter" value="SPEED"/>
				    <property name="weight" value="1"/>
				    <property name="scaler">
						<bean class="ch.pschatzmann.jflightcontroller4pi.control.Scaler" >
					   	    <property name="inputMin" value="-1.0"/>
		   					<property name="inputMax" value="1.0"/>
		    					<property name="outputMin" value="0.2"/>
		    					<property name="outputMax" value="1.0"/>
				    		</bean>
				    </property>
				</bean>
        		</list>
	  	</property>	 
	
	</bean>


We did not show yet how to determine the necessary Sensor Values which are needed for the PID controller. This is our next step.
 
 
 