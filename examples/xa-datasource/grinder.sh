#!/bin/bash

#
# Prior to running this script, start TomEE with:
# mvn clean install tomee:run
#
TESTS=$(ls $PWD/target/*-tests.jar)
PROPERTIES=$PWD/target/grinder.properties
echo "
grinder.script $PWD/target/test-classes/grinder.py
grinder.processes 1
grinder.threads 2
grinder.runs 0
grinder.jvm.classpath $TESTS
grinder.logDirectory $PWD/target/logs
grinder.numberOfOldLogs 0
" > $PROPERTIES


(
cd $(dirname "$0")/grinder/
java -cp grinder.jar net.grinder.Console &
sleep 10
)
(
cd $(dirname "$0")/grinder/
java -cp grinder.jar net.grinder.Grinder $PROPERTIES
)