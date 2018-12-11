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
package org.apache.tomee.arquillian.remote;

import org.apache.openejb.arquillian.common.ArquillianFilterRunner;
import org.apache.openejb.arquillian.common.ArquillianUtil;
import org.apache.openejb.arquillian.common.Files;
import org.apache.openejb.arquillian.common.IO;
import org.apache.openejb.arquillian.common.Setup;
import org.apache.openejb.arquillian.common.TomEEContainer;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.DeployerEjb;
import org.apache.openejb.config.RemoteServer;
import org.apache.openejb.util.NetworkUtil;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.protocol.servlet.ServletMethodExecutor;
import org.jboss.shrinkwrap.api.Archive;

import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemoteTomEEContainer extends TomEEContainer<RemoteTomEEConfiguration> {
    private static final Logger logger = Logger.getLogger(RemoteTomEEContainer.class.getName());

    private static final String ARQUILLIAN_FILTER = "-Dorg.apache.openejb.servlet.filters=" + ArquillianFilterRunner.class.getName() + "=" + ServletMethodExecutor.ARQUILLIAN_SERVLET_MAPPING;

    private RemoteServer container;
    private boolean shutdown;
    private File tomeeHome;
    private Collection<Archive<?>> containerArchives;
    private final Properties deployerProperties = new Properties();

    @Override
    public void setup(final RemoteTomEEConfiguration configuration) {
        super.setup(configuration);

        if (configuration.getDeployerProperties() != null) {
            try {
                final InputStream bytes = IO.read(configuration.getDeployerProperties().getBytes());
                IO.readProperties(bytes, deployerProperties);
            } catch (final IOException e) {
                logger.log(Level.SEVERE, "Can't parse <property name=\"properties\"> value '" + configuration.getProperties() + "'", e);
            }
        }
    }

    @Override
    protected String providerUrl() {
        return String.format(configuration.getProviderUrlPattern(), super.providerUrl());
    }

    @Override
    public void start() throws LifecycleException {
        // see if TomEE is already running by checking the http port
        final int httpPort = configuration.getHttpPort();

        if (Setup.isRunning(configuration.getHost(), httpPort)) {

            String host = "local";

            if (!NetworkUtil.isLocalAddress(configuration.getHost())) {
                //Supply at least this property so that the archive is transmitted on deploy
                if (null == deployerProperties.getProperty(DeployerEjb.OPENEJB_USE_BINARIES)) {
                    deployerProperties.setProperty(DeployerEjb.OPENEJB_USE_BINARIES, "true");
                }
                host = "remote";
            }

            logger.info(String.format("TomEE found running on %s port %s", host, httpPort));

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

            final int stopPort = configuration.getStopPort();
            System.setProperty(RemoteServer.SERVER_SHUTDOWN_PORT, Integer.toString(stopPort));
            System.setProperty(RemoteServer.SERVER_SHUTDOWN_COMMAND, configuration.getStopCommand());
            System.setProperty(RemoteServer.SERVER_SHUTDOWN_HOST, configuration.getStopHost());

            if (configuration.isDebug()) {
                System.setProperty(RemoteServer.OPENEJB_SERVER_DEBUG, "true");
                System.setProperty(RemoteServer.SERVER_DEBUG_PORT, Integer.toString(configuration.getDebugPort()));
            }

            container = new RemoteServer();
            container.setPortStartup(httpPort);

            try {
                container.start(args(), "start", true);
            } catch (final Exception e) {
                container.destroy();
                throw e;
            }

            container.killOnExit();

            if (configuration.getProperties() != null) {
                final Properties props = new Properties();
                IO.readProperties(IO.read(configuration.getProperties().getBytes()), props);

                containerArchives = ArquillianUtil.toDeploy(props);
                for (final Archive<?> archive : containerArchives) {
                    deploy(archive);
                }
            }
        } catch (final Exception e) {
            if (container != null) {
                container.destroy();
            }
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

    @Override
    protected Properties getDeployerProperties() {
        if (deployerProperties.isEmpty()) {
            return null;
        }
        return deployerProperties;
    }

    private List<String> args() {
        String opts = configuration.getCatalina_opts();
        if (opts != null) {
            opts = opts.trim();
        }
        if (opts == null || opts.isEmpty()) {
            return Arrays.asList(
                    "-Dorg.apache.catalina.STRICT_SERVLET_COMPLIANCE=false",
                    ARQUILLIAN_FILTER,
                    "-Dopenejb.system.apps=true", "-Dtomee.remote.support=true"
            );
        }

        final List<String> splitOnSpace = new ArrayList<String>();

        final Iterator<String> it = new ArgsIterator(opts);
        while (it.hasNext()) {
            splitOnSpace.add(it.next());
        }

        if (!splitOnSpace.contains("-Dorg.apache.catalina.STRICT_SERVLET_COMPLIANCE=true")) {
            splitOnSpace.add("-Dorg.apache.catalina.STRICT_SERVLET_COMPLIANCE=false");
        }
        splitOnSpace.add(ARQUILLIAN_FILTER);
        splitOnSpace.add("-Dopenejb.system.apps=true");
        splitOnSpace.add("-Dtomee.remote.support=true");
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
            tomeeHome = Setup.downloadAndUnpack(workingDirectory, configuration.getArtifactName(), configuration.getDir());

            logger.log(Level.INFO, "Downloaded container to: " + tomeeHome);
        }

        Files.assertDir(tomeeHome);
        Files.readable(tomeeHome);
        Files.writable(tomeeHome);

        Setup.synchronizeFolder(tomeeHome, configuration.getConf(), "conf");
        Setup.synchronizeFolder(tomeeHome, configuration.getBin(), "bin");
        Setup.synchronizeFolder(tomeeHome, configuration.getLib(), "lib");
        Setup.addTomEELibraries(new File(tomeeHome, "lib"), configuration.getAdditionalLibs(), false);
        if (configuration.getEndorsed() != null && !configuration.getEndorsed().isEmpty()) {
            final File endorsed = new File(tomeeHome, "endorsed");
            Files.mkdir(endorsed);
            Setup.addTomEELibraries(endorsed, configuration.getEndorsed(), false);
        }

        String opts = configuration.getCatalina_opts();
        if (configuration.getJavaagent() != null && !configuration.getJavaagent().isEmpty()) {
            final File javaagent = new File(tomeeHome, "javaagent");
            Files.mkdir(javaagent);
            final Map<File, String> agents = Setup.addTomEELibraries(javaagent, configuration.getJavaagent(), true);
            if (!agents.isEmpty()) {
                if (opts == null) {
                    opts = "";
                }
                for (final Map.Entry<File, String> entry : agents.entrySet()) {
                    opts += " \"-javaagent:" + entry.getKey().getAbsolutePath() + entry.getValue() + "\"";
                }
            }
            configuration.setCatalina_opts(opts);
        }

        Setup.configureServerXml(tomeeHome, configuration);

        Setup.configureSystemProperties(tomeeHome, configuration);

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
            logging.put("java.util.logging.ConsoleHandler.formatter", "org.apache.tomee.jul.formatter.SimpleTomEEFormatter");

            IO.writeProperties(loggingProperties, logging);
        }

        if (logger.isLoggable(Level.FINE)) {
            final Map<Object, Object> map = new TreeMap<Object, Object>(System.getProperties());
            for (final Map.Entry<Object, Object> entry : map.entrySet()) {
                logger.log(Level.FINE, String.format("%s = %s\n", entry.getKey(), entry.getValue()));
            }
        }
    }

    private boolean noLoggingConfigProvided() {
        if (configuration.getConf() == null) {
            return true;
        }

        final File conf = new File(configuration.getConf());

        return !(conf.exists()
                && (new File(conf, "logging.properties").exists()
                || new File(conf, "log4j.properties").exists()
                || new File(conf, "log4j.xml").exists()));
    }

    @Override
    public void stop() throws LifecycleException {
        ArquillianUtil.undeploy(this, containerArchives);

        // only stop the container if we started it
        if (shutdown) {
            try {
                Setup.removeArquillianBeanDiscoverer(tomeeHome);
                container.destroy();
            } finally {
                resetSerialization();
            }
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
        } catch (final RuntimeException ne) {
            // some debug lines
            if (Boolean.getBoolean("openejb.arquillian.debug")) {
                container.kill3UNIX();
                LOGGER.info("Can't connect to deployer through: " + providerUrl());
                try {
                    LOGGER.info("Here is the server.xml:\n" + IO.slurp(new File(Setup.findHome(new File(configuration.getDir()).getAbsoluteFile()), "conf/server.xml")));
                } catch (final IOException ignored) {
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

            final char endChar;
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
