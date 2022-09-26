#!/bin/sh

# check java version
echo your JAVA_HOME=$JAVA_HOME
$JAVA_HOME/bin/java -version
echo Running BioPAX Validator...

# if run with java 9 and above, possibly also add these options: "--add-opens java.base/java.lang=ALL-UNNAMED"
VALIDATOR_OPTS="-javaagent:lib/spring-instrument-${spring.version}.jar -Xmx2g -Dfile.encoding=UTF-8"

# run the validator with log4j.properties and obo.properties from current directory -
#$JAVA_HOME/bin/java -cp .:biopax-validator.jar $VALIDATOR_OPTS org.biopax.validator.Main "$1" "$2" "$3" "$4" "$5" "$6" "$7"

# run with default logging and OBO properties (from the default classpath)
$JAVA_HOME/bin/java --illegal-access=permit $VALIDATOR_OPTS -jar biopax-validator.jar "$1" "$2" "$3" "$4" "$5" "$6" "$7"
