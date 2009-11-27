#!/bin/sh

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

# Get standard environment variables
PRGDIR=`dirname "$PRG"`
# if one wants absolute path (also in the classpath), uncomment hte next line
#PRGDIR=`cd "$PRGDIR" ; pwd`

echo BioPAX Validator Home Dir: `cd "$PRGDIR" ; pwd`
echo Current dir: `pwd`

test -d "$PRGDIR"/build/classes || `cd "$PRGDIR" ; ant build`

test -d "$PRGDIR"/build/classes || {
       echo "Failed." >&2
       exit 1
}

CLASSPATH=$CLASSPATH:$PRGDIR/build/classes;

# interate over all jars in PRGDIR/lib and add to class path
JARS="$PRGDIR/lib/*.jar"
for jar in $(ls $JARS); do
   CLASSPATH=$CLASSPATH:$jar;
done

export CLASSPATH

echo Using Classpath: $CLASSPATH

# execute validator
$JAVA_HOME/bin/java -javaagent:$PRGDIR/lib/spring-agent.jar -Xmx2048m -Xms512m org.biopax.validator.Main $1 $2
