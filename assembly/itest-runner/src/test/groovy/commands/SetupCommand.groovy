/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package commands

import org.apache.commons.lang.SystemUtils

class SetupCommand
{
	def log;
	def ant;
	def project;
	
    def SetupCommand(source) {
		this.log = source.log;
		this.project = source.project
		this.ant = new AntBuilder();
    }
	
	def get(name) {
		assert name != null

		def value = project.properties.getProperty(name)

		log.debug("Get property: $name=$value")

		return value
	}

	def get(name, defaultValue) {
		def value = get(name)

		if (value == null) {
			value = defaultValue
		}

		return value
	}
	
	def getBoolean(name, defaultValue) {
		def value = get(name, defaultValue)
		return Boolean.valueOf("$value")
	}
    
	def require(name) {
		assert name != null

		log.debug("Require property: $name")

		if (!project.properties.containsKey(name) && !System.properties.containsKey(name)) {
			throw new Exception("Missing required property: $name")
		}

		def value = get(name)

		if (value == 'null') {
			throw new Exception("Missing required property: $name (resolved to null)")
		}

		return value
	}
	
    def execute() {
		def tomcatVersion = require('tomcat.version');
		def localRepo = require('localRepository');
		def openejbHome = require('openejb.home');
		
		if (getBoolean('skipTests', false)) {
			log.info('Skipping itests.')
			return
		}
		
		ant.echo("Removing ejb-example application from exploded bundle")
		ant.delete(dir: "${project.build.directory}/apache-tomcat-${tomcatVersion}/webapps/ejb-examples")

		ant.echo("Assigning execute privileges to scripts in Tomcat bin directory")
		ant.chmod(dir: "${project.build.directory}/apache-tomcat-${tomcatVersion}/bin", perm: "u+x", includes: "**/*.sh")
		
		ant.echo("Deploying the itests war")
		ant.unzip(src: "${localRepo}/org/apache/openejb/openejb-itests-web/${project.version}/openejb-itests-web-${project.version}.war",
					dest: "${project.build.directory}/apache-tomcat-${tomcatVersion}/webapps/itests")
		
		ant.echo("Starting Tomcat...")
		ant.exec(executable: "${project.build.directory}/apache-tomcat-${tomcatVersion}/bin/startup.sh")
		
		ant.waitfor(maxwait: 1, maxwaitunit: "minute") {
			ant.and() {
				ant.socket(server: "localhost", port: "8080")
				ant.socket(server: "localhost", port: "8005")
				ant.socket(server: "localhost", port: "8009")
			}
		}
		
		ant.echo("Tomcat started. Running itests...")
		ant.java(jar: "${localRepo}/org/apache/openejb/openejb-itests-standalone-client/${project.version}/openejb-itests-standalone-client-${project.version}.jar", fork: "yes") {
			sysproperty(key: "openejb.home", value: "${openejbHome}")
			arg(value: "tomcat")
		}

		ant.echo("Tomcat itests complete, stopping Tomcat")
		ant.exec(executable: "${project.build.directory}/apache-tomcat-${tomcatVersion}/bin/shutdown.sh")
		ant.waitfor(maxwait: 1, maxwaitunit: "minute") {
			ant.not() {
				ant.or() {
					ant.socket(server: "localhost", port: "8080")
					ant.socket(server: "localhost", port: "8005")
					ant.socket(server: "localhost", port: "8009")
					ant.socket(server: "localhost", port: "61616")
				}
			}
		}
		
		ant.echo("Tomcat stopped, itest run complete")
    }
}
