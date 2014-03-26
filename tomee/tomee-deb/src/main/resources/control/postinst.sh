#!/bin/sh

ln -sf /etc/tomee/${classifier}/${tomeeVersion} /usr/share/tomee/${classifier}/${tomeeVersion}/conf
ln -sf /var/log/tomee/${classifier}/${tomeeVersion} /usr/share/tomee/${classifier}/${tomeeVersion}/logs
ln -sf /var/lib/tomee/${classifier}/${tomeeVersion}/temp /usr/share/tomee/${classifier}/${tomeeVersion}/temp
ln -sf /var/lib/tomee/${classifier}/${tomeeVersion}/work /usr/share/tomee/${classifier}/${tomeeVersion}/work
ln -sf /var/lib/tomee/${classifier}/${tomeeVersion}/webapps /usr/share/tomee/${classifier}/${tomeeVersion}/webapps

ln -sf /etc/init.d/tomee-${classifier} /etc/init.d/tomee

groupadd apachetomee
useradd apachetomee -g apachetomee

chown -R root:apachetomee /var/log/tomee/${classifier}/${tomeeVersion}
chown -R root:apachetomee /var/lib/tomee/${classifier}/${tomeeVersion}
chown -R root:apachetomee /etc/tomee/${classifier}/${tomeeVersion}
chmod -R g+w /var/log/tomee/${classifier}/${tomeeVersion}
chmod -R g+w /var/lib/tomee/${classifier}/${tomeeVersion}
chmod -R o-rwx /etc/tomee/${classifier}/${tomeeVersion}
chmod -R g+w /etc/tomee/${classifier}/${tomeeVersion}

update-rc.d tomee-${classifier} defaults
echo "Reboot your machine or run 'service tomee-${classifier} start' to start the Apache TomEE ${classifier} server (version: ${tomeeVersion})"