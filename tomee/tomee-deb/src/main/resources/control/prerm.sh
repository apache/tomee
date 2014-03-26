#!/bin/sh

CATALINA_HOME=/usr/share/tomee/${tomeeVersion}
su - apachetomee -c "\$CATALINA_HOME/bin/shutdown.sh -force"

rm -f /usr/share/tomee/${tomeeVersion}/conf
rm -f /usr/share/tomee/${tomeeVersion}/logs
rm -f /usr/share/tomee/${tomeeVersion}/temp
rm -f /usr/share/tomee/${tomeeVersion}/work
rm -f /usr/share/tomee/${tomeeVersion}/webapps

rm -Rf /var/lib/tomee/${tomeeVersion}/*
rm -Rf /var/log/tomee/${tomeeVersion}/*
