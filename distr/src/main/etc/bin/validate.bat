# use this shell script to run the BioPAX validator from 
# the validator distribution directory (you have to switch into)

set VALIDATOR_OPTS="-javaagent:spring-instrument.jar -Xms2g -Xmx2g -Dfile.encoding=UTF-8 -Djava.io.tmpdir=tmp"

# to run using log4j and obo properties from the working (current) dir -
java -cp .;validate.jar %VALIDATOR_OPTS% org.biopax.validator.Main "$1" "$2" "$3" "$4" "$5" "$6"

