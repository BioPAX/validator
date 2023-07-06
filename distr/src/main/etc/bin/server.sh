#!/bin/sh
# Start The BioPAX Validator web service/app
# (this script is processed by `mvn assembly:assembly`)

echo "JAVA_HOME=$JAVA_HOME"
echo "Running The BioPAX Validator Web Server..."

VALIDATOR_OPTS="-javaagent:lib/spring-instrument-${spring-framework.version}.jar -Xmx4g -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED"

# if you have a Google Analytics account and want to track this app visits,
# add your code using the JVM option: -Dbiopax.validator.ga.code="UA-XXXX-Y"
# to see more options, use: java -jar biopax-validator-web.war --help
$JAVA_HOME/bin/java -server $VALIDATOR_OPTS -jar biopax-validator-web.war --server.port=8080
