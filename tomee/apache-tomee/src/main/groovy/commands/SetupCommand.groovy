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

import org.apache.tomee.installer.Installer;
import org.apache.tomee.installer.Paths;
import org.apache.tools.ant.taskdefs.optional.net.SetProxy

class SetupCommand {
	def log
	def ant
	def project

	def SetupCommand(source) {
		this.log = source.log
		this.project = source.project
		this.ant = new AntBuilder()
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
		def workDir = require('tomee.workdir')
		def webapp = require('tomee.webapp')
		def tomcatVersion = require('tomcat.version')
		System.setProperty('tomcat.version', tomcatVersion)
		def openejbVersion = require('openejb.version')
		System.setProperty('openejb.version', openejbVersion)
		def localRepo = require('localRepository')

		def proxyHost = get('http.proxy.host', '')
		def proxyPort = get('http.proxy.port', '')
		def proxyUsername = get('http.proxy.username', '')
		def proxyPassword = get('http.proxy.password', '')
		def proxyNonProxyHosts = get('http.proxy.nonProxyHosts', '')

		if ((proxyHost != null && proxyHost.length() > 0) || (proxyPort != null && proxyPort.length() > 0)) {
			log.info("Setting proxy host=${proxyHost} and proxy port=${proxyPort}")
			
			def setProxy = new SetProxy();
			setProxy.setProxyHost(proxyHost)
			setProxy.setProxyPort(Integer.parseInt(proxyPort))
			setProxy.setProxyUser(proxyUsername)
			setProxy.setProxyPassword(proxyPassword)
			setProxy.setNonProxyHosts(proxyNonProxyHosts)
			setProxy.execute()
		}
		
		def dest = "${workDir}/apache-tomcat-${tomcatVersion}.zip"
		def catalinaHome = "${workDir}/apache-tomcat-${tomcatVersion}"

        log.info("extracting ${catalinaHome}")
		ant.unzip(src: dest, dest: "${workDir}")

		log.info("Deploying the tomee war")
		ant.unzip(src: "${localRepo}/org/apache/openejb/${webapp}/${openejbVersion}/${webapp}-${openejbVersion}.war",
				dest: "${workDir}/apache-tomcat-${tomcatVersion}/webapps/tomee")

		log.info("Installing to: ${catalinaHome}")

		System.setProperty("catalina.home", "${catalinaHome}")
		System.setProperty("catalina.base", "${catalinaHome}")
		Paths paths = new Paths(new File("${catalinaHome}/webapps/tomee"))
		Installer installer = new Installer(paths, true)
		installer.installAll()

		log.info("Assigning execute privileges to scripts in Tomcat bin directory")
		ant.chmod(dir: "${workDir}/apache-tomcat-${tomcatVersion}/bin", perm: "u+x", includes: "**/*.sh")

        ant.delete(dir: "${workDir}/apache-tomcat-${tomcatVersion}/webapps/examples")
        ant.delete(file: "${workDir}/apache-tomcat-${tomcatVersion}/webapps/tomee/META-INF/LICENSE")
        ant.delete(file: "${workDir}/apache-tomcat-${tomcatVersion}/webapps/tomee/META-INF/NOTICE")
	}
}

