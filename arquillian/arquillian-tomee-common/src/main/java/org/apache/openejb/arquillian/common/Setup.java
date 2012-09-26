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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.arquillian.common;

import org.apache.openejb.loader.ProvisioningUtil;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.JarExtractor;
import org.apache.tomee.util.QuickServerXmlParser;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @version $Rev$ $Date$
 */
public class Setup {
    private static final Logger LOGGER = Logger.getLogger(Setup.class.getName()); // JUL is used by arquillian so that's fine

    public static void exportProperties(final File openejbHome, final TomEEConfiguration c) {
        System.setProperty("java.naming.provider.url", "http://" + c.getHost() + ":" + c.getHttpPort() + "/tomee/ejb");
        System.setProperty("connect.tries", "90");
        System.setProperty("server.http.port", String.valueOf(c.getHttpPort()));
        System.setProperty("server.shutdown.port", String.valueOf(c.getStopPort()));
        System.setProperty("java.opts", "-Xmx512m -Xms256m -XX:PermSize=64m -XX:MaxPermSize=256m -XX:ReservedCodeCacheSize=64m -Dtomee.httpPort=" + c.getHttpPort());
        System.setProperty("openejb.home", openejbHome.getAbsolutePath());
        System.setProperty("tomee.home", openejbHome.getAbsolutePath());
    }

    public static void updateServerXml(final File openejbHome, final int httpPort, final int stopPort, final int ajpPort) throws IOException {
        final File sXml = new File(openejbHome, "conf" + File.separator + "server.xml");
        final QuickServerXmlParser ports = QuickServerXmlParser.parse(sXml);

        final Map<String, String> replacements = new HashMap<String, String>();
        replacements.put(ports.http(), String.valueOf(httpPort));
        replacements.put(ports.stop(), String.valueOf(stopPort));
        replacements.put(ports.ajp(), String.valueOf(ajpPort));
        replace(replacements, sXml);
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
                if (".".equals(file.getName()) || "..".equals(file.getName())) continue;

                final File found = findHome(file);

                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    public static File downloadAndUnpack(final File dir, final String artifactID) throws LifecycleException {

        final File zipFile = downloadFile(artifactID, null);

        Zips.unzip(zipFile, dir);

        return findHome(dir);
    }

    public static File downloadFile(final String artifactName, final String altUrl) {
        final String cache = SystemInstance.get().getOptions().get(ProvisioningUtil.OPENEJB_DEPLOYER_CACHE_FOLDER, (String) null);
        if (cache == null) { // let the user override it
            System.setProperty(ProvisioningUtil.OPENEJB_DEPLOYER_CACHE_FOLDER, "target");
        }

        try {
            final File artifact = MavenCache.getArtifact(artifactName, altUrl);
            if (artifact == null) throw new NullPointerException(String.format("No such artifact: %s", artifactName));
            return artifact.getAbsoluteFile();
        } finally {
            if (cache == null) {
                System.clearProperty(ProvisioningUtil.OPENEJB_DEPLOYER_CACHE_FOLDER);
            }
        }
    }

    public static boolean isRunning(final String host, final int port) {
        Socket socket = null;
        try {
            socket = new Socket(host, port);
            socket.getOutputStream().close();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                    // no-op
                }
            }
        }
    }

    public static void replace(final Map<String, String> replacements, final File file) throws IOException {
        BufferedReader reader = null;
        PrintWriter writer = null;

        try {
            final File tmpFile = copyToTempFile(file);
            reader = new BufferedReader(new FileReader(tmpFile));
            writer = new PrintWriter(new FileWriter(file));
            String line;

            while ((line = reader.readLine()) != null) {
                final Iterator<String> iterator = replacements.keySet().iterator();
                while (iterator.hasNext()) {
                    final String pattern = iterator.next();
                    final String replacement = replacements.get(pattern);

                    line = line.replaceAll(pattern, replacement);
                }

                writer.println(line);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }

            if (writer != null) {
                writer.close();
            }
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            IO.copy(file, System.out);
        }
    }

    private static File copyToTempFile(final File file) throws IOException {
        InputStream is = null;
        OutputStream os = null;

        File tmpFile;
        try {
            tmpFile = File.createTempFile("oejb", ".fil");
            tmpFile.deleteOnExit();

            is = new FileInputStream(file);
            os = new FileOutputStream(tmpFile);

            IO.copy(is, os);
        } finally {
            if (is != null) {
                is.close();
            }

            if (os != null) {
                os.close();
            }
        }

        return tmpFile;
    }

    public static void removeUselessWebapps(final File openejbHome) {
        final File webapps = new File(openejbHome, "webapps");
        if (webapps.isDirectory()) {
            final File[] files = webapps.listFiles();
            if (files != null) {
                for (final File webapp : files) {
                    final String name = webapp.getName();
                    if (webapp.isDirectory() && !name.equals("openejb") && !name.equals("tomee")) {
                        JarExtractor.delete(webapp);
                    }
                }
            }
        }
    }

    public static void configureServerXml(final File openejbHome, final TomEEConfiguration configuration) throws IOException {
        if (configuration.getServerXml() != null) {
            final File sXml = new File(configuration.getServerXml());
            if (!sXml.exists()) {
                LOGGER.severe("provided server.xml doesn't exist: '" + sXml.getPath() + "'");
            } else {
                final FileOutputStream fos = new FileOutputStream(new File(openejbHome, "conf/server.xml"));
                try {
                    IO.copy(sXml, fos);
                } finally {
                    IO.close(fos);
                }
                configuration.setStopPort(Integer.parseInt(QuickServerXmlParser.parse(sXml).stop()));
                return; // in this case we don't want to override the conf
            }
        }
        Setup.updateServerXml(openejbHome, configuration.getHttpPort(), configuration.getStopPort(), ajpPort(configuration));
    }

    private static int ajpPort(final TomEEConfiguration config) {
        try {
            final Method ajbPort = config.getClass().getMethod("getAjpPort");
            return (Integer) ajbPort.invoke(config);
        } catch (Exception e) {
            return Integer.parseInt(QuickServerXmlParser.DEFAULT_AJP_PORT);
        }
    }

    public static void configureSystemProperties(final File openejbHome, final TomEEConfiguration configuration) {
        final StringBuilder builder = new StringBuilder();
        final File systemProperties = new File(openejbHome, "conf/system.properties");
        if (systemProperties.exists()) {
            try {
                builder.append(org.apache.openejb.loader.IO.slurp(systemProperties));
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "can't read " + systemProperties.getAbsolutePath(), e);
            }
        }
        builder.append("\n").append(configuration.systemProperties());

        if (configuration.isQuickSession()) {
            builder.append("\nopenejb.session.manager=org.apache.tomee.catalina.session.QuickSessionManager");
        }
        builder.append("\n");

        FileWriter writer = null;
        try {
            writer = new FileWriter(systemProperties);
            writer.write(builder.toString());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "can't save system properties " + systemProperties.getAbsolutePath(), e);
            return;
        } finally {
            try {
                IO.close(writer);
            } catch (IOException ignored) {
                // no-op
            }
        }
    }

    public static void synchronizeFolder(final File openejbHome, final String src, final String dir) {
        if (src != null && !src.isEmpty()) {
            final File confSrc = new File(src);
            if (confSrc.exists()) {
                final File conf = new File(openejbHome, dir);
                final Collection<File> files = org.apache.openejb.loader.Files.collect(confSrc, TrueFileFilter.instance());
                files.remove(confSrc);
                for (File f : files) {
                    if (f.isDirectory()) {
                        LOGGER.log(Level.WARNING, "skipping " + f.getAbsolutePath() + " since folders are currently not supported");
                        continue;
                    }

                    try {
                        org.apache.openejb.loader.IO.copy(f, new File(conf, relativize(f, confSrc)));
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "ignoring copy of " + f.getAbsolutePath(), e);
                    }
                }
            } else {
                LOGGER.warning("can't find " + confSrc.getAbsolutePath());
            }
        }
    }

    private static String relativize(final File f, final File base) {
        return f.getAbsolutePath().substring(base.getAbsolutePath().length() + 1);
    }

    private static class TrueFileFilter implements FileFilter {
        private static TrueFileFilter INSTANCE = null;

        public static TrueFileFilter instance() {
            if (INSTANCE == null) {
                INSTANCE = new TrueFileFilter();
            }
            return INSTANCE;
        }

        @Override
        public boolean accept(final File pathname) {
            return true;
        }
    }
}
