#!/bin/sh

service tomee-${classifier} stop

rm /usr/share/tomee/${classifier}/${tomeeVersion}/conf
rm -Rf /var/lib/tomee/${classifier}/${tomeeVersion}/*
rm -Rf /var/log/tomee/${classifier}/${tomeeVersion}/*
