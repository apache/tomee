#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

HOME_DIR=$(shell cd && pwd)
TOMEE_DIR=$(HOME_DIR)/TOMEE
PROJECT_NAME=webaccess
TOMEE_ORIGINAL_DIR_NAME=apache-tomee-plus-1.6.0-SNAPSHOT

up-static:
	rm -rf $(TOMEE_DIR)/tomee-runtime/webapps/$(PROJECT_NAME)/app
	cp -r $(PROJECT_NAME)-gui/src/main/webapp/app $(TOMEE_DIR)/tomee-runtime/webapps/$(PROJECT_NAME)/
	cp -r $(PROJECT_NAME)-gui/src/main/webapp/index.jsp $(TOMEE_DIR)/tomee-runtime/webapps/$(PROJECT_NAME)/index.jsp

up-war: kill-tomee clean-install
	rm -f $(TOMEE_DIR)/tomee-runtime/webapps/$(PROJECT_NAME).war
	rm -Rf $(TOMEE_DIR)/tomee-runtime/webapps/$(PROJECT_NAME)
	cp ./$(PROJECT_NAME)-gui/target/$(PROJECT_NAME).war $(TOMEE_DIR)/tomee-runtime/webapps/

up-war-restart: up-war restart-tomee

clean-start: start-tomee up-war

clean-install: kill-tomee
	mvn clean install -DskipTests=true

unzip-tomee: kill-tomee clean-install
	cd ./$(PROJECT_NAME)-resources/target && \
	rm -Rf tomee-runtime && \
	tar -xzf tomee-runtime.tar.gz && \
	mv $(TOMEE_ORIGINAL_DIR_NAME) tomee-runtime
	mkdir -p $(TOMEE_DIR)
	rm -Rf $(TOMEE_DIR)/tomee-runtime
	mv ./$(PROJECT_NAME)-resources/target/tomee-runtime $(TOMEE_DIR)/
	cp ./$(PROJECT_NAME)-gui/target/$(PROJECT_NAME).war $(TOMEE_DIR)/tomee-runtime/webapps

kill-tomee:
	@if test -f $(HOME_DIR)/tomee-pid.txt; then \
		kill -9 `cat $(HOME_DIR)/tomee-pid.txt`; \
		rm $(HOME_DIR)/tomee-pid.txt; \
	fi

shutdown-tomee:
	cd $(TOMEE_DIR)/tomee-runtime/ && \
	./bin/shutdown.sh

start-tomee: unzip-tomee restart-tomee

tail:
	tail -f $(TOMEE_DIR)/tomee-runtime/logs/catalina.out

restart-tomee: kill-tomee
	cp -r ./src/main/config/tomcat-users.xml $(TOMEE_DIR)/tomee-runtime/conf
	cd $(TOMEE_DIR)/tomee-runtime/ && \
	export JPDA_SUSPEND=n && \
	export CATALINA_PID=$(HOME_DIR)/tomee-pid.txt && \
	./bin/catalina.sh jpda start

run-jasmine:
	cd ./$(PROJECT_NAME)-gui/ && mvn jasmine:bdd

run-lint:
	cd ./$(PROJECT_NAME)-gui/ && mvn jslint4java:lint

.PHONY: up-war up-war-restart up-static clean-start clean-install unzip-tomee shutdown-tomee kill-tomee start-tomee \
		restart-tomee run-jasmine run-lint tail

