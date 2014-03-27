#!/bin/bash
### BEGIN INIT INFO
# Provides:          tomee-${classifier}
# Required-Start:    \$local_fs \$remote_fs \$network
# Required-Stop:     \$local_fs \$remote_fs \$network
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Apache TomEE ${classifier} ${tomeeVersion}
# Description:       Manages the Apache TomEE server.
### END INIT INFO
TOMEE_HOME=/usr/share/tomee/${classifier}/${tomeeVersion}
export CATALINA_BASE=/var/lib/tomee/${classifier}/${tomeeVersion}
TOMEE_USER=apachetomee

start(){
   echo "Starting TomEE (\$TOMEE_CLASSIFIER \$TOMEE_VERSION): "
   su - \$TOMEE_USER --preserve-environment -c "\$TOMEE_HOME/bin/catalina.sh start"
   return \$?
}

stop(){
   echo "Shutting down TomEE (\$TOMEE_CLASSIFIER \$TOMEE_VERSION): "
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
   *)
      echo \$"Usage: \$0 {start|stop|restart}"
      exit 1
      ;;
esac
exit \$?