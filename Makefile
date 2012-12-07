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

# This file is used during development only.
# It has just some handy shortcuts.
TOMEE_VERSION=1.5.1
TOMCAT_ZIP_NAME=apache-tomcat-7.0.33
TOMCAT_ZIP=tomee/apache-tomcat/target/$(TOMCAT_ZIP_NAME)-SNAPSHOT.zip
TOMEEPLUS_WAR=tomee/tomee-plus-webapp/target/tomee-plus-webapp-$(TOMEE_VERSION).war
TOMEE_WAR=/home/tveronezi/dev/ws/openejb/trunk/tomee/tomee-webapp/target/tomee-webapp-$(TOMEE_VERSION).war
TOMEEPLUS_ZIP=tomee/apache-tomee/target/apache-tomee-plus-$(TOMEE_VERSION).tar.gz

$(TOMEE_WAR):
	cd tomee/tomee-webapp/ && mvn clean install -DskipTests=true	

$(TOMEEPLUS_WAR): $(TOMEE_WAR)
	cd tomee/tomee-plus-webapp/ && mvn clean install -DskipTests=true	

$(TOMCAT_ZIP):
	cd tomee/apache-tomcat && mvn clean install	

tomcat: $(TOMCAT_ZIP)

tomee: tomcat $(TOMEEPLUS_WAR)

kill-tomee: 
	@if test -f target/runnner/tomcat-pid.txt; then \
		kill -9 `cat target/runnner/tomcat-pid.txt`; \
		rm target/runnner/tomcat-pid.txt; \
	fi

start-plus: kill-tomee tomee
	mkdir -p target/runnner
	cp $(TOMEEPLUS_ZIP) target/runnner/
	rm -Rf target/runnner/apache-tomee-plus-$(TOMEE_VERSION) 
	cd target/runnner/ && tar -xvzf apache-tomee-plus-$(TOMEE_VERSION).tar.gz
	rm target/runnner/apache-tomee-plus-$(TOMEE_VERSION)/conf/tomcat-users.xml 	
	cp tomee/tomee-webapp/src/test/conf/tomcat-users.xml target/runnner/apache-tomee-plus-$(TOMEE_VERSION)/conf/
	export JPDA_SUSPEND=n && export CATALINA_PID=target/runnner/tomcat-pid.txt && ./target/runnner/apache-tomee-plus-$(TOMEE_VERSION)/bin/catalina.sh jpda start

start-tomee: kill-tomee tomee
	mkdir -p target/runnner
	cp $(TOMCAT_ZIP) target/runnner/
	rm -Rf target/runnner/$(TOMCAT_ZIP_NAME) 
	cd target/runnner/ && unzip $(TOMCAT_ZIP_NAME)-SNAPSHOT.zip
	cp tomee/tomee-plus-webapp/target/tomee-plus-webapp-$(TOMEE_VERSION).war \
		target/runnner/$(TOMCAT_ZIP_NAME)/webapps/
	rm target/runnner/apache-tomcat-7.0.33/conf/tomcat-users.xml 	
	cp tomee/tomee-webapp/src/test/conf/tomcat-users.xml target/runnner/apache-tomcat-7.0.33/conf/
	chmod +x target/runnner/$(TOMCAT_ZIP_NAME)/bin/startup.sh
	chmod +x target/runnner/apache-tomcat-7.0.33/bin/catalina.sh
	export JPDA_SUSPEND=n && export CATALINA_PID=target/runnner/tomcat-pid.txt && ./target/runnner/$(TOMCAT_ZIP_NAME)/bin/catalina.sh jpda start

reload-tomee: kill-tomee
	chmod +x target/runnner/$(TOMCAT_ZIP_NAME)/bin/startup.sh
	chmod +x target/runnner/apache-tomcat-7.0.33/bin/catalina.sh
	export CATALINA_PID=target/runnner/tomcat-pid.txt && ./target/runnner/$(TOMCAT_ZIP_NAME)/bin/catalina.sh jpda start

up-static:
	rm -Rf target/runnner/$(TOMCAT_ZIP_NAME)/webapps/tomee-plus-webapp-$(TOMEE_VERSION)/app
	cp -r tomee/tomee-webapp/src/main/webapp/app target/runnner/$(TOMCAT_ZIP_NAME)/webapps/tomee-plus-webapp-$(TOMEE_VERSION)/
	

install-tomee: $(TOMCAT_ZIP) 
	cd tomee && mvn clean install -DskipTests=true

install-all:
	mvn clean && mvn install -DskipTests=true

.PHONY: install-all install-tomee tomcat tomee start-tomee reload-tomee kill-tomee up-static
