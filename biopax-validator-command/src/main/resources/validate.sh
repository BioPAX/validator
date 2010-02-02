#!/bin/sh

# find validator's home dir...
# resolve links - $0 may be a softlink
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

PRGDIR=`dirname "$PRG"`
# make it full path
PRGDIR=`cd "$PRGDIR" ; pwd -P`

echo BioPAX Validator Home Dir: $PRGDIR

test -d "$PRGDIR"/build/classes || { 
	echo Building the BioPAX Validator Application from Sources...
	$CURDIR=`pwd -P`
	cd $PRGDIR 
	ant build
	cd $CURDIR
}

test -d "$PRGDIR"/build/classes || {
       echo "Build Failed." >&2
       exit 1
}

echo Starting...

CLASSPATH=$PRGDIR/build/classes
# interate over all jars in PRGDIR/lib and add to class path
JARS="$PRGDIR/lib/*.jar"
for jar in $(ls $JARS); do
   CLASSPATH=$CLASSPATH:$jar;
done

export CLASSPATH
#echo Using Classpath: $CLASSPATH

# run validator
$JAVA_HOME/bin/java -cp $CLASSPATH -javaagent:$PRGDIR/lib/spring-agent.jar -Xmx2048m -Xms256m org.biopax.validator.Main $1 $2
