#!/bin/sh

CATALINA_HOME=/usr/share/tomee
su - apachetomee -c "$CATALINA_HOME/bin/shutdown.sh -force"

rm -f /usr/share/tomee/conf
rm -f /usr/share/tomee/logs
rm -f /usr/share/tomee/temp
rm -f /usr/share/tomee/work
rm -f /usr/share/tomee/webapps

rm -Rf /var/lib/tomee/*
rm -Rf /var/log/tomee/*
