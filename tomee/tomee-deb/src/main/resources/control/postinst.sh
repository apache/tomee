#!/bin/sh

ln -sf /etc/tomee/${tomeeVersion} /usr/share/tomee/${tomeeVersion}/conf
ln -sf /var/log/tomee/${tomeeVersion} /usr/share/tomee/${tomeeVersion}/logs
ln -sf /var/lib/tomee/${tomeeVersion}/temp /usr/share/tomee/${tomeeVersion}/temp
ln -sf /var/lib/tomee/${tomeeVersion}/work /usr/share/tomee/${tomeeVersion}/work
ln -sf /var/lib/tomee/${tomeeVersion}/webapps /usr/share/tomee/${tomeeVersion}/webapps

groupadd apachetomee
useradd apachetomee -g apachetomee

chown -R root:apachetomee /var/log/tomee/${tomeeVersion}
chown -R root:apachetomee /var/lib/tomee/${tomeeVersion}
chmod -R g+w /var/log/tomee/${tomeeVersion}
chmod -R g+w /var/lib/tomee/${tomeeVersion}

update-rc.d tomee defaults
echo "Reboot your machine or run 'service tomee start' to start the Apache TomEE server (version: ${tomeeVersion})"