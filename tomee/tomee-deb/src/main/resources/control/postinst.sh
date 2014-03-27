#!/bin/sh

ln -sf /etc/tomee/${classifier}/${tomeeVersion} /usr/share/tomee/${classifier}/${tomeeVersion}/conf
ln -sf /var/log/tomee/${classifier}/${tomeeVersion} /var/lib/tomee/${classifier}/${tomeeVersion}/logs

groupadd apachetomee
useradd --system apachetomee -g apachetomee

chown -R root:apachetomee /var/log/tomee/${classifier}/${tomeeVersion}
chown -R root:apachetomee /var/lib/tomee/${classifier}/${tomeeVersion}
chown -R root:apachetomee /etc/tomee/${classifier}/${tomeeVersion}
chmod -R g+w /var/log/tomee/${classifier}/${tomeeVersion}
chmod -R g+w /var/lib/tomee/${classifier}/${tomeeVersion}

update-rc.d tomee-${classifier} defaults

update-alternatives --install /etc/init.d/tomee tomee /etc/init.d/tomee-${classifier} ${priority}

echo "Reboot your machine or run 'service tomee-${classifier} start' to start the Apache TomEE ${classifier} server (version: ${tomeeVersion})"