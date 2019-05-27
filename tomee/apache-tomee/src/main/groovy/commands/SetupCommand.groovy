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
    AntBuilder ant
    def properties

    def require(String name) {
        assert name != null

        def value = properties.getProperty(name)
        if (!value || value == 'null') value = project.properties.getProperty(name)
        if (!value || value == 'null') value = System.properties.getProperty(name)
        if (value == 'null') value = null
        log.debug("Require property $name = $value")

        if (!value) {
            throw new Exception("Missing required property: $name (resolved to null)" as String)
        }
        return value
    }

    def deleteWithRetry = {
        try {
            ant.delete(it)
        } catch (e) {
            System.gc()
            log.warn("RETRY Deleting: ${it}")
            ant.delete(it)
        }
    }

    def execute() {
        String tomcatVersion = require('tomcat.version')
        System.setProperty('tomcat.version', tomcatVersion)

        String tomeeVersion = require('tomee.version')
        System.setProperty('tomee.version', tomeeVersion)

        String proxyHost = pom.settings.activeProxy?.host ?: ''
        String proxyPort = pom.settings.activeProxy?.port ?: ''
        if (proxyHost && proxyPort) {
            log.info("Setting proxy host=${proxyHost} and proxy port=${proxyPort}")
            new SetProxy(
                    proxyHost: proxyHost,
                    proxyPort: proxyPort as int,
                    proxyUser: pom.settings.activeProxy.username ?: '',
                    proxyPassword: pom.settings.activeProxy.password ?: '',
                    nonProxyHosts: pom.settings.activeProxy.nonProxyHosts ?: ''
            ).execute()
        }

        def workDir = require('tomee.workdir')
        def webapp = require('tomee.webapp')

        def dest = "${workDir}/apache-tomcat-${tomcatVersion}.zip" as String
        def catalinaHome = "${workDir}/apache-tomcat-${tomcatVersion}" as String
        System.setProperty('catalina.home', catalinaHome)
        System.setProperty('catalina.base', catalinaHome)

        log.info("extracting ${catalinaHome}")
        ant.unzip(src: dest, dest: "${workDir}")

        log.info('Deploying the tomee war')
        def localRepo = pom.settings.localRepository
        ant.unzip(src: "${localRepo}/org/apache/tomee/${webapp}/${tomeeVersion}/${webapp}-${tomeeVersion}.war",
                dest: "${workDir}/apache-tomcat-${tomcatVersion}/webapps/tomee")

        log.info("Installing to: ${catalinaHome}")


        Paths paths = new Paths(new File("${catalinaHome}/webapps/tomee" as String))
        Installer installer = new Installer(paths, properties, true)
        installer.installFull()

        // clean up duplicate jars since in TomEE it is useless
        // = gain of space ;)
        deleteWithRetry(file: paths.getOpenEJBTomcatLoaderJar())
        deleteWithRetry(file: paths.findTomEELibJar("openejb-javaagent-${tomeeVersion}.jar" as String))
        // we need the one without version

        deleteWithRetry(file: paths.findOpenEJBWebJar('tomee-loader'))
        deleteWithRetry(file: paths.findOpenEJBWebJar('swizzle-stream'))

        log.info('Assigning execute privileges to scripts in Tomcat bin directory')
        ant.chmod(dir: "${workDir}/apache-tomcat-${tomcatVersion}/bin", perm: 'u+x', includes: '**/*.sh')

        deleteWithRetry(dir: "${workDir}/apache-tomcat-${tomcatVersion}/webapps/examples")
        deleteWithRetry(dir: "${workDir}/apache-tomcat-${tomcatVersion}/webapps/tomee")
    }
}

