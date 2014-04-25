#!/bin/bash -e

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

export CATALINA_HOME=/usr/share/tomee/$classifier/$tomeeVersion
export CATALINA_BASE=\$(cd \$(dirname \$0) && cd .. && pwd)

buildIt() {
    if [ -z "\$1" ]; then
        echo "Usage: \$0 build {path}"
        echo "       You should define the home directory of your personal TomEE Server."
        exit 1
    fi
    if [ -d "\$1" ]; then
        echo "'\$1' already exists. Please choose another path."
        exit 1
    fi
    home=\$1
    mkdir -p \$home/conf
    mkdir -p \$home/logs
    mkdir -p \$home/temp
    mkdir -p \$home/webapps
    mkdir -p \$home/work
    mkdir -p \$home/bin
    cp \$CATALINA_HOME/bin/tomee-instance.sh \$home/bin
    cp \$CATALINA_HOME/conf/server.xml \$home/conf
    echo "Your private Apache TomEE ($classifier) server is ready."
    echo "Use the '\$home/bin/tomee-instance.sh' script to manage this instance."
    echo "   \$home/bin/tomee-instance.sh start"
    echo "   \$home/bin/tomee-instance.sh stop"
    echo "Dont forget to change the ports defined in '\$home/conf/server.xml' before using it."
    return 0
}

checkInstance(){
    if [ \$CATALINA_HOME == "\$CATALINA_BASE" ]; then
        echo "Please use 'service tomee-plus start|stop' to start|stop the system-wide instance of TomEE."
        echo "    CATALINA_HOME=\$CATALINA_HOME"
        echo "    CATALINA_BASE=\$CATALINA_BASE"
        exit 1
    fi
}

start(){
    checkInstance
    echo "Starting TomEE ${classifier} (version ${tomeeVersion}): "
    \$CATALINA_HOME/bin/catalina.sh start
    return \$?
}

stop(){
    checkInstance
    echo "Shutting down TomEE ${classifier} (version ${tomeeVersion}): "
    \$CATALINA_HOME/bin/shutdown.sh
    return \$?
}

case \$1 in
   start)
      start
      ;;
   stop)
      stop
      ;;
   restart)
      stop
      start
      ;;
   build)
      buildIt \$2
      ;;
   *)
      echo "Usage: \$0 {start|stop|restart|build}"
      exit 1
      ;;
esac
exit \$?