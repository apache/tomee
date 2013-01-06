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

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Info;
import org.apache.openejb.assembler.classic.ServletInfo;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.loader.Options;
import org.apache.openejb.util.NetworkUtil;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Assignable;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.Character;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class TomEEContainer<Configuration extends TomEEConfiguration> implements DeployableContainer<Configuration> {
    protected static final Logger LOGGER = Logger.getLogger(TomEEContainer.class.getName());

    protected Configuration configuration;
    protected Map<String, DeployedApp> moduleIds = new HashMap<String, DeployedApp>();
    private final Options options;

    @Inject
    private Instance<TestClass> testClass;

    protected TomEEContainer() {
        this.options = new Options(System.getProperties());
    }

    @Override
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
                        map.put(key, Integer.parseInt(value)); // we manage String and int and boolean so let's try an int
                    } catch (Exception ignored) {
                        try {
                            map.put(key, Boolean.parseBoolean(value)); // idem let's try a boolean
                        } catch (Exception ignored2) {
                            LOGGER.log(Level.WARNING, String.format("Override failed '%s=%s'", property, value), e);
                        }
                    }
                }
            }
        }

        setPorts();

        // with multiple containers we don't want it so let the user eb able to skip it
        if (configuration.getExportConfAsSystemProperty()) {
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
    }

    protected void setPorts() {
        //
        // Set ports if they are unspecified
        //
        final Collection<Integer> randomPorts = new ArrayList<Integer>();
        for (int i : configuration.portsAlreadySet()) { // ensure we don't use already initialized port (fixed ones)
            randomPorts.add(i);
        }

        final ObjectMap map = new ObjectMap(configuration);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!entry.getKey().toLowerCase().endsWith("port")) continue;
            try {
                Object value = entry.getValue();
                int port = new Integer(value + "");
                if (port <= 0) {
                    int retry = 0;
                    do { // nextPort can in some case returns twice the same port since it doesn't hold the port
                        if (retry++ == Integer.MAX_VALUE) { // really too much, just some protection over infinite loop
                            break;
                        }

                        // ports already set != random port if some port are forced
                        port = nextPort(configuration.getPortRange(), randomPorts);
                    } while (randomPorts.contains(port));

                    entry.setValue(port);
                    randomPorts.add(port);
                }
            } catch (NumberFormatException mustNotBeAPortConfig) {
                // no-op
            }
        }
        randomPorts.clear();
    }

    private int nextPort(final String portRange, final Collection<Integer> excluded) {
        if (portRange == null || portRange.isEmpty()) {
            int retry = 10;
            while (retry > 0) {
                int port = NetworkUtil.getNextAvailablePort();
                if (!excluded.contains(port)) {
                    return port;
                }
                retry--;
            }
            throw new IllegalArgumentException("can't find a port available excluding " + excluded);
        }

        if (!portRange.contains("-")) {
            int port = Integer.parseInt(portRange.trim());
            return NetworkUtil.getNextAvailablePort(new int[]{port});
        }

        final String[] minMax = portRange.trim().split("-");
        int min = Integer.parseInt(minMax[0]);
        int max = Integer.parseInt(minMax[1]);
        return NetworkUtil.getNextAvailablePort(min, max, excluded);
    }

    public abstract void start() throws LifecycleException;

    @Override
    public void stop() throws LifecycleException {
        try {
            Socket socket = new Socket(configuration.getStopHost(), configuration.getStopPort());
            OutputStream out = socket.getOutputStream();
            out.write((configuration.getStopCommand() + Character.toString((char) 0)).getBytes());

            waitForShutdown(socket, 10);
        } catch (Exception e) {
            throw new LifecycleException("Unable to stop TomEE", e);
        }
    }

    protected void waitForShutdown(Socket socket, int tries) {
        try {
            OutputStream out = socket.getOutputStream();
            out.close();
        } catch (Exception e) {
            if (tries > 2) {
                Threads.sleep(2000);

                waitForShutdown(socket, --tries);
            }
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                    // no-op
                }
            }
        }
    }

    @Override
    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription("Servlet 2.5");
    }

    public void addServlets(final HTTPContext httpContext, final AppInfo appInfo) {
        for (WebAppInfo webApps : appInfo.webApps) {
            for (ServletInfo servlet : webApps.servlets) {
                // weird but arquillian url doesn't match the servlet url but its context
                String clazz = servlet.servletClass;
                if (clazz == null) {
                    clazz = servlet.servletName;
                    if (clazz == null) {
                        continue;
                    }
                }

                httpContext.add(new Servlet(clazz, webApps.contextRoot));
                /*
                for (String mapping : servlet.mappings) {
                    httpContext.add(new Servlet(servlet.servletClass, startWithSlash(uniqueSlash(webApps.contextRoot, mapping))));

                }
                */
            }
        }
    }

    @Override
    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        try {
            final File file = dumpFile(archive);

            final String fileName = file.getName();
            if (fileName.endsWith(".war")) { // ??
                File extracted = new File(file.getParentFile(), fileName.substring(0, fileName.length() - 4));
                if (extracted.exists()) {
                    extracted.deleteOnExit();
                }
            }

            final AppInfo appInfo;
            try {
                appInfo = deployer().deploy(file.getAbsolutePath());
                if (appInfo != null) {
                    moduleIds.put(archive.getName(), new DeployedApp(appInfo.path, file.getParentFile()));
                    Files.deleteOnExit(file); // "i" folder
                } else {
                    LOGGER.severe("appInfo was not found for " + file.getPath() + ", available are: " + apps());
                    throw new OpenEJBException("can't get appInfo");
                }
            } catch (OpenEJBException re) { // clean up in undeploy needs it
                moduleIds.put(archive.getName(), new DeployedApp(file.getPath(), file.getParentFile()));
                throw re;
            }

            if (options.get("tomee.appinfo.output", false)) {
                Info.marshal(appInfo);
            }

            HTTPContext httpContext = new HTTPContext(configuration.getHost(), configuration.getHttpPort());

            String arquillianServlet;
            // Avoids "inconvertible types" error in windows build
            final Object object = archive;
            if (object instanceof WebArchive) {
                arquillianServlet = "/" + getArchiveNameWithoutExtension(archive);
            } else {
                arquillianServlet = "/arquillian-protocol";
            }
            httpContext.add(new Servlet("ArquillianServletRunner", arquillianServlet));
            addServlets(httpContext, appInfo);

            return new ProtocolMetaData().addContext(httpContext);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DeploymentException("Unable to deploy", e);
        }
    }

    protected File dumpFile(final Archive<?> archive) {
        String tmpDir = configuration.getAppWorkingDir();
        Files.deleteOnExit(new File(tmpDir));

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
        } while (file.getParentFile().exists()); // we will delete the parent (to clean even complicated unpacking)
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            LOGGER.warning("can't create " + file.getParent());
        }

        Files.deleteOnExit(file.getParentFile());

        archiveWithTestInfo(archive).as(ZipExporter.class).exportTo(file, true);

        return file;
    }

    private Collection<String> apps() {
        final Collection<String> paths = new ArrayList<String>();
        try {
            final Collection<AppInfo> appInfos = deployer().getDeployedApps();
            for (AppInfo info : appInfos) {
                paths.add(info.path);
            }
        } catch (Exception e) { // don't throw an exception just because of this log info
            // no-op
        }
        return paths;
    }

    protected Assignable archiveWithTestInfo(final Archive<?> archive) {
        return archive.add(new StringAsset(testClass.get().getJavaClass().getName()), ArchivePaths.create("arquillian-tomee-info.txt"));
    }

    protected Deployer deployer() throws NamingException {
        return lookupDeployerWithRetry(5);
    }

    protected Deployer lookupDeployerWithRetry(int retry) throws NamingException {
        try {
            final Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
            properties.setProperty(Context.PROVIDER_URL, providerUrl());
            return (Deployer) new InitialContext(properties).lookup("openejb/DeployerBusinessRemote");
        } catch (RuntimeException ne) { // surely "org.apache.openejb.client.ClientRuntimeException: Invalid response from server: -1"
            if (retry > 1) {
                try { // wait a bit before retrying
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {
                    // no-op
                }
                return lookupDeployerWithRetry(retry - 1);
            }
            if (Boolean.getBoolean("openejb.arquillian.debug") && retry >= 0) {
                try { // wait a lot to be sure that's not a timing issue
                    Thread.sleep(10000);
                } catch (InterruptedException ignored) {
                    // no-op
                }
                return lookupDeployerWithRetry(-1);
            }
            throw ne;
        }
    }

    protected String providerUrl() {
        return "http://" + configuration.getHost() + ":" + configuration.getHttpPort() + "/tomee/ejb";
    }

    protected String getArchiveNameWithoutExtension(final Archive<?> archive) {
        final String archiveName = archive.getName();
        final int extensionOffset = archiveName.lastIndexOf('.');
        if (extensionOffset >= 0) {
            return archiveName.substring(0, extensionOffset);
        }
        return archiveName;
    }

    @Override
    public void undeploy(Archive<?> archive) throws DeploymentException {
        final DeployedApp deployed = moduleIds.get(archive.getName());
        try {
            deployer().undeploy(deployed.path);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DeploymentException("Unable to undeploy " + archive.getName(), e);
        } finally {
            LOGGER.info("cleaning " + deployed.file.getAbsolutePath());
            Files.tryTodelete(deployed.file); // "i" folder

            final File pathFile = new File(deployed.path);
            if (!deployed.path.equals(deployed.file.getAbsolutePath()) && pathFile.exists()) {
                LOGGER.info("cleaning " + pathFile);
                Files.delete(pathFile);
            }
        }
    }

    @Override
    public void deploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void undeploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Not implemented");
    }

    private static String startWithSlash(final String s) {
        if (s == null) {
            return "/";
        }
        if (s.startsWith("/")) {
            return s;
        }
        return "/" + s;
    }

    private static String uniqueSlash(final String contextRoot, final String mapping) {
        boolean ctxSlash = contextRoot.endsWith("/");
        boolean mappingSlash = mapping.startsWith("/");
        if (ctxSlash && mappingSlash) {
            return contextRoot.substring(0, contextRoot.length() - 1) + mapping;
        }
        if ((!ctxSlash && mappingSlash) || (ctxSlash && !mappingSlash)) {
            return contextRoot + mapping;
        }
        return contextRoot + "/" + mapping;
    }

    public static class DeployedApp {
        public final File file;
        public final String path;

        public DeployedApp(final String path, final File file) {
            this.path = path;
            this.file = file;
        }
    }
}
