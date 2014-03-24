#!/bin/sh

kill -9 $(ps aux | grep org.apache.catalina.startup.Bootstrap | grep -v grep | awk '{print $2}')

rm -f /opt/tomee/conf
rm -f /opt/tomee/logs
rm -f /opt/tomee/temp
rm -f /opt/tomee/work
rm -f /opt/tomee/webapps

update-rc.d -f tomee remove