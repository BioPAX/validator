#!/bin/sh


# check java version
echo your JAVA_HOME=$JAVA_HOME
$JAVA_HOME/bin/java -version
echo Running BioPAX Validator...

VALIDATOR_OPTS="-Xmx2g -Dfile.encoding=UTF-8 -Dpaxtools.CollectionProvider=org.biopax.paxtools.trove.TProvider"

# to see more options, use: java -jar biopax-validator-web.jar --help
$JAVA_HOME/bin/java $VALIDATOR_OPTS -jar biopax-validator-web.jar
