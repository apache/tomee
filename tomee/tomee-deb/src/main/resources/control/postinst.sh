#!/bin/bash -e

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

ln -sf /etc/tomee-${classifier} /usr/share/tomee-${classifier}/conf
ln -sf /var/log/tomee-${classifier} /var/lib/tomee-${classifier}/logs

# Creating links from catalina_base to the configuration files in catalina_home
mkdir -p /var/lib/tomee-${classifier}/conf
ln -sf /etc/tomee-${classifier}/server.xml /var/lib/tomee-${classifier}/conf/server.xml
ln -sf /etc/tomee-${classifier}/tomcat-users.xml /var/lib/tomee-${classifier}/conf/tomcat-users.xml
ln -sf /etc/tomee-${classifier}/openejb.conf /var/lib/tomee-${classifier}/conf/openejb.conf
ln -sf /etc/tomee-${classifier}/tomee.xml /var/lib/tomee-${classifier}/conf/tomee.xml
ln -sf /etc/tomee-${classifier}/conf.d /var/lib/tomee-${classifier}/conf/conf.d

# Creating user apps directory. We dont want to erase it during an upgrade.
if [ ! -d /var/lib/tomee-${classifier}-user-data ] ; then
    mkdir -p /var/lib/tomee-${classifier}-user-data/temp
	mkdir -p /var/lib/tomee-${classifier}-user-data/webapps
fi
ln -sf /var/lib/tomee-${classifier}-user-data/temp /var/lib/tomee-${classifier}/temp
ln -sf /var/lib/tomee-${classifier}-user-data/webapps /var/lib/tomee-${classifier}/webapps

groupadd apachetomee >/dev/null 2>&1 || true
useradd --system apachetomee -g apachetomee >/dev/null 2>&1 || true

# common jars links
mkdir -p /usr/share/tomee-${classifier}/lib
${libLinks}

mkdir -p /var/log/tomee-${classifier}

chown -R root:apachetomee /var/log/tomee-${classifier}
chown -R root:apachetomee /var/lib/tomee-${classifier}
chown -R root:apachetomee /etc/tomee-${classifier}
chown -R root:apachetomee /var/lib/tomee-${classifier}-user-data

# users from the apachetomee group should be able to change settings.
# there is no need to be root.
chmod -R g+w /etc/tomee-${classifier}

chmod -R g+w /var/log/tomee-${classifier}
chmod -R g+w /var/lib/tomee-${classifier}
chmod -R g+w /var/lib/tomee-${classifier}-user-data

update-rc.d tomee-${classifier} defaults >/dev/null 2>&1

update-alternatives --install /etc/init.d/tomee tomee /etc/init.d/tomee-${classifier} ${priority} >/dev/null 2>&1

echo "Apache TomEE classifier ${tomeeVersion} installed."
echo "Reboot your machine or run 'service tomee-${classifier} start' to start TomEE."

