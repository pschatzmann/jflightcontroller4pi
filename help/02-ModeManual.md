# Manual Mode  

## Overview


Manual Mode was very easy to implement. We do not rely on any sensors and the input from the end user is directly fed into the primary flight control surfaces:

	- YAW -> RUDDER 
	- ROLL -> AILERON 
	- PITCH -> ELEVETOR 
	- SPEED -> THROTTLE


For more sophisticated flight modes we need to use this sensor information to measure the current state of the airplane in order to stabilize the flight 

## Implementation

This model can be defined quite easily with the following XML


	<bean id="manualMode" class="ch.pschatzmann.jflightcontroller4pi.modes.FlightMode" >
	    <property name="flightController" ref="flightController"/>
	    <property name="name" value="manual" /> 
	  	<property name="rules">
	  		<list>
            	<ref bean="manualModeRuleRudder" />
            	<ref bean="manualModeRuleElevator" />                
            	<ref bean="manualModeRuleAileron" />                
            	<ref bean="manualModeRuleThrottle" />                
        	</list>
	  	</property>	 	  	
	</bean>
	
	<bean id="manualModeRuleRudder" class="ch.pschatzmann.jflightcontroller4pi.modes.manualModeRule" >
	    <property name="flightController" ref="flightController"> </property>
	    <property name="device" ref="rudder"> </property>
	    <property name="parameterFrom" value="YAW"> </property>
	</bean>
	<bean id="manualModeRuleElevator" class="ch.pschatzmann.jflightcontroller4pi.modes.manualModeRule" >
	    <property name="flightController" ref="flightController"> </property>
	    <property name="device" ref="elevator"> </property>
	    <property name="parameterFrom" value="PITCH"> </property>
	</bean>
	<bean id="manualModeRuleAileron" class="ch.pschatzmann.jflightcontroller4pi.modes.manualModeRule" >
	    <property name="flightController" ref="flightController"> </property>
	    <property name="device" ref="aileron"> </property>
	    <property name="parameterFrom" value="ROLL"> </property>
	</bean>
	<bean id="manualModeRuleThrottle" class="ch.pschatzmann.jflightcontroller4pi.modes.manualModeRule" >
	    <property name="flightController" ref="flightController"> </property>
	    <property name="device" ref="throttle"> </property>
	    <property name="parameterFrom" value="SPEED"> </property>
	</bean>


And we must register the mode in the flight controller and select it as actual mode

	<bean id="flightController" class="ch.pschatzmann.jflightcontroller4pi.FlightController" >
	    ...
	  	<property name="modes">
	  		<list>
            	<ref bean="offMode" />
            	<ref bean="manualMode" />
        	</list>
 	    <property name="mode" ref="manualMode"/> 
		...

With this setup we can plug in now the Game Controller to the USB port of our raspberry pi and 
control our airplane. 

In the next step we show how to set up the remote control.

