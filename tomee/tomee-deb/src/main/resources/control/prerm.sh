#!/bin/sh -e

service tomee-${classifier} stop || true

# removing alternative
update-alternatives --remove tomee /etc/init.d/tomee-${classifier}

# removing link to /var/logs
rm -f /var/lib/tomee-${classifier}-${tomeeVersion}/logs

# removing non-configuration files
rm -Rf /var/lib/tomee-${classifier}-${tomeeVersion}/apps
rm -Rf /var/lib/tomee-${classifier}-${tomeeVersion}/bin
rm -Rf /var/lib/tomee-${classifier}-${tomeeVersion}/temp
rm -Rf /var/lib/tomee-${classifier}-${tomeeVersion}/webapps
rm -Rf /var/lib/tomee-${classifier}-${tomeeVersion}/work

# removing files created by tomcat
rm -Rf /var/lib/tomee-${classifier}-${tomeeVersion}/conf/Catalina

# removing link to /etc
rm -f /usr/share/tomee-${classifier}-${tomeeVersion}/conf

# removing logs
rm -Rf /var/log/tomee-${classifier}-${tomeeVersion}/*
