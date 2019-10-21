## Configuration / Settings

The airplane controller defines the airplane with it's components and parameters are defined with the help of a Spring xml definition file.

- Parameter Store
- Modes
- Control Planes
- Devices
- Control Loop

Here is a minimum working example of a prototype airplane that just implements a dummy Mode which leaves the airplane in an inactive neutral state. In this example the autoput and input pins are not connected to any real pins on the PI.

We can manage and monitor the basic airplane state with the help of JMX by starting the standard Java jconsole!


		<?xml version="1.0" encoding="UTF-8"?>
		<!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN 2.0//EN"
		        "http://www.springframework.org/dtd/spring-beans-2.0.dtd">
		
		<beans>
			<bean id="flightController" class="ch.pschatzmann.jflightcontroller4pi.FlightController" >
			    <property name="parameterStore">
					<bean id="ParameterStore" class="ch.pschatzmann.jflightcontroller4pi.parameters.ParameterStore" />
			    </property>
		 	    <property name="mode" ref="offMode"/> 
			  	<property name="devices">
			  		<list>
		            	<ref bean="jmx" />
		        	</list>
			  	</property>
			  	<property name="modes">
			  		<list>
		            	<ref bean="offMode" />
		        	</list>
			  	</property>
			  	<property name="controlLoop">
			  		<bean class="ch.pschatzmann.jflightcontroller4pi.control.ControlLoopWithTimers">
					    <property name="flightController" ref="flightController"/>
			  		</bean>
			  	</property>
			  		    	    
			</bean>
		
			<!-- Devices -->
			<bean id="gameConsole" class="ch.pschatzmann.jflightcontroller4pi.devices.GameConsole" >
			    <property name="flightController" ref="flightController"/>
			</bean>
		
			<!-- JMX -->
			<bean id="jmx" class="ch.pschatzmann.jflightcontroller4pi.integration.JMXParameterStore" >
			    <property name="flightController" ref="flightController"/>
			</bean>
		
			<!-- Control Planes -->
			
			<bean id="aileron" class="ch.pschatzmann.jflightcontroller4pi.devices.OutDevice" >
			    <property name="flightController" ref="flightController"/>
			  	<property name="controlParameter" value="AILERON"/>
			  	<property name="pin" ref="pin1"/>	  	
			</bean>
		
			<bean id="elevator" class="ch.pschatzmann.jflightcontroller4pi.devices.OutDevice" >
			    <property name="flightController" ref="flightController"/>
			  	<property name="controlParameter" value="ELEVATOR"/>
			  	<property name="pin" ref="pin2"/>	  	
			</bean>
		
			<bean id="rudder" class="ch.pschatzmann.jflightcontroller4pi.devices.OutDevice" >
			    <property name="flightController" ref="flightController"/>
			  	<property name="controlParameter" value="RUDDER"/>
			  	<property name="pin" ref="pin3"/>	  	
			</bean>
		
			<bean id="throttle" class="ch.pschatzmann.jflightcontroller4pi.devices.OutDevice" >
			    <property name="flightController" ref="flightController"/>
			  	<property name="controlParameter" value="THROTTLE"/>
			  	<property name="pin" ref="pin4"/>	  	
			  	<property name="defaultSetting" value="0.2"/>	  	
			</bean>
		
			<!-- Modes -->
			
			<bean id="offMode" class="ch.pschatzmann.jflightcontroller4pi.modes.FlightMode" >
			    <property name="flightController" ref="flightController"/> 
			    <property name="name" value="Off" /> 
			</bean>
		</beans>

		
		
	