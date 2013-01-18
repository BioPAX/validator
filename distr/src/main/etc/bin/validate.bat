set VALIDATOR_OPTS="-javaagent:lib/spring-instrument-${spring.version}.jar -Xms2g -Xmx2g -Dfile.encoding=UTF-8 -Djava.io.tmpdir=tmp"

# run using log4j and obo properties from current dir -
#java -cp .;validate.jar %VALIDATOR_OPTS% org.biopax.validator.Main "$1" "$2" "$3" "$4" "$5" "$6"

# run with default logging and OBO properties (from the default classpath)
java %VALIDATOR_OPTS% -jar validate.jar "$1" "$2" "$3" "$4" "$5" "$6"
