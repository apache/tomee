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
import org.apache.openejb.webapp.common.Installers
import org.apache.openejb.webapp.common.Alerts
import java.io.File
import org.apache.openejb.tomcat.installer.Installer;
import org.apache.openejb.tomcat.installer.Paths;
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
		def tomcatVersion = require('tomcat.version')
		def localRepo = require('localRepository')
		def openejbHome = "${project.build.directory}/apache-tomcat-${tomcatVersion}"
		def examplesVersion = require('examples.version')

		def proxyHost = get('http.proxy.host', '')
		def proxyPort = get('http.proxy.port', '')
		def proxyUsername = get('http.proxy.username', '')
		def proxyPassword = get('http.proxy.password', '')
		def proxyNonProxyHosts = get('http.proxy.nonProxyHosts', '')

		if ((proxyHost != null && proxyHost.length() > 0) || (proxyPort != null && proxyPort.length() > 0)) {
			ant.echo("Setting proxy host=${proxyHost} and proxy port=${proxyPort}")
			
			def setProxy = new SetProxy();
			setProxy.setProxyHost(proxyHost)
			setProxy.setProxyPort(Integer.parseInt(proxyPort))
			setProxy.setProxyUser(proxyUsername)
			setProxy.setProxyPassword(proxyPassword)
			setProxy.setNonProxyHosts(proxyNonProxyHosts)
			setProxy.execute()
		}
		
		def source = ""
		def dest = "${project.build.directory}/apache-tomcat-${tomcatVersion}.zip"
		def catalinaHome = "${project.build.directory}/apache-tomcat-${tomcatVersion}"

		if (tomcatVersion =~ /^7\./) {
			source = "http://archive.apache.org/dist/tomcat/tomcat-7/v${tomcatVersion}/bin/apache-tomcat-${tomcatVersion}.zip"
		}

		if (tomcatVersion =~ /^6\./) {
			source = "http://archive.apache.org/dist/tomcat/tomcat-6/v${tomcatVersion}/bin/apache-tomcat-${tomcatVersion}.zip"
		}

		if (tomcatVersion =~ /^5\.5/) {
			source = "http://archive.apache.org/dist/tomcat/tomcat-5/v${tomcatVersion}/bin/apache-tomcat-${tomcatVersion}.zip"
		}

		ant.get(src: source, dest: dest)

		ant.unzip(src: dest, dest: "${project.build.directory}")

		ant.echo("Deploying the openejb war")
		ant.unzip(src: "${localRepo}/org/apache/openejb/openejb-tomcat-webapp/${project.version}/openejb-tomcat-webapp-${project.version}.war",
				dest: "${project.build.directory}/apache-tomcat-${tomcatVersion}/webapps/openejb")

		ant.echo("Installing to: ${catalinaHome}")

		System.setProperty("catalina.home", "${catalinaHome}")
		System.setProperty("catalina.base", "${catalinaHome}")
		Paths paths = new Paths(new File("${catalinaHome}/webapps/openejb"))
		Installer installer = new Installer(paths, true)
		installer.installAll()

		ant.echo("Assigning execute privileges to scripts in Tomcat bin directory")
		ant.chmod(dir: "${project.build.directory}/apache-tomcat-${tomcatVersion}/bin", perm: "u+x", includes: "**/*.sh")

		ant.echo("Deploying the examples war")
		ant.unzip(src: "${localRepo}/org/superbiz/ejb-examples/${examplesVersion}/ejb-examples-${examplesVersion}.war",
				dest: "${project.build.directory}/apache-tomcat-${tomcatVersion}/webapps/ejb-examples")
	}
}

