#!/bin/bash

JAR=$PWD/../../itests/openejb-itests-app/target/openejb-itests-app-3.0-incubating-SNAPSHOT.ear

cd target/

tar xzvf openejb-3.0-incubating-SNAPSHOT-bin.tar.gz
cd openejb-3.0-incubating-SNAPSHOT
cp $JAR beans/
./bin/openejb start

