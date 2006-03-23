#!/bin/bash
mvn clean:clean assembly:assembly && tar tzvf target/openejb-3.0-SNAPSHOT-bin.tar.gz