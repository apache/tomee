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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class Setup {
    public static final int DEFAULT_HTTP_PORT = 8080;
    public static final int DEFAULT_STOP_PORT = 8005;
    public static final int DEFAULT_AJP_PORT = 8009;

    public static void exportProperties(File openejbHome, TomEEConfiguration c) {
        System.setProperty("tomee.http.port", String.valueOf(c.getHttpPort()));
        System.setProperty("tomee.ajp.port", String.valueOf(c.getAjpPort()));
        System.setProperty("tomee.shutdown.port", String.valueOf(c.getStopPort()));
        System.setProperty("java.naming.provider.url", "http://localhost:" + c.getHttpPort() + "/tomee/ejb");
        System.setProperty("connect.tries", "90");
        System.setProperty("server.http.port", String.valueOf(c.getHttpPort()));
        System.setProperty("server.shutdown.port", String.valueOf(c.getStopPort()));
        System.setProperty("java.opts", "-Xmx512m -Xms256m -XX:PermSize=64m -XX:MaxPermSize=256m -XX:ReservedCodeCacheSize=64m -Dtomee.http.port=" + c.getHttpPort());
        System.setProperty("openejb.home", openejbHome.getAbsolutePath());
    }

    public static void updateServerXml(File openejbHome, TomEEConfiguration c, int http, int stop, int ajp) throws IOException {
        final Map<String, String> replacements = new HashMap<String, String>();
        replacements.put(Integer.toString(http), String.valueOf(c.getHttpPort()));
        replacements.put(Integer.toString(stop), String.valueOf(c.getStopPort()));
        replacements.put(Integer.toString(ajp), String.valueOf(c.getAjpPort()));
        final String s = File.separator;
        replace(replacements, new File(openejbHome, "conf" + s + "server.xml"));
    }

    public static File findHome(File directory) {
        final File conf = new File(directory, "conf");
        final File webapps = new File(directory, "webapps");

        if (conf.exists() && conf.isDirectory() && webapps.exists() && webapps.isDirectory()) {
            return directory;
        }

        for (File file : directory.listFiles()) {
            if (".".equals(file.getName()) || "..".equals(file.getName())) continue;

            final File found = findHome(file);

            if (found != null) {
                return found;
            }
        }

        return null;
    }

    public static File downloadAndUnpack(File dir, String artifactID) throws LifecycleException {

        File zipFile = downloadFile(artifactID, null);

        Zips.unzip(zipFile, dir);

        return findHome(dir);
    }

    public static File downloadFile(String artifactName, String altUrl) {
        final String cache = SystemInstance.get().getOptions().get(ProvisioningUtil.OPENEJB_DEPLOYER_CACHE_FOLDER, (String) null);
        System.setProperty(ProvisioningUtil.OPENEJB_DEPLOYER_CACHE_FOLDER, "target");
        try {
            final File artifact = new MavenCache().getArtifact(artifactName, altUrl);
            if (artifact == null) throw new NullPointerException(String.format("No such artifact: %s", artifactName));
            return artifact;
        } finally {
            if (cache == null) {
                System.clearProperty(ProvisioningUtil.OPENEJB_DEPLOYER_CACHE_FOLDER);
            } else {
                System.setProperty(ProvisioningUtil.OPENEJB_DEPLOYER_CACHE_FOLDER, cache);
            }
        }
    }

    public static boolean isRunning(int port) {
        try {
            Socket socket = new Socket("localhost", port);
            OutputStream out = socket.getOutputStream();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void replace(Map<String, String> replacements, File file) throws IOException {
        BufferedReader reader = null;
        PrintWriter writer = null;

        try {
            File tmpFile = copyToTempFile(file);
            reader = new BufferedReader(new FileReader(tmpFile));
            writer = new PrintWriter(new FileWriter(file));
            String line;

            while ((line = reader.readLine()) != null) {
                Iterator<String> iterator = replacements.keySet().iterator();
                while (iterator.hasNext()) {
                    String pattern = iterator.next();
                    String replacement = replacements.get(pattern);

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
    }

    private static File copyToTempFile(File file) throws IOException {
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
            for (File webapp : webapps.listFiles()) {
                final String name = webapp.getName();
                if (webapp.isDirectory() && !name.equals("openejb") && !name.equals("tomee")) {
                    JarExtractor.delete(webapp);
                }
            }
        }
    }
}
