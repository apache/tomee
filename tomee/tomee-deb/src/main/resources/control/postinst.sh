#!/bin/sh

ln -sf /etc/tomee /usr/share/tomee/conf
ln -sf /var/log/tomee /usr/share/tomee/logs
ln -sf /var/lib/tomee/temp /usr/share/tomee/temp
ln -sf /var/lib/tomee/work /usr/share/tomee/work
ln -sf /var/lib/tomee/webapps /usr/share/tomee/webapps

groupadd apachetomee
useradd apachetomee -g apachetomee

chown -R apachetomee:apachetomee /usr/share/tomee
chown -R apachetomee:apachetomee /var/log/tomee
chown -R apachetomee:apachetomee /var/lib/tomee

update-rc.d tomee defaults
echo "Reboot your machine or run 'service tomee start' to start the Apache TomEE server"