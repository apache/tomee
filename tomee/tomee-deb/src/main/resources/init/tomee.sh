#!/bin/bash
### BEGIN INIT INFO
# Provides:          tomee
# Short-Description: Apache TomEE
# Description:       Manages the Apache TomEE server.
### END INIT INFO
CATALINA_HOME=/opt/tomee
TOMCAT_USER=apachetomee

RETVAL=0
start(){
   echo "Starting TomEE: "
   su - $TOMCAT_USER -c "$CATALINA_HOME/bin/startup.sh"
   RETVAL=$?
   return $RETVAL
}

stop(){
   echo "Shutting down TomEE: "
   su - $TOMCAT_USER -c "$CATALINA_HOME/bin/shutdown.sh"
   RETVAL=$?
   return $RETVAL
}

case "$1" in
   start)
      start
      ;;
   stop)
      stop
      ;;
   force-reload)
      stop
      start
      ;;
   *)
      echo $"Usage: $0 {start|stop|force-reload}"
      exit 1
      ;;
esac
exit $?