#!/bin/bash

# build the jar if it does not exist
if [ ! -f ./target/jflightcontroller-0.0.1-SNAPSHOT-jar-with-dependencies.jar ]; then
	mvn -Dmaven.test.skip=true package
fi

# run the jar
java -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9010 -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -jar ./target/jflightcontroller-0.0.1-SNAPSHOT-jar-with-dependencies.jar 
