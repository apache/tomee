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
package org.apache.openejb.arquillian.common;

import java.io.File;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Info;
import org.apache.openejb.loader.Options;
import org.apache.openejb.util.NetworkUtil;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

public abstract class TomEEContainer<Configuration extends TomEEConfiguration> implements DeployableContainer<Configuration> {
    protected static final Logger LOGGER = Logger.getLogger(TomEEContainer.class.getName());

    protected static final String LOCALHOST = "localhost";
    protected static final String SHUTDOWN_COMMAND = "SHUTDOWN" + Character.toString((char) -1);
    protected Configuration configuration;
    protected Map<String, File> moduleIds = new HashMap<String, File>();
    private final Options options;

    protected TomEEContainer() {
        this.options = new Options(System.getProperties());
    }

    public void setup(Configuration configuration) {
        this.configuration = configuration;

        final Prefixes prefixes = configuration.getClass().getAnnotation(Prefixes.class);

        if (prefixes == null) return;

        //
        // Override the config with system properties
        //
        final ObjectMap map = new ObjectMap(configuration);
        for (String key : map.keySet()) {
            for (String prefix : prefixes.value()) {
                final String property = prefix + "." + key;
                final String value = System.getProperty(property);

                if (value == null) {
                    LOGGER.log(Level.FINE, String.format("Unset '%s'", property));
                    continue;
                }

                try {
                    LOGGER.log(Level.INFO, String.format("Applying override '%s=%s'", property, value));
                    map.put(key, value);
                } catch (Exception e) {
                    try {
                        map.put(key, Integer.parseInt(value)); // we manage String and int so let's try an int
                    } catch (Exception ignored) {
                        LOGGER.log(Level.WARNING, String.format("Override failed '%s=%s'", property, value), e);
                    }
                }
            }
        }
        //
        // Set ports if they are unspecified
        //
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!entry.getKey().toLowerCase().endsWith("port")) continue;
            try {
                Object value = entry.getValue();
                int port = new Integer(value + "");
                if (port <= 0) {
                    port = NetworkUtil.getNextAvailablePort();
                    entry.setValue(port);
                }
            } catch (NumberFormatException mustNotBeAPortConfig) {
            }
        }
        //
        // Export the config back out to properties
        //
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            for (String prefix : prefixes.value()) {
                try {
                    final String property = prefix + "." + entry.getKey();
                    final String value = entry.getValue().toString();

                    LOGGER.log(Level.FINER, String.format("Exporting '%s=%s'", property, value));

                    System.setProperty(property, value);
                } catch (Throwable e) {
                    // value cannot be converted to a string
                }
            }
        }
    }

    public abstract void start() throws LifecycleException;

    public void stop() throws LifecycleException {
        try {
            Socket socket = new Socket(LOCALHOST, configuration.getStopPort());
            OutputStream out = socket.getOutputStream();
            out.write(SHUTDOWN_COMMAND.getBytes());

            waitForShutdown(10);
        } catch (Exception e) {
            throw new LifecycleException("Unable to stop TomEE", e);
        }
    }

    protected void waitForShutdown(int tries) {
        try {

            Socket socket = new Socket(LOCALHOST, configuration.getStopPort());
            OutputStream out = socket.getOutputStream();
            out.close();
        } catch (Exception e) {
            if (tries > 2) {
                Threads.sleep(2000);

                waitForShutdown(--tries);
            }
        }
    }

    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription("Servlet 2.5");
    }

    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        try {
            String tmpDir = configuration.getAppWorkingDir();
            File file, folderFile;
            int i = 0;
            do { // be sure we don't override something existing
                file = new File(tmpDir + File.separator + i++ + File.separator + archive.getName());
                if (file.isDirectory() || !file.getName().endsWith("ar")) {
                	folderFile = file;
                } else {
                	final String name = file.getName();
                	folderFile = new File(file.getParentFile(), name.substring(0, name.length() - 4));
                }
            } while (file.exists() || folderFile.exists()); // we unpack the war/ear and the delete of "i" can fail (on win in particular)
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                LOGGER.warning("can't create " + file.getParent());
            }

            archive.as(ZipExporter.class).exportTo(file, true);

            final AppInfo appInfo = deployer().deploy(file.getAbsolutePath());

            if (options.get("tomee.appinfo.output", false)) {
                Info.marshal(appInfo);
            }

            moduleIds.put(archive.getName(), file);

            final String fileName = file.getName();
            if (fileName.endsWith(".war")) {
                File extracted = new File(file.getParentFile(), fileName.substring(0, fileName.length() - 4));
                if (extracted.exists()) {
                    extracted.deleteOnExit();
                }
            }

            HTTPContext httpContext = new HTTPContext(LOCALHOST, configuration.getHttpPort());
            String arquillianServlet;
            // Avoids "inconvertible types" error in windows build
            final Object object = archive;
            if (object instanceof WebArchive) {
                arquillianServlet = "/" + getArchiveNameWithoutExtension(archive);
            } else {
                arquillianServlet = "/arquillian-protocol";
            }
            httpContext.add(new Servlet("ArquillianServletRunner", arquillianServlet));

            // we should probably get all servlets and add them to the context
            return new ProtocolMetaData().addContext(httpContext);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DeploymentException("Unable to deploy", e);
        }
    }

    protected Deployer deployer() throws NamingException {
        final Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
        properties.setProperty(Context.PROVIDER_URL, "http://" + LOCALHOST + ":" + configuration.getHttpPort() + "/tomee/ejb");
        return (Deployer) new InitialContext(properties).lookup("openejb/DeployerBusinessRemote");
    }

    protected String getArchiveNameWithoutExtension(final Archive<?> archive) {
        final String archiveName = archive.getName();
        final int extensionOffset = archiveName.lastIndexOf('.');
        if (extensionOffset >= 0) {
            return archiveName.substring(0, extensionOffset);
        }
        return archiveName;
    }

    public void undeploy(Archive<?> archive) throws DeploymentException {
        try {
            final File file = moduleIds.get(archive.getName());
            deployer().undeploy(file.getAbsolutePath());
            Files.delete(file.getParentFile()); // "i" folder
        } catch (Exception e) {
            e.printStackTrace();
            throw new DeploymentException("Unable to undeploy", e);
        }
    }

    public void deploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void undeploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Not implemented");
    }
}
