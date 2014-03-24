#!/bin/sh

ln -sf /etc/tomee /opt/tomee/conf
ln -sf /var/log/tomee /opt/tomee/logs
ln -sf /var/tmp/tomee/temp /opt/tomee/temp
ln -sf /var/tmp/tomee/work /opt/tomee/work
ln -sf /var/lib/tomee/webapps /opt/tomee/webapps

groupadd apachetomee
useradd apachetomee -g apachetomee

chown -R apachetomee:apachetomee /opt/tomee/
chown -R apachetomee:apachetomee /var/log/tomee
chown -R apachetomee:apachetomee /var/tmp/tomee/temp
chown -R apachetomee:apachetomee /var/tmp/tomee/work
chown -R apachetomee:apachetomee /var/lib/tomee/webapps

update-rc.d tomee defaults
echo "Reboot your machine or run 'service tomee start' to start the Apache TomEE server"