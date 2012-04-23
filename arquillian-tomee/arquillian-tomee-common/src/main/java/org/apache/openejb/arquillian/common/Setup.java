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
import org.jboss.arquillian.container.spi.client.container.LifecycleException;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @version $Rev$ $Date$
 */
public class Setup {
    private static final Logger LOGGER = Logger.getLogger(Setup.class.getName()); // JUL is used by arquillian so that's fine

    public static final int DEFAULT_HTTP_PORT = 8080;
    public static final int DEFAULT_STOP_PORT = 8005;
    public static final int DEFAULT_AJP_PORT = 8009;

    public static void exportProperties(final File openejbHome, final TomEEConfiguration c) {
        System.setProperty("java.naming.provider.url", "http://localhost:" + c.getHttpPort() + "/tomee/ejb");
        System.setProperty("connect.tries", "90");
        System.setProperty("server.http.port", String.valueOf(c.getHttpPort()));
        System.setProperty("server.shutdown.port", String.valueOf(c.getStopPort()));
        System.setProperty("java.opts", "-Xmx512m -Xms256m -XX:PermSize=64m -XX:MaxPermSize=256m -XX:ReservedCodeCacheSize=64m -Dtomee.httpPort=" + c.getHttpPort());
        System.setProperty("openejb.home", openejbHome.getAbsolutePath());
        System.setProperty("tomee.home", openejbHome.getAbsolutePath());
    }

    public static void updateServerXml(final File openejbHome, final int httpPort, final int stopPort, final int ajpPort) throws IOException {
        final Map<String, String> replacements = new HashMap<String, String>();
        replacements.put(Integer.toString(DEFAULT_HTTP_PORT), String.valueOf(httpPort));
        replacements.put(Integer.toString(DEFAULT_STOP_PORT), String.valueOf(stopPort));
        replacements.put(Integer.toString(DEFAULT_AJP_PORT), String.valueOf(ajpPort));
        final String s = File.separator;
        replace(replacements, new File(openejbHome, "conf" + s + "server.xml"));
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
        System.setProperty(ProvisioningUtil.OPENEJB_DEPLOYER_CACHE_FOLDER, "target");
        try {
            final File artifact = new MavenCache().getArtifact(artifactName, altUrl);
            if (artifact == null) throw new NullPointerException(String.format("No such artifact: %s", artifactName));
            return artifact.getAbsoluteFile();
        } finally {
            if (cache == null) {
                System.clearProperty(ProvisioningUtil.OPENEJB_DEPLOYER_CACHE_FOLDER);
            } else {
                System.setProperty(ProvisioningUtil.OPENEJB_DEPLOYER_CACHE_FOLDER, cache);
            }
        }
    }

    public static boolean isRunning(final int port) {
        try {
            final Socket socket = new Socket("localhost", port);
            final OutputStream out = socket.getOutputStream();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
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
}
