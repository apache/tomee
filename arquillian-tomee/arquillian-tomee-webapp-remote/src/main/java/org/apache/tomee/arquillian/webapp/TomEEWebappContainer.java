/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.arquillian.webapp;

import org.apache.openejb.arquillian.common.Files;
import org.apache.openejb.arquillian.common.IO;
import org.apache.openejb.arquillian.common.Setup;
import org.apache.openejb.arquillian.common.TomEEContainer;
import org.apache.openejb.arquillian.common.Zips;
import org.apache.openejb.config.RemoteServer;
import org.apache.tomee.installer.Installer;
import org.apache.tomee.installer.Paths;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/*
 * TODO: delete old embedded adapter, move the tests and set those up
 */
public class TomEEWebappContainer extends TomEEContainer<TomEEWebappConfiguration> {

    private static final Logger logger = Logger.getLogger(TomEEWebappContainer.class.getName());

    private RemoteServer container;
    private boolean shutdown = false;

    public void start() throws LifecycleException {
        // see if TomEE is already running by checking the http port
        if (Setup.isRunning(configuration.getHttpPort())) {

            logger.info(String.format("Tomcat found running on port %s", configuration.getHttpPort()));

            return;
        }

        shutdown = true;

        final String s = File.separator;
        try {
            final File workingDirectory = new File(configuration.getDir());

            if (workingDirectory.exists()) {

                Files.assertDir(workingDirectory);

            } else {

                Files.mkdir(workingDirectory);
                Files.deleteOnExit(workingDirectory);
            }

            Files.readable(workingDirectory);
            Files.writable(workingDirectory);

            File openejbHome = Setup.findHome(workingDirectory);

            if (openejbHome == null) {

                downloadTomcat(workingDirectory, configuration.getTomcatVersion());

                openejbHome = Setup.findHome(workingDirectory);

                Files.deleteOnExit(openejbHome);

                final File webapp = new File(openejbHome, "webapps" + s + "tomee");

                Files.mkdir(webapp);
                downloadOpenEJBWebapp(webapp);

                System.setProperty("catalina.home", openejbHome.getAbsolutePath());
                System.setProperty("catalina.base", openejbHome.getAbsolutePath());

                System.setProperty("openejb.deploymentId.format", "{appId}/{ejbJarId}/{ejbName}");

                Paths paths = new Paths(webapp);
                Installer installer = new Installer(paths, true);
                installer.installAll();
            }

            Files.assertDir(openejbHome);
            Files.readable(openejbHome);
            Files.writable(openejbHome);

            Setup.updateServerXml(openejbHome, configuration.getHttpPort(), configuration.getStopPort(), 0);
            Setup.exportProperties(openejbHome, configuration);

            final URL logging = Thread.currentThread().getContextClassLoader().getResource("default.remote.logging.properties");
            if (logging != null) {
                write(logging, new File(openejbHome, "conf" + s + "logging.properties"));
            }

            if (configuration.isRemoveUnusedWebapps()) {
                Setup.removeUselessWebapps(openejbHome);
            }

            if (false) {
                Map<Object, Object> map = new TreeMap(System.getProperties());
                for (Map.Entry<Object, Object> entry : map.entrySet()) {
                    System.out.printf("%s = %s\n", entry.getKey(), entry.getValue());
                }
            }

            container = new RemoteServer();
            container.start();
        } catch (Exception e) {
            throw new LifecycleException("Unable to start remote container", e);
        }
    }

    private static void write(URL resource, File file) throws IOException {
        if (file.exists()) {
            Files.delete(file);
        }
        InputStream is = org.apache.openejb.loader.IO.read(resource);

        try {
            IO.copy(is, file);
        } finally {
            is.close();
        }
    }

    protected void downloadOpenEJBWebapp(File targetDirectory) throws LifecycleException {
        String artifactName = configuration.getArtifactName();
        File zipFile = Setup.downloadFile(artifactName, null);
        Zips.unzip(zipFile, targetDirectory);
    }

    protected void downloadTomcat(File catalinaDirectory, String tomcatVersion) throws LifecycleException {
        String source = null;

        if (tomcatVersion.startsWith("7.")) {
            source = "http://archive.apache.org/dist/tomcat/tomcat-7/v" + tomcatVersion + "/bin/apache-tomcat-" + tomcatVersion + ".zip";
        }

        if (tomcatVersion.startsWith("6.")) {
            source = "http://archive.apache.org/dist/tomcat/tomcat-6/v" + tomcatVersion + "/bin/apache-tomcat-" + tomcatVersion + ".zip";
        }

        if (tomcatVersion.startsWith("5.5")) {
            source = "http://archive.apache.org/dist/tomcat/tomcat-5/v" + tomcatVersion + "/bin/apache-tomcat-" + tomcatVersion + ".zip";
        }

        if (source == null) {
            throw new LifecycleException("Unable to find URL for Tomcat " + tomcatVersion);
        }

        File zipFile = Setup.downloadFile("org.apache.openejb:apache-tomcat:" + tomcatVersion + ":zip", source);
        Zips.unzip(zipFile, catalinaDirectory);
    }

    public void stop() throws LifecycleException {
        // only stop the container if we started it
        if (shutdown) {
            container.stop();
        }
    }

    public Class<TomEEWebappConfiguration> getConfigurationClass() {
        return TomEEWebappConfiguration.class;
    }
}
