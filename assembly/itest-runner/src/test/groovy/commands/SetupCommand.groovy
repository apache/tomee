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

  def isWindows() {
    def os = System.getProperty("os.name").toLowerCase()
    return (os.indexOf("win") >= 0)
  }

  def execute() {
    execute("6.0.29")
    execute("7.0.6")
  }

  def execute(tomcatVersion) {
    def localRepo = require('localRepository')
    def openejbHome = require('openejb.home')
    def extension = ".sh"
    def stopPort = get('tomcat.port.stop', '18005')
    def httpPort = get('tomcat.port.http', '18080')
    def ajpPort = get('tomcat.port.ajp', '18009')

    if (isWindows()) {
      extension = ".bat"
    }

    if (getBoolean('skipTests', false)) {
      log.info('Skipping itests.')
      return
    }

    def source = ""
    def dest = "${project.build.directory}/apache-tomcat-${tomcatVersion}.zip"
    def catalinaHome = "${project.build.directory}/apache-tomcat-${tomcatVersion}"

    if (tomcatVersion != "testonly") {
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

      ant.echo("Deploying the itests war")
      ant.unzip(src: "${localRepo}/org/apache/openejb/openejb-itests-web/${project.version}/openejb-itests-web-${project.version}.war",
              dest: "${project.build.directory}/apache-tomcat-${tomcatVersion}/webapps/itests")

      def alerts = new Alerts()
      def file = new File("${project.build.directory}/apache-tomcat-${tomcatVersion}/conf/server.xml")
      def fileContent = Installers.readAll(file, alerts)
      fileContent = fileContent.replaceAll("Server port=\"8005\"", "Server port=\"" + stopPort + "\"");
      fileContent = fileContent.replaceAll("Connector port=\"8080\"", "Connector port=\"" + httpPort + "\"");
      fileContent = fileContent.replaceAll("Connector port=\"8009\"", "Connector port=\"" + ajpPort + "\"");
      Installers.writeAll(file, fileContent, alerts)

      ant.exec(executable: "${project.build.directory}/apache-tomcat-${tomcatVersion}/bin/startup${extension}",
              spawn: true) {
        env(key: "CATALINA_HOME", value: "${catalinaHome}")
        env(key: "CATALINA_BASE", value: "${catalinaHome}")
        //env(key: "JPDA_SUSPEND", value: "y")
        //arg(line: "jpda start")
        arg(line: "start")
      }

      ant.waitfor(maxwait: 1, maxwaitunit: "minute") {
        ant.and() {
          ant.socket(server: "localhost", port: httpPort)
          ant.socket(server: "localhost", port: stopPort)
          ant.socket(server: "localhost", port: ajpPort)
        }
      }

      ant.echo("Tomcat started. Running itests...")
    }

    ant.java(jar: "${localRepo}/org/apache/openejb/openejb-itests-standalone-client/${project.version}/openejb-itests-standalone-client-${project.version}.jar", fork: "yes") {
      sysproperty(key: "openejb.home", value: "${openejbHome}")
      sysproperty(key: "openejb.server.uri", value: "http://127.0.0.1:" + httpPort + "/openejb/ejb")

      //sysproperty(key:"DEBUG", value:"true")
      //jvmarg(value:"-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8888")
      arg(value: "tomcat")
    }

    if (tomcatVersion != "testonly") {
      ant.echo("Tomcat itests complete, stopping Tomcat...")
      ant.exec(executable: "${project.build.directory}/apache-tomcat-${tomcatVersion}/bin/shutdown${extension}",
              spawn: true) {
        env(key: "CATALINA_HOME", value: "${catalinaHome}")
        env(key: "CATALINA_BASE", value: "${catalinaHome}")
      }

      ant.waitfor(maxwait: 1, maxwaitunit: "minute") {
        ant.not() {
          ant.or() {
            ant.socket(server: "localhost", port: httpPort)
            ant.socket(server: "localhost", port: stopPort)
            ant.socket(server: "localhost", port: ajpPort)
            ant.socket(server: "localhost", port: "61616")
          }
        }
      }
      ant.echo("Tomcat stopped")
    }
    ant.echo("itest run complete")
  }
}

