#!/bin/sh

# check java version
echo your JAVA_HOME=$JAVA_HOME
$JAVA_HOME/bin/java -version
echo Running BioPAX Validator...

JDK_JAVA_OPTIONS="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED"
VALIDATOR_OPTS="-javaagent:lib/spring-instrument-${spring-framework.version}.jar -Xmx2g -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom"

# run the validator with an alternative log4j.properties/logback.xml or obo.properties, e.g., from current directory -
#$JAVA_HOME/bin/java -cp .:biopax-validator.jar $VALIDATOR_OPTS org.biopax.validator.Main "$1" "$2" "$3" "$4" "$5" "$6" "$7"

# run with built-in logging and OBO properties (from the default classpath)
$JAVA_HOME/bin/java $VALIDATOR_OPTS -jar biopax-validator.jar "$1" "$2" "$3" "$4" "$5" "$6" "$7"
