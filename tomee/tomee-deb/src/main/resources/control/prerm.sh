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

# Unlinking tomcat jars
for tomcatJar in /usr/share/tomee-lib-tomcat/lib/*
do
    rm /usr/share/tomee-${classifier}/lib/\$(basename "\$tomcatJar")
done

# removing files created by tomcat
rm -Rf /var/lib/tomee-${classifier}/conf/Catalina

# removing link to /etc
rm -f /usr/share/tomee-${classifier}/conf

# removing logs
rm -Rf /var/log/tomee-${classifier}/*
