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
package org.apache.openejb.arquillian.common;

import org.apache.openejb.loader.ProvisioningUtil;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.loader.provisining.ProvisioningResolver;
import org.apache.tomee.util.QuickServerXmlParser;
import org.codehaus.swizzle.stream.ReplaceStringsInputStream;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Collections.emptyMap;

/**
 * @version $Rev$ $Date$
 */
public class Setup {
    private static final Logger LOGGER = Logger.getLogger(Setup.class.getName()); // JUL is used by arquillian so that's fine
    public static final String TOMEE_BEAN_DISCOVERER_JAR = "lib" + File.separator + "xx-arquillian-tomee-bean-discoverer.jar"; // starts with xx to not be filtered
    private static final String DEFAULT_MEM_CONFIG = (javaVersion() >= 1.8 ? "" : "-XX:PermSize=64m -XX:MaxPermSize=256m ")
                                                            + "-Xmx512m -Xms256m -XX:ReservedCodeCacheSize=64m";

    private static double javaVersion() {
        try {
            return Double.parseDouble(System.getProperty("java.version", "1.7").substring(0, 3));
        } catch (final Exception nfe) {
            return 1.6;
        }
    }

    public static void exportProperties(final File tomeeHome, final TomEEConfiguration c, final boolean defaultMem) {
        final int httpPort = c.getHttpPort();
        System.setProperty("java.naming.provider.url", "http://" + c.getHost() + ":" + httpPort + "/tomee/ejb");
        System.setProperty("connect.tries", "90");
        System.setProperty("server.http.port", String.valueOf(httpPort));
        System.setProperty("server.shutdown.port", String.valueOf(c.getStopPort()));
        if (defaultMem) {
            System.setProperty("java.opts", DEFAULT_MEM_CONFIG + " -Dtomee.httpPort=" + httpPort);
        } else {
            System.setProperty("java.opts", "-Dtomee.httpPort=" + httpPort);
        }
        System.setProperty("openejb.home", tomeeHome.getAbsolutePath());
        System.setProperty("tomee.home", tomeeHome.getAbsolutePath());
    }

    public static void updateServerXml(final File tomeeHome, final TomEEConfiguration configuration) throws IOException {
        final File serverXml = Files.path(new File(tomeeHome.getAbsolutePath()), "conf", "server.xml");
        if (!serverXml.exists()) {
            return;
        }

        final QuickServerXmlParser ports = QuickServerXmlParser.parse(serverXml);
        if (configuration.getKeepServerXmlAsThis()) {
            // force ports to be able to stop the server and get @ArquillianResource
            configuration.setHttpPort(Integer.parseInt(ports.http()));
            configuration.setStopPort(Integer.parseInt(ports.stop()));
            return; // in this case we don't want to override the conf
        }

        final Map<String, String> replacements = new HashMap<>();
        replacements.put(ports.http(), String.valueOf(configuration.getHttpPort()));
        replacements.put(ports.https(), String.valueOf(configuration.getHttpsPort()));
        replacements.put(ports.stop(), String.valueOf(configuration.getStopPort()));
        replacements.put(ports.ajp(), String.valueOf(ajpPort(configuration)));

        if (configuration.isUnpackWars()) {
            replacements.put("unpackWARs=\"false\"", "unpackWARs=\"true\"");
        } else {
            replacements.put("unpackWARs=\"true\"", "unpackWARs=\"false\"");
        }

        replace(replacements, serverXml, true);
    }

    public static File findHome(File directory) {

        directory = directory.getAbsoluteFile();

        final File f = findHomeImpl(directory);

        if (null == f) {
            LOGGER.log(Level.INFO, "Unable to find home in: " + directory);
        }

        return f;
    }

    public static File findHomeImpl(final File directory) {
        final File conf = new File(directory, "conf").getAbsoluteFile();
        final File webapps = new File(directory, "webapps").getAbsoluteFile();

        if (conf.exists() && conf.isDirectory() && webapps.exists() && webapps.isDirectory()) {
            return directory;
        }

        final File[] files = directory.listFiles();
        if (null != files) {

            for (final File file : files) {
                if (".".equals(file.getName()) || "..".equals(file.getName())) {
                    continue;
                }

                final File found = findHome(file);

                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    public static File downloadAndUnpack(final File dir, final String artifactID, final String defaultTempDir) throws LifecycleException {

        final File zipFile = downloadFile(artifactID, null, defaultTempDir);

        Zips.unzip(zipFile, dir);

        return findHome(dir);
    }

    public static File downloadFile(final String artifactName, final String altUrl, final String defaultTempDir) {
        final String cache = SystemInstance.isInitialized() ?
            SystemInstance.get().getOptions().get(ProvisioningResolver.OPENEJB_DEPLOYER_CACHE_FOLDER, (String) null) :
            System.getProperty(ProvisioningResolver.OPENEJB_DEPLOYER_CACHE_FOLDER);
        if (cache == null && defaultTempDir != null) { // let the user override it
            System.setProperty(ProvisioningResolver.OPENEJB_DEPLOYER_CACHE_FOLDER, defaultTempDir);
        }

        try {
            final File artifact = MavenCache.getArtifact(artifactName, altUrl);
            if (artifact == null) {
                throw new NullPointerException(String.format("No such artifact: %s", artifactName));
            }
            return artifact.getAbsoluteFile();
        } finally {
            if (cache == null) {
                System.clearProperty(ProvisioningResolver.OPENEJB_DEPLOYER_CACHE_FOLDER);
            }
        }
    }

    public static boolean isRunning(final String host, final int port) {
        Socket socket = null;
        try {
            socket = new Socket(host, port);
            socket.getOutputStream().close();
            return true;
        } catch (final Exception e) {
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (final IOException ignored) {
                    // no-op
                }
            }
        }
    }

    public static void replace(final Map<String, String> replacements, final File file, final boolean escape) throws IOException {

        InputStream in = IO.read(file);

        if (escape) {
            final Map<String, String> escaped = new HashMap<>();
            for (final Map.Entry<String, String> entry : replacements.entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();
                if (key.startsWith("\"") && key.endsWith("\"")) {
                    escaped.put(key, value);
                } else {
                    escaped.put("\"" + key + "\"", "\"" + value + "\"");
                }
            }

            replacements.clear();
            replacements.putAll(escaped);
        }

        in = new ReplaceStringsInputStream(in, replacements);

        final String data = IO.slurp(in);

        IO.copy(data.getBytes(), file);

        if (LOGGER.isLoggable(Level.FINE)) {
            IO.copy(data.getBytes(), System.out);
        }
    }

    public static void removeUselessWebapps(final File tomeeHome, final String... exceptions) {
        final File webapps = new File(tomeeHome, "webapps");
        if (webapps.isDirectory()) {
            final File[] files = webapps.listFiles();
            if (files != null) {
                for (final File webapp : files) {
                    boolean delete = true;
                    if (exceptions != null) {
                        for (final String ignore : exceptions) {
                            if (webapp.getName().equals(ignore)) {
                                delete = false;
                                break;
                            }
                        }
                    }
                    if (delete) {
                        org.apache.openejb.loader.Files.delete(webapp);
                    }
                }
            }
        }
    }

    public static void configureServerXml(final File tomeeHome, final TomEEConfiguration configuration) throws IOException {

        if (configuration.getServerXml() != null) {

            final File serverXml = new File(configuration.getServerXml());

            if (!serverXml.exists()) {
                LOGGER.severe("Provided server.xml doesn't exist: '" + serverXml.getPath() + "'");
            } else {

                // Read in the contents to memory so we can avoid re-reading for parsing
                final String data = IO.slurp(serverXml);

                IO.copy(data.getBytes(), Files.path(tomeeHome, "conf", "server.xml"));
            }
        }
        updateServerXml(tomeeHome, configuration);
    }

    private static int ajpPort(final TomEEConfiguration config) {
        try {
            final Method ajbPort = config.getClass().getMethod("getAjpPort");
            return (Integer) ajbPort.invoke(config);
        } catch (final Exception e) {
            return Integer.parseInt(QuickServerXmlParser.DEFAULT_AJP_PORT);
        }
    }

    public static void configureSystemProperties(final File tomeeHome, final TomEEConfiguration configuration) {
        final File file = Files.path(tomeeHome, "conf", "system.properties");

        // Must use an actual properties object to avoid duplicate keys
        final Properties properties = new Properties();

        if (file.exists()) {
            try {
                IO.readProperties(file, properties);
            } catch (final IOException e) {
                LOGGER.log(Level.SEVERE, "Can't read " + file.getAbsolutePath(), e);
            }
        }

        if (configuration.getProperties() != null) {
            try {
                final InputStream bytes = IO.read(configuration.getProperties().getBytes());
                IO.readProperties(bytes, properties);
            } catch (final IOException e) {
                LOGGER.log(Level.SEVERE, "Can't parse <property name=\"properties\"> value '" + configuration.getProperties() + "'", e);
            }
        }

        if (configuration.isQuickSession()) {
            properties.put("openejb.session.manager", "org.apache.tomee.catalina.session.QuickSessionManager");
        }

        if (configuration.isUnsafeEjbd() && "*".equals(properties.getProperty("tomee.serialization.class.blacklist", "-").trim())) {
            properties.remove("tomee.serialization.class.blacklist");
            properties.put("tomee.serialization.class.whitelist", "*");
        }

        try {
            IO.writeProperties(file, properties);
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "Can't save system properties " + file.getAbsolutePath(), e);
        }
    }


    public static void synchronizeFolder(final File tomeeHome, final String src, final String dir) {
        if (src != null && !src.isEmpty()) {
            final File confSrc = new File(src);
            if (confSrc.exists()) {
                final File conf = new File(tomeeHome, dir);
                final Collection<File> files = org.apache.openejb.loader.Files.collect(confSrc, TrueFilter.INSTANCE);
                files.remove(confSrc);
                for (final File f : files) {
                    if (f.equals(confSrc) || f.getName().startsWith(".")) {
                        continue;
                    }

                    final String relativize = relativize(f, confSrc);
                    if (f.isDirectory()) {
                        final String path = dir + '/' + relativize;
                        Files.mkdir(new File(tomeeHome, path));
                        synchronizeFolder(tomeeHome, f.getAbsolutePath(), path);
                    } else {
                        try {
                            final File to = new File(conf, relativize);
                            Files.mkdir(to.getParentFile());
                            org.apache.openejb.loader.IO.copy(f, to);
                        } catch (final Exception e) {
                            LOGGER.log(Level.WARNING, "Ignoring copy of " + f.getAbsolutePath(), e);
                        }
                    }
                }
            } else {
                LOGGER.warning("Can't find " + confSrc.getAbsolutePath());
            }
        }
    }

    private static String relativize(final File f, final File base) {
        return f.getAbsolutePath().substring(base.getAbsolutePath().length() + 1);
    }

    public static void installArquillianBeanDiscoverer(final File home) {
        final File destination = new File(home, TOMEE_BEAN_DISCOVERER_JAR);
        ShrinkWrap.create(JavaArchive.class, destination.getName())
                .addClasses(BeanDicovererInstaller.class, TestClassDiscoverer.class, ArquillianFilterRunner.class, Discover.class)
                .addAsManifestResource(new StringAsset(BeanDicovererInstaller.class.getName()), ArchivePaths.create("org.apache.openejb.extension"))
                .as(ZipExporter.class).exportTo(destination, false);
    }

    public static void removeArquillianBeanDiscoverer(final File home) {
        final File destination = new File(home, TOMEE_BEAN_DISCOVERER_JAR);
        Files.delete(destination);
    }

    public static Map<File, String> addTomEELibraries(final File libFolder, final String additionalLibs, final boolean parseArgs) {
        if (additionalLibs == null) {
            return emptyMap();
        }
        final String libs = additionalLibs.trim();
        if (libs.isEmpty()) {
            return emptyMap();
        }

        try { // initializing the SystemInstance because we'll need it for configuration
            if (!SystemInstance.isInitialized()) {
                SystemInstance.init(new Properties());
            }
        } catch (final Exception e) {
            // no-op
        }

        final Map<File, String> configs = new HashMap<>();
        for (final String lib : libs.split("\n")) {
            final String trim = lib.trim();
            if (trim.isEmpty() || trim.startsWith("#")) {
                continue;
            }

            final String args;
            if (parseArgs) { // javaagent friendly
                int argsIdx = trim.indexOf('=');
                if (argsIdx < 0) {
                    argsIdx = trim.indexOf('?');
                }
                if (argsIdx > 0) {
                    args = trim.substring(argsIdx);
                } else {
                    args = "";
                }
            } else {
                args = null;
            }

            final Set<String> locations = ProvisioningUtil.realLocation(trim);
            for (final String location : locations) {
                final File from = new File(location);
                try {
                    final File to = new File(libFolder, from.getName());
                    org.apache.openejb.loader.IO.copy(from, to);
                    if (args != null) {
                        configs.put(to, args);
                    }
                } catch (final IOException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }
        return configs;
    }

    private static class TrueFilter implements FileFilter {
        private static final TrueFilter INSTANCE = new TrueFilter();

        @Override
        public boolean accept(final File pathname) {
            return true;
        }
    }
}
