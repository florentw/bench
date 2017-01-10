#!/usr/bin/env bash

# Detects java command
which java 2>/dev/null 1>&2
if [ $? -eq 0 ]
then
    javaCmd=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    javaCmd="$JAVA_HOME/bin/java"
else
    echo "ERROR: Could not locate java, check the \$PATH variable."
    exit 1
fi

# Check for right java version
javaVersion=$(${javaCmd} -version 2>&1 | sed 's/java version "\(.*\)\.\(.*\)\..*"/\1\2/; 1q')
if [ "$javaVersion" = "" ] || [ ${javaVersion} -lt 18 ]
then
    echo "ERROR: JDK version 1.8 or more is required."
    echo "Version $javaVersion found instead for '$javaCmd':"
    ${javaCmd} -version
    exit 1
fi

if [ $# -ne 1 ]
then
    echo "Usage:"
    echo "$0 <config file>"
    exit 1
fi

${javaCmd} -jar target/agent.jar $1
