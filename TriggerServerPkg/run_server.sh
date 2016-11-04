#!/bin/sh
java -classpath "TriggerServer.jar:pi4j-core.jar:pi4j-service.jar:pi4j-service.jar:pi4j-gpio-extension.jar:jetty-server-9.4.0.M1.jar:jetty-http-9.4.0.M1.jar:jetty-util-9.4.0.M1.jar:jetty-io-9.4.0.M1.jar:javax.servlet-api-3.1.0.jar" org.mtahq.pfc.turnstile.TriggerServer

