#!/bin/sh

# use this shell script to run the BioPAX validator from 
# the validator distribution directory (you have to switch into)

VALIDATOR_OPTS="-javaagent:spring-instrument.jar -Xms2g -Xmx2g -Dfile.encoding=UTF-8 -Djava.io.tmpdir=tmp"

# to run using log4j and obo properties from the working (current) dir -
$JAVA_HOME/bin/java -cp .:validate.jar $VALIDATOR_OPTS org.biopax.validator.Main "$1" "$2" "$3" "$4" "$5" "$6"

# alternatively (and as a fall-back), run with default logging and OBO properties (from validate.jar classpath)
#$JAVA_HOME/bin/java $VALIDATOR_OPTS -jar validate.jar "$1" "$2" "$3" "$4" "$5" "$6"
