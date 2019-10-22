#!/bin/bash
# mvn exec:java -Dexec.mainClass="ch.pschatzmann.jflightcontroller4pi.Main"
java -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9010 -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -jar ./target/jflightcontroller-0.0.1-SNAPSHOT-jar-with-dependencies.jar 
