#!/bin/bash

# build the jar if it does not exist
if [ ! -f ./target/jflightcontroller-0.0.1-SNAPSHOT-jar-with-dependencies.jar ]; then
	echo "Building jars..."
	mvn -Dmaven.test.skip=true package
fi

# run the jar
echo "Starting..."
INTERFACE=wlan0
PORT=7002
IP=$(ifconfig $INTERFACE | grep inet | grep -v inet6 | awk '{print $2}')
echo "You can connect with your JMX console to $IP:$PORT"
java -Dlogback.configurationFile=file:./logback.xml -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=$PORT -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=$IP -jar ./target/jflightcontroller-0.0.1-SNAPSHOT-jar-with-dependencies.jar 

