#!/bin/bash
### BEGIN INIT INFO
# Provides:          tomee
# Short-Description: Apache TomEE ${tomeeVersion}
# Description:       Manages the Apache TomEE server.
### END INIT INFO
CATALINA_HOME=/usr/share/tomee/${tomeeVersion}
TOMCAT_USER=apachetomee

RETVAL=0
start(){
   echo "Starting TomEE ${properties.tomeeVersion}: "
   su - \$TOMCAT_USER -c "\$CATALINA_HOME/bin/catalina.sh start"
   RETVAL=\$?
   return \$RETVAL
}

stop(){
   echo "Shutting down TomEE ${properties.tomeeVersion}: "
   su - \$TOMCAT_USER -c "\$CATALINA_HOME/bin/shutdown.sh -force"
   RETVAL=\$?
   return \$RETVAL
}

case "\$1" in
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