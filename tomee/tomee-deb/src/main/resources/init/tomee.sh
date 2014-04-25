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

### BEGIN INIT INFO
# Provides:          tomee-${classifier}
# Required-Start:    \$local_fs \$remote_fs \$network
# Required-Stop:     \$local_fs \$remote_fs \$network
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Apache TomEE ${classifier} ${tomeeVersion}
# Description:       Manages the Apache TomEE server.
### END INIT INFO
TOMEE_HOME=/usr/share/tomee-${classifier}
export CATALINA_BASE=/var/lib/tomee-${classifier}
TOMEE_USER=apachetomee

start(){
   echo "Starting TomEE ${classifier} (version ${tomeeVersion}): "
   su - \$TOMEE_USER --preserve-environment -c "\$TOMEE_HOME/bin/catalina.sh start"
   return \$?
}

stop(){
   echo "Shutting down TomEE ${classifier} (version ${tomeeVersion}): "
   su - \$TOMEE_USER --preserve-environment -c "\$TOMEE_HOME/bin/shutdown.sh -force"
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
   force-reload)
      stop
      start
      ;;
   version)
      echo "TomEE ${classifier} (version ${tomeeVersion})"
      ;;
   *)
      echo \$"Usage: \$0 {start|stop|restart|version}"
      exit 1
      ;;
esac
exit \$?