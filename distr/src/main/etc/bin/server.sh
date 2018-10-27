#!/bin/sh

# check java version
echo your JAVA_HOME=$JAVA_HOME
$JAVA_HOME/bin/java -version
echo Running The BioPAX Validator Web Server...

VALIDATOR_OPTS="-javaagent:lib/spring-instrument-${spring.version}.jar -Xmx4g -Dfile.encoding=UTF-8 -Dpaxtools.CollectionProvider=org.biopax.paxtools.trove.TProvider"

# if you have a Google Analytics account and want to track this app visits,
# add your code using the JVM option: -Dbiopax.validator.ga.code="UA-XXXX-Y"

# to see more options, use: java -jar biopax-validator-web.jar --help
# $1 - can be -httpPort=8080, $2 - can be -ajpPort=8009 (or whatever ports we want it use)


$JAVA_HOME/bin/java -server $VALIDATOR_OPTS -jar biopax-validator-web.jar "$1" "$2"
