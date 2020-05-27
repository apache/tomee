#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
CWD=$(pwd)

cd "$DIR"

if [ -d "jakartaee-prototype" ]; then
  rm -rf jakartaee-prototype 
fi

if [ -d "transformer-0.1.0-SNAPSHOT" ]; then
  rm -rf transformer-0.1.0-SNAPSHOT 
fi

git clone https://github.com/tbitonti/jakartaee-prototype.git
cd jakartaee-prototype
./gradlew assemble
cd ..

unzip jakartaee-prototype/transformer/build/distributions/transformer-0.1.0-SNAPSHOT.zip
for f in ../tomee/apache-tomee/target/*.zip; do ./transformer-0.1.0-SNAPSHOT/bin/transformer "$f"; done

cd "$CWD"
