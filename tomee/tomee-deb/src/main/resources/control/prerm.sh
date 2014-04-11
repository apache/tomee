#!/bin/sh -e

service tomee-${classifier} stop || true

# removing alternative
update-alternatives --remove tomee /etc/init.d/tomee-${classifier}

# removing link to /var/logs
rm -f /var/lib/tomee-${classifier}/logs

# removing non-configuration files
rm -Rf /var/lib/tomee-${classifier}/apps
rm -Rf /var/lib/tomee-${classifier}/conf
rm -Rf /var/lib/tomee-${classifier}/bin
rm -Rf /var/lib/tomee-${classifier}/temp
rm -Rf /var/lib/tomee-${classifier}/webapps
rm -Rf /var/lib/tomee-${classifier}/work

# removing files created by tomcat
rm -Rf /var/lib/tomee-${classifier}/conf/Catalina

# removing link to /etc
rm -f /usr/share/tomee-${classifier}/conf

# removing logs
rm -Rf /var/log/tomee-${classifier}/*
