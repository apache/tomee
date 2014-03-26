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
TOMEE_VERSION=${tomeeVersion}
TOMEE_CLASSIFIER=${classifier}
CATALINA_HOME=/usr/share/tomee
TOMCAT_USER=apachetomee
RETVAL=0

start(){
   echo "Starting TomEE (\$TOMEE_CLASSIFIER \$TOMEE_VERSION): "
   su - \$TOMCAT_USER -c "\$CATALINA_HOME/\$TOMEE_CLASSIFIER/\$TOMEE_VERSION/bin/catalina.sh start"
   RETVAL=\$?
   return \$RETVAL
}

stop(){
   echo "Shutting down TomEE (\$TOMEE_CLASSIFIER \$TOMEE_VERSION): "
   su - \$TOMCAT_USER -c "\$CATALINA_HOME/\$TOMEE_CLASSIFIER/\$TOMEE_VERSION/bin/shutdown.sh -force"
   RETVAL=\$?
   return \$RETVAL
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