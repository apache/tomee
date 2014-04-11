#!/bin/sh -e

ln -sf /etc/tomee-${classifier} /usr/share/tomee-${classifier}/conf
ln -sf /var/log/tomee-${classifier} /var/lib/tomee-${classifier}/logs

# Creating links from catalina_base to the configuration files in catalina_home
ln -sf /etc/tomee-${classifier}/server.xml /var/lib/tomee-${classifier}/conf/server.xml
ln -sf /etc/tomee-${classifier}/tomcat-users.xml /var/lib/tomee-${classifier}/conf/tomcat-users.xml
ln -sf /etc/tomee-${classifier}/openejb.conf /var/lib/tomee-${classifier}/conf/openejb.conf
ln -sf /etc/tomee-${classifier}/conf.d /var/lib/tomee-${classifier}/conf/conf.d

groupadd apachetomee || true
useradd --system apachetomee -g apachetomee || true

chown -R root:apachetomee /var/log/tomee-${classifier}
chown -R root:apachetomee /var/lib/tomee-${classifier}
chown -R root:apachetomee /etc/tomee-${classifier}

# users from the apachetomee group should be able to change settings.
# there is no need to be root.
chmod -R g+w /etc/tomee-${classifier}

chmod -R g+w /var/log/tomee-${classifier}
chmod -R g+w /var/lib/tomee-${classifier}

update-rc.d tomee-${classifier} defaults

update-alternatives --install /etc/init.d/tomee tomee /etc/init.d/tomee-${classifier} ${priority}

echo "Reboot your machine or run 'service tomee-${classifier} start' to start the Apache TomEE ${classifier} server (version: ${tomeeVersion})"