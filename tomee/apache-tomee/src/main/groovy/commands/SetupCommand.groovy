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

import org.apache.tomee.installer.Installer
import org.apache.tomee.installer.Paths
import org.apache.tools.ant.taskdefs.optional.net.SetProxy

class SetupCommand {

    def pom
    def log
    def project
    def ant
    def properties

    def SetupCommand(def pom, def log, def project, def ant, def properties) {
        this.pom = pom
        this.log = log
        this.project = project
        this.ant = ant
        this.properties = properties
    }

    def get(name) {
        assert name != null

        def value = properties.getProperty(name)

        if (null == value) value = project.properties.getProperty(name)
        if (null == value) value = System.properties.getProperty(name)

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

        if (!properties.containsKey(name) && !project.properties.containsKey(name) && !System.properties.containsKey(name)) {
            throw new Exception("Missing required property: $name" as String)
        }

        def value = get(name)

        if (value == 'null') {
            throw new Exception("Missing required property: $name (resolved to null)" as String)
        }

        return value
    }

    def execute() {
        def workDir = require('tomee.workdir')
        def webapp = require('tomee.webapp')
        String tomcatVersion = require('tomcat.version')
        System.setProperty('tomcat.version', tomcatVersion)

        String tomeeVersion = require('tomee.version')
        System.setProperty('tomee.version', tomeeVersion)

        String openejbVersion = require('openejb.version')
        System.setProperty('openejb.version', openejbVersion)
        def localRepo = pom.settings.localRepository

        def proxyHost = get('http.proxy.host', '')
        String proxyPort = get('http.proxy.port', '')
        def proxyUsername = get('http.proxy.username', '')
        def proxyPassword = get('http.proxy.password', '')
        def proxyNonProxyHosts = get('http.proxy.nonProxyHosts', '')

        if ((!'${settings.activeProxy.host}'.equals(proxyHost) && proxyHost != null && proxyHost.length() > 0)
                || (!'${settings.activeProxy.port}'.equals(proxyPort) && proxyPort != null && proxyPort.length() > 0)) {
            log.info("Setting proxy host=${proxyHost} and proxy port=${proxyPort}")

            def setProxy = new SetProxy();
            setProxy.setProxyHost(proxyHost as String)
            setProxy.setProxyPort(Integer.parseInt(proxyPort) as int)
            setProxy.setProxyUser(proxyUsername as String)
            setProxy.setProxyPassword(proxyPassword as String)
            setProxy.setNonProxyHosts(proxyNonProxyHosts as String)
            setProxy.execute()
        }

        def dest = "${workDir}/apache-tomcat-${tomcatVersion}.zip" as String
        def catalinaHome = "${workDir}/apache-tomcat-${tomcatVersion}" as String

        log.info("extracting ${catalinaHome}")
        ant.unzip(src: dest, dest: "${workDir}")

        log.info("Deploying the tomee war")
        ant.unzip(src: "${localRepo}/org/apache/openejb/${webapp}/${tomeeVersion}/${webapp}-${tomeeVersion}.war",
                dest: "${workDir}/apache-tomcat-${tomcatVersion}/webapps/tomee")

        log.info("Installing to: ${catalinaHome}")

        System.setProperty('catalina.home', "${catalinaHome}" as String)
        System.setProperty('catalina.base', "${catalinaHome}" as String)
        Paths paths = new Paths(new File("${catalinaHome}/webapps/tomee" as String))
        Installer installer = new Installer(paths, true)
        installer.installFull()

        // clean up duplicate jars since in TomEE it is useless
        // = gain of space ;)
        ant.delete(file: paths.getJAXBImpl())
        ant.delete(file: paths.getOpenEJBTomcatLoaderJar())
        ant.delete(file: paths.findTomEELibJar("jaxb-impl"))
        ant.delete(file: paths.findTomEELibJar("openejb-javaagent-${openejbVersion}.jar" as String))
        // we need the one without version

        ant.delete(file: "${paths.catalinaLibDir}/static-tomee-jquery-${tomeeVersion}.jar")
        ant.delete(file: "${paths.catalinaLibDir}/static-tomee-bootstrap-${tomeeVersion}.jar")

        ant.delete(file: paths.findOpenEJBWebJar("tomee-loader"))
        ant.delete(file: paths.findOpenEJBWebJar("swizzle-stream"))

        log.info("Assigning execute privileges to scripts in Tomcat bin directory")
        ant.chmod(dir: "${workDir}/apache-tomcat-${tomcatVersion}/bin", perm: "u+x", includes: "**/*.sh")

        ant.delete(dir: "${workDir}/apache-tomcat-${tomcatVersion}/webapps/examples")
        ant.delete(file: "${workDir}/apache-tomcat-${tomcatVersion}/webapps/tomee/META-INF/LICENSE")
        ant.delete(file: "${workDir}/apache-tomcat-${tomcatVersion}/webapps/tomee/META-INF/NOTICE")
    }
}

