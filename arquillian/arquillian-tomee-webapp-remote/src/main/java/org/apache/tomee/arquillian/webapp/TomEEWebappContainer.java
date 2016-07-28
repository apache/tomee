/*
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

import org.apache.openejb.arquillian.common.ArquillianFilterRunner;
import org.apache.openejb.arquillian.common.Files;
import org.apache.openejb.arquillian.common.IO;
import org.apache.openejb.arquillian.common.Setup;
import org.apache.openejb.arquillian.common.TomEEContainer;
import org.apache.openejb.arquillian.common.Zips;
import org.apache.openejb.config.RemoteServer;
import org.apache.tomee.installer.Installer;
import org.apache.tomee.installer.Paths;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.protocol.servlet.ServletMethodExecutor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TomEEWebappContainer extends TomEEContainer<TomEEWebappConfiguration> {

    private static final Logger logger = Logger.getLogger(TomEEWebappContainer.class.getName());

    private RemoteServer container;
    private boolean shutdown;
    private File openejbHome;
    private boolean wereOpenejbHomeSet = true;

    @Override
    protected String providerUrl() {
        return String.format(configuration.getProviderUrlPattern(), super.providerUrl());
    }

    @Override
    public void start() throws LifecycleException {
        // see if TomEE is already running by checking the http port
        final int httpPort = configuration.getHttpPort();
        if (Setup.isRunning(configuration.getHost(), httpPort)) {

            logger.info(String.format("Tomcat found running on port %s", httpPort));

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

            openejbHome = Setup.findHome(workingDirectory);
            Installer installer = null;

            if (openejbHome == null) {

                downloadTomcat(workingDirectory, configuration.getTomcatVersion(), configuration.getDir());

                openejbHome = Setup.findHome(workingDirectory);

                Files.deleteOnExit(openejbHome);

                final File webapp = new File(openejbHome, "webapps" + s + "tomee");

                Files.mkdir(webapp);
                downloadOpenEJBWebapp(webapp, configuration.getDir());

                System.setProperty("catalina.home", openejbHome.getAbsolutePath());
                System.setProperty("catalina.base", openejbHome.getAbsolutePath());

                System.setProperty("openejb.deploymentId.format", System.getProperty("openejb.deploymentId.format", "{appId}/{ejbJarId}/{ejbName}"));

                final Paths paths = new Paths(webapp);
                installer = new Installer(paths, true);
                if (!configuration.isUseInstallerServlet()) {
                    installer.installAll();

                }

                wereOpenejbHomeSet = false;
            }

            Files.assertDir(openejbHome);
            Files.readable(openejbHome);
            Files.writable(openejbHome);

            Setup.configureServerXml(openejbHome, configuration);
            Setup.configureSystemProperties(openejbHome, configuration);

            Setup.exportProperties(openejbHome, configuration, true);

            final URL logging = Thread.currentThread().getContextClassLoader().getResource("default.remote.logging.properties");
            if (logging != null) {
                write(logging, new File(openejbHome, "conf" + s + "logging.properties"));
            }

            if (configuration.isRemoveUnusedWebapps()) {
                Setup.removeUselessWebapps(openejbHome, "tomee");
            }

            if (logger.isLoggable(Level.FINE)) {
                final Map<Object, Object> map = new TreeMap<>(System.getProperties());
                for (final Map.Entry<Object, Object> entry : map.entrySet()) {
                    System.out.printf("%s = %s\n", entry.getKey(), entry.getValue());
                }
            }

            Setup.installArquillianBeanDiscoverer(openejbHome);

            if (!wereOpenejbHomeSet && configuration.isUseInstallerServlet()) {
                // instead of calling the Installer, let's just do like users do
                // call the servlet installer instead
                final String baseUrl = "http://" + configuration.getHost() + ":" + httpPort + "/tomee/installer";

                assert installer != null;
                installer.addTomEEAdminConfInTomcatUsers(true);

                final RemoteServer tmpContainer = new RemoteServer();
                tmpContainer.setPortStartup(httpPort);

                try {
                    tmpContainer.start();
                } catch (final Exception e) {
                    tmpContainer.destroy();
                    throw e;
                }

                final URL url = new URL(baseUrl);
                logger.info("Calling TomEE Installer Servlet on " + url);

                for (int i = 0; i < Integer.getInteger("tomee.webapp.container.client.retries", 3); i++) {
                    final URLConnection uc = url.openConnection();
                    // dG9tZWU6dG9tZWU= --> Base64 of tomee:tomee
                    final String authorizationString = "Basic dG9tZWU6dG9tZWU=";
                    final int timeout = Integer.getInteger("tomee.webapp.container.client.timeout", 60000);
                    uc.setConnectTimeout(timeout);
                    uc.setReadTimeout(timeout);
                    uc.setRequestProperty("Authorization", authorizationString);
                    try {
                        final InputStream is = uc.getInputStream();
                        org.apache.openejb.loader.IO.slurp(is);
                        is.close();
                        break;
                    } catch (final Exception e) {
                        logger.warning(e.getMessage());
                        Thread.sleep(1000);
                    }
                }

                tmpContainer.stop();
                tmpContainer.getServer().waitFor();
            }

            container = new RemoteServer();
            container.setPortStartup(httpPort);
            container.start(Arrays.asList(
                    "-Dopenejb.system.apps=true",
                    "-Dtomee.remote.support=true",
                    "-Dorg.apache.openejb.servlet.filters=" + ArquillianFilterRunner.class.getName() + "=" + ServletMethodExecutor.ARQUILLIAN_SERVLET_MAPPING), "start", true);
            container.killOnExit();
        } catch (final Exception e) {
            if (null != container) {
                container.destroy();
            }
            throw new LifecycleException("Unable to start remote container on port: " + httpPort, e);
        }
    }

    private static void write(final URL resource, final File file) throws IOException {
        if (file.exists()) {
            Files.delete(file);
        }

        try (final InputStream is = org.apache.openejb.loader.IO.read(resource)) {
            IO.copy(is, file);
        }
    }

    protected void downloadOpenEJBWebapp(final File targetDirectory, final String defaultTempDir) throws LifecycleException {
        final String artifactName = configuration.getArtifactName();
        final File zipFile = Setup.downloadFile(artifactName, null, defaultTempDir);
        Zips.unzip(zipFile, targetDirectory);
    }

    protected void downloadTomcat(final File catalinaDirectory, final String tomcatVersion, final String defaultTempDir) throws LifecycleException {
        String source = null;

        try {
            int v = Integer.parseInt(tomcatVersion.substring(0, tomcatVersion.indexOf('.')));
            source = "http://archive.apache.org/dist/tomcat/tomcat-" + v + "/v" + v + "/bin/apache-tomcat-" + tomcatVersion + ".zip";
        } catch (final Exception e) {
            // no-op
        }

        if (source == null) {
            throw new LifecycleException("Unable to find URL for Tomcat " + tomcatVersion);
        }

        final File zipFile = Setup.downloadFile("org.apache.tomcat:tomcat:" + tomcatVersion + ":zip", source, defaultTempDir);
        Zips.unzip(zipFile, catalinaDirectory);
    }

    @Override
    public void stop() throws LifecycleException {
        // only stop the container if we started it
        if (shutdown) {
            try {
                Setup.removeArquillianBeanDiscoverer(openejbHome);
                container.destroy();
            } finally {
                resetSerialization();
            }
        }
    }

    @Override
    public Class<TomEEWebappConfiguration> getConfigurationClass() {
        return TomEEWebappConfiguration.class;
    }
}
