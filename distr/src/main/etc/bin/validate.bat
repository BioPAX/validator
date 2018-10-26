set VALIDATOR_OPTS="-javaagent:lib/spring-instrument-${spring.version}.jar -Xmx2g -Dfile.encoding=UTF-8"

REM make sure the right (the one you want) java installation directory 
REM goes before the system java, etc. in your current PATH

echo path: %PATH%
java --version

echo Starting BioPAX Validator...
REM run using log4j and obo properties from current dir -
REM java -cp .;biopax-validator.jar %VALIDATOR_OPTS% org.biopax.validator.Main "$1" "$2" "$3" "$4" "$5" "$6" "$7"

REM run with default logging and OBO properties (from the default classpath)
java %VALIDATOR_OPTS% -jar biopax-validator.jar "$1" "$2" "$3" "$4" "$5" "$6" "$7"
