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
package org.apache.openejb.arquillian.remote;

import org.apache.openejb.arquillian.common.FileUtils;
import org.apache.openejb.arquillian.common.MavenCache;
import org.apache.openejb.arquillian.common.TomEEContainer;
import org.apache.openejb.config.RemoteServer;
import org.apache.tomee.installer.Installer;
import org.apache.tomee.installer.Paths;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.sonatype.aether.artifact.Artifact;

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
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
 * TODO: delete old embedded adapter, move the tests and set those up
 */
public class RemoteTomEEContainer extends TomEEContainer {
    private static File DOWNLOADED = null;

    private RemoteServer container;
    private boolean needsStart = false;

    public void start() throws LifecycleException {
        // see if TomEE is already running by checking the http port
        try {
            connect(configuration.getHttpPort());
        } catch (Exception e) {
            needsStart = true;
        }

        if (!needsStart) {
            return;
        }

        try {
            final File workingDirectory = new File(configuration.getDir());
            workingDirectory.mkdirs();
            if (workingDirectory.exists()) {
                FileUtils.deleteOnExit(workingDirectory);
            }

            File openejbHome;
            if (DOWNLOADED != null && DOWNLOADED.getAbsolutePath().startsWith(workingDirectory.getAbsolutePath())) {
                openejbHome = findOpenEJBHome(workingDirectory);
            } else if (configuration.getTomcatVersion() == null || configuration.getTomcatVersion().length() == 0) {
                downloadTomEE(workingDirectory);
                openejbHome = findOpenEJBHome(workingDirectory);
            } else {
                downloadTomcat(workingDirectory, configuration.getTomcatVersion());
                openejbHome = findOpenEJBHome(workingDirectory);
                File webappsOpenEJB = new File(openejbHome, "webapps/openejb");
                webappsOpenEJB.mkdirs();
                downloadOpenEJBWebapp(webappsOpenEJB);

                System.setProperty("catalina.home", openejbHome.getAbsolutePath());
                System.setProperty("catalina.base", openejbHome.getAbsolutePath());
                System.setProperty("openejb.deploymentId.format", "{appId}/{ejbJarId}/{ejbName}");
                Paths paths = new Paths(new File(openejbHome.getAbsolutePath(), "/webapps/openejb"));
                Installer installer = new Installer(paths, true);
                installer.installAll();
            }
            DOWNLOADED = workingDirectory;

            if (openejbHome == null || (!openejbHome.exists())) {
                throw new LifecycleException("Error finding OPENEJB_HOME");
            }

            FileUtils.deleteOnExit(openejbHome);
            Map<String, String> replacements = new HashMap<String, String>();
            replacements.put("8080", String.valueOf(configuration.getHttpPort()));
            replacements.put("8005", String.valueOf(configuration.getStopPort()));
            replace(replacements, new File(openejbHome, "conf/server.xml"));

            write(Thread.currentThread().getContextClassLoader().getResource("default.remote.logging.properties"), new File(openejbHome, "conf/logging.properties"));

            System.setProperty("tomee.http.port", String.valueOf(configuration.getHttpPort()));
            System.setProperty("tomee.shutdown.port", String.valueOf(configuration.getStopPort()));
            System.setProperty("java.naming.provider.url", "http://localhost:" + configuration.getHttpPort() + "/openejb/ejb");
            System.setProperty("connect.tries", "90");
            System.setProperty("server.http.port", String.valueOf(configuration.getHttpPort()));
            System.setProperty("server.shutdown.port", String.valueOf(configuration.getStopPort()));
            System.setProperty("java.opts", "-Xmx512m -Xms256m -XX:PermSize=64m -XX:MaxPermSize=256m -XX:ReservedCodeCacheSize=64m");
            System.setProperty("openejb.home", openejbHome.getAbsolutePath());

            container = new RemoteServer();
            container.start();
        } catch (Exception e) {
            throw new LifecycleException("Unable to start remote container", e);
        }
    }

    private static void write(URL resource, File file) throws IOException {
        if (file.exists()) {
            FileUtils.delete(file);
        }
        InputStream is = resource.openStream();
        OutputStream out = new FileOutputStream(file);
        byte buf[] = new byte[1024];
        int len;
        while ((len = is.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        is.close();
    }

    private File findOpenEJBHome(File directory) {
        File conf = new File(directory, "conf");
        File webapps = new File(directory, "webapps");

        if (conf.exists() && conf.isDirectory() && webapps.exists() && webapps.isDirectory()) {
            return directory;
        }

        for (File file : directory.listFiles()) {
            if (".".equals(file.getName()) || "..".equals(file.getName())) continue;

            File found = findOpenEJBHome(file);
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    protected void downloadTomEE(File catalinaDirectory) throws LifecycleException {
        String artifactName;
        if (configuration.isPlusContainer()) {
            artifactName = "org.apache.openejb:apache-tomee:zip:plus:" + configuration.getOpenejbVersion();
        } else {
            artifactName = "org.apache.openejb:apache-tomee:zip:webprofile:" + configuration.getOpenejbVersion();
        }

        File zipFile = downloadFile(artifactName, null);
        ZipExtractor.unzip(zipFile, catalinaDirectory);
    }

    protected File downloadFile(String artifactName, String altUrl) {
        Artifact artifact = new MavenCache().getArtifact(artifactName, altUrl);
        return artifact.getFile();
    }

    protected void downloadOpenEJBWebapp(File targetDirectory) throws LifecycleException {
        String artifactName;
        if (configuration.isPlusContainer()) {
            artifactName = "org.apache.openejb:openejb-tomcat-plus-webapp:war:" + configuration.getOpenejbVersion();
        } else {
            artifactName = "org.apache.openejb:openejb-tomcat-webapp:war:" + configuration.getOpenejbVersion();
        }

        File zipFile = downloadFile(artifactName, null);
        ZipExtractor.unzip(zipFile, targetDirectory);
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

        File zipFile = downloadFile("org.apache.openejb:tomcat:zip:" + tomcatVersion, source);
        ZipExtractor.unzip(zipFile, catalinaDirectory);
    }

    public void stop() throws LifecycleException {
        // only stop the container if we started it
        if (needsStart) {
            container.stop();
        }
    }

    public void connect(int port) throws Exception {
        Socket socket = new Socket("localhost", port);
        OutputStream out = socket.getOutputStream();
        out.close();
    }

    private void replace(Map<String, String> replacements, File file) throws IOException {
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
        } catch (IOException e) {
            throw e;
        } finally {
            if (reader != null) {
                reader.close();
            }

            if (writer != null) {
                writer.close();
            }
        }
    }

    private File copyToTempFile(File file) throws IOException {
        InputStream is = null;
        OutputStream os = null;

        File tmpFile;
        try {
            tmpFile = File.createTempFile("oejb", ".fil");
            tmpFile.deleteOnExit();

            is = new FileInputStream(file);
            os = new FileOutputStream(tmpFile);

            Installer.copy(is, os);
        } catch (IOException e) {
            throw e;
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
}
