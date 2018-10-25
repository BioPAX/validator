#!/bin/sh

# check java version
echo your JAVA_HOME=$JAVA_HOME
$JAVA_HOME/bin/java -version
echo Running BioPAX Validator...

VALIDATOR_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED -javaagent:lib/spring-instrument-${spring.version}.jar -Xmx2g -Dfile.encoding=UTF-8 -Dpaxtools.CollectionProvider=org.biopax.paxtools.trove.TProvider"

# run the validator with log4j.properties and obo.properties from current directory -
#$JAVA_HOME/bin/java -cp .:biopax-validator.jar $VALIDATOR_OPTS org.biopax.validator.Main "$1" "$2" "$3" "$4" "$5" "$6" "$7"

# run with default logging and OBO properties (from the default classpath)
$JAVA_HOME/bin/java $VALIDATOR_OPTS -jar biopax-validator.jar "$1" "$2" "$3" "$4" "$5" "$6" "$7"
