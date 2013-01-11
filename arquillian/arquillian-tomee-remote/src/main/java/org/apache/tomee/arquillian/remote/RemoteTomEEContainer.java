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
package org.apache.tomee.arquillian.remote;

import org.apache.openejb.arquillian.common.ArquillianUtil;
import org.apache.openejb.arquillian.common.Files;
import org.apache.openejb.arquillian.common.IO;
import org.apache.openejb.arquillian.common.Setup;
import org.apache.openejb.arquillian.common.TomEEContainer;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.config.RemoteServer;
import org.apache.tomee.util.InstallationEnrichers;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.shrinkwrap.api.Archive;

import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * TODO: delete old embedded adapter, move the tests and set those up
 */
public class RemoteTomEEContainer extends TomEEContainer<RemoteTomEEConfiguration> {

    private static final Logger logger = Logger.getLogger(RemoteTomEEContainer.class.getName());

    private RemoteServer container;
    private boolean shutdown = false;
    private File tomeeHome;
    private Collection<Archive<?>> containerArchives;

    @Override
    public void start() throws LifecycleException {
        // see if TomEE is already running by checking the http port
        if (Setup.isRunning(configuration.getHost(), configuration.getHttpPort())) {

            logger.info(String.format("TomEE found running on port %s", configuration.getHttpPort()));

            return;
        }

        shutdown = true;

        final String shutdownPort = System.getProperty(RemoteServer.SERVER_SHUTDOWN_PORT);
        final String shutdownHost = System.getProperty(RemoteServer.SERVER_SHUTDOWN_HOST);
        final String shutdownCommand = System.getProperty(RemoteServer.SERVER_SHUTDOWN_COMMAND);
        final String debug = System.getProperty(RemoteServer.OPENEJB_SERVER_DEBUG);
        final String debugPort = System.getProperty(RemoteServer.SERVER_DEBUG_PORT);

        try {

            configure();

            System.setProperty(RemoteServer.SERVER_SHUTDOWN_PORT, Integer.toString(configuration.getStopPort()));
            System.setProperty(RemoteServer.SERVER_SHUTDOWN_COMMAND, configuration.getStopCommand());
            System.setProperty(RemoteServer.SERVER_SHUTDOWN_HOST, configuration.getStopHost());
            if (configuration.isDebug()) {
                System.setProperty(RemoteServer.OPENEJB_SERVER_DEBUG, "true");
                System.setProperty(RemoteServer.SERVER_DEBUG_PORT, Integer.toString(configuration.getDebugPort()));
            }
            container = new RemoteServer();

            container.setAdditionalClasspath(InstallationEnrichers.addOneLineFormatter(tomeeHome));
            container.start(args(), "start", true);
            container.killOnExit();

            if (configuration.getProperties() != null) {
                final Properties props = new Properties();
                IO.readProperties(IO.read(configuration.getProperties().getBytes()), new Properties());

                containerArchives = ArquillianUtil.toDeploy(props);
                for (Archive<?> archive : containerArchives) {
                    deploy(archive);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to start remote container", e);
            throw new LifecycleException("Unable to start remote container:" + e.getMessage(), e);
        } finally {
            resetSystemProperty(RemoteServer.SERVER_SHUTDOWN_PORT, shutdownPort);
            resetSystemProperty(RemoteServer.SERVER_SHUTDOWN_HOST, shutdownHost);
            resetSystemProperty(RemoteServer.SERVER_SHUTDOWN_COMMAND, shutdownCommand);
            resetSystemProperty(RemoteServer.OPENEJB_SERVER_DEBUG, debug);
            resetSystemProperty(RemoteServer.SERVER_DEBUG_PORT, debugPort);
        }
    }

    private List<String> args() {
        String opts = configuration.getCatalina_opts();
        if (opts == null || (opts = opts.trim()).isEmpty()) {
            return Collections.emptyList();
        }

        final List<String> splitOnSpace = new ArrayList<String>();
        opts = opts.replace("\n", " ").trim();

        final Iterator<String> it = new ArgsIterator(opts);
        while (it.hasNext()) {
            splitOnSpace.add(it.next());
        }

        return splitOnSpace;
    }

    private static void resetSystemProperty(final String key, final String value) {
        if (value == null) {
            System.getProperties().remove(key);
        } else {
            System.setProperty(key, value);
        }
    }

    private void configure() throws LifecycleException, IOException {
        final File workingDirectory = new File(configuration.getDir()).getAbsoluteFile();

        if (configuration.getCleanOnStartUp()) {
            Files.delete(workingDirectory);
        }

        if (workingDirectory.exists()) {

            Files.assertDir(workingDirectory);

        } else {

            Files.mkdir(workingDirectory);
            Files.deleteOnExit(workingDirectory);
        }

        Files.readable(workingDirectory);
        Files.writable(workingDirectory);

        tomeeHome = Setup.findHome(workingDirectory);

        if (tomeeHome == null) {
            tomeeHome = Setup.downloadAndUnpack(workingDirectory, configuration.getArtifactName());

            logger.log(Level.INFO, "Downloaded container to: " + tomeeHome);
        }

        Files.assertDir(tomeeHome);
        Files.readable(tomeeHome);
        Files.writable(tomeeHome);

        Setup.configureServerXml(tomeeHome, configuration);

        Setup.synchronizeFolder(tomeeHome, configuration.getConf(), "conf");
        Setup.synchronizeFolder(tomeeHome, configuration.getBin(), "bin");
        Setup.synchronizeFolder(tomeeHome, configuration.getLib(), "lib");

        Setup.configureSystemProperties(tomeeHome, configuration);

        final String opts = configuration.getCatalina_opts();

        Setup.exportProperties(tomeeHome, configuration, opts == null || (!opts.contains("-Xm") && !opts.matches(".*-XX:[^=]*Size=.*")));
        Setup.installArquillianBeanDiscoverer(tomeeHome);

        if (configuration.isRemoveUnusedWebapps()) {
            Setup.removeUselessWebapps(tomeeHome);
        }

        if (configuration.isSimpleLog() && noLoggingConfigProvided()) {
            final File loggingProperties = Files.path(tomeeHome, "conf", "logging.properties");

            final Properties logging = new Properties();
            logging.put("handlers", "java.util.logging.ConsoleHandler");
            logging.put(".handlers", "java.util.logging.ConsoleHandler");
            logging.put("java.util.logging.ConsoleHandler.level", "INFO");
            logging.put("java.util.logging.ConsoleHandler.formatter", SimpleTomEEFormatter.class.getName());

            IO.writeProperties(loggingProperties, logging);
        }

        if (logger.isLoggable(Level.FINE)) {
            final Map<Object, Object> map = new TreeMap<Object, Object>(System.getProperties());
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                logger.log(Level.FINE, String.format("%s = %s\n", entry.getKey(), entry.getValue()));
            }
        }
    }

    private boolean noLoggingConfigProvided() {
        if (configuration.getConf() == null) return true;

        final File conf = new File(configuration.getConf());

        return !(conf.exists() && new File(conf, "logging.properties").exists());
    }

    @Override
    public void stop() throws LifecycleException {
        ArquillianUtil.undeploy(this, containerArchives);

        // only stop the container if we started it
        if (shutdown) {
            Setup.removeArquillianBeanDiscoverer(tomeeHome);
            container.destroy();
        }
    }

    @Override
    public Class<RemoteTomEEConfiguration> getConfigurationClass() {
        return RemoteTomEEConfiguration.class;
    }

    @Override
    protected Deployer deployer() throws NamingException {
        try {
            return super.deployer();
        } catch (RuntimeException ne) {
            // some debug lines
            if (Boolean.getBoolean("openejb.arquillian.debug")) {
                container.kill3UNIX();
                LOGGER.info("Can't connect to deployer through: " + providerUrl());
                try {
                    LOGGER.info("Here is the server.xml:\n" + IO.slurp(new File(Setup.findHome(new File(configuration.getDir()).getAbsoluteFile()), "conf/server.xml")));
                } catch (IOException ignored) {
                    // no-op
                }
            }
            throw ne;
        }
    }

    private static class ArgsIterator implements Iterator<String> {
        private final String string;
        private int currentIndex;

        public ArgsIterator(final String opts) {
            string = opts;
            currentIndex = 0;
        }

        @Override
        public boolean hasNext() {
            return string != null && currentIndex < string.length();
        }

        @Override
        public String next() {
            skipWhiteCharacters();

            if (done()) {
                throw new UnsupportedOperationException("No more element");
            }

            char endChar;
            if (string.charAt(currentIndex) == '"') {
                currentIndex++;
                endChar = '"';
            } else {
                endChar = ' ';
            }

            final int start = currentIndex;
            int end = string.indexOf(endChar, currentIndex + 1);
            if (end <= 0) {
                end = string.length();
            }

            currentIndex = end + 1;

            return string.substring(start, end);
        }

        private void skipWhiteCharacters() {
            while (!done() && (string.charAt(currentIndex) == ' ' || string.charAt(currentIndex) == '\t')) {
                currentIndex++;
            }
        }

        private boolean done() {
            return currentIndex >= string.length();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
