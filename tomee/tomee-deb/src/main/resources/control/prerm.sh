#!/bin/sh

CATALINA_HOME=/usr/share/tomee/${classifier}/${tomeeVersion}
su - apachetomee -c "\$CATALINA_HOME/bin/shutdown.sh -force"

rm -f /usr/share/tomee/${classifier}/${tomeeVersion}/conf
rm -f /usr/share/tomee/${classifier}/${tomeeVersion}/logs
rm -f /usr/share/tomee/${classifier}/${tomeeVersion}/temp
rm -f /usr/share/tomee/${classifier}/${tomeeVersion}/work
rm -f /usr/share/tomee/${classifier}/${tomeeVersion}/webapps

rm -Rf /var/lib/tomee/${classifier}/${tomeeVersion}/*
rm -Rf /var/log/tomee/${classifier}/${tomeeVersion}/*
