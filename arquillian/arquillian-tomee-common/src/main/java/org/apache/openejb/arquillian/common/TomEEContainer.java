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

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.DeployerEjb;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Info;
import org.apache.openejb.assembler.classic.ServletInfo;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.loader.Options;
import org.apache.openejb.util.NetworkUtil;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
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
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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

    @Inject
    protected Instance<DeploymentDescription> deployment;

    protected TomEEContainer() {
        this.options = new Options(System.getProperties());
    }

    protected boolean isTestable(final Archive<?> archive, final DeploymentDescription deploymentDescription) {
        return deploymentDescription != null
            && deploymentDescription.isArchiveDeployment()
            && (deploymentDescription.getArchive() == archive || deploymentDescription.getTestableArchive() == archive)
            && deploymentDescription.testable();
    }

    @Override
    public void setup(final Configuration configuration) {
        this.configuration = configuration;

        final Prefixes prefixes = configuration.getClass().getAnnotation(Prefixes.class);

        if (prefixes == null) {
            return;
        }

        final Properties systemProperties = System.getProperties();
        ConfigurationOverrides.apply(configuration, systemProperties, prefixes.value());

        setPorts();

        // with multiple containers we don't want it so let the user eb able to skip it
        if (configuration.getExportConfAsSystemProperty()) {
            //
            // Export the config back out to properties
            //
            for (final Map.Entry<String, Object> entry : new ObjectMap(configuration).entrySet()) {
                for (final String prefix : prefixes.value()) {
                    try {
                        final String property = prefix + "." + entry.getKey();
                        final String value = entry.getValue().toString();

                        LOGGER.log(Level.FINER, String.format("Exporting '%s=%s'", property, value));

                        System.setProperty(property, value);
                    } catch (final Throwable e) {
                        // value cannot be converted to a string
                    }
                }
            }
        }

        ArquillianUtil.preLoadClassesAsynchronously(configuration.getPreloadClasses());
    }

    protected void setPorts() {
        //
        // Set ports if they are unspecified
        //
        final Collection<Integer> randomPorts = new ArrayList<Integer>();
        for (final int i : configuration.portsAlreadySet()) { // ensure we don't use already initialized port (fixed ones)
            randomPorts.add(i);
        }

        for (final Map.Entry<String, Object> entry : new ObjectMap(configuration).entrySet()) {
            if (!entry.getKey().toLowerCase().endsWith("port")) {
                continue;
            }
            try {
                final Object value = entry.getValue();
                int port = new Integer(String.valueOf(value));
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
            } catch (final NumberFormatException mustNotBeAPortConfig) {
                // no-op
            }
        }
        randomPorts.clear();
    }

    private int nextPort(final String portRange, final Collection<Integer> excluded) {
        if (portRange == null || portRange.isEmpty()) {
            int retry = 10;
            while (retry > 0) {
                final int port = NetworkUtil.getNextAvailablePort();
                if (!excluded.contains(port)) {
                    return port;
                }
                retry--;
            }
            throw new IllegalArgumentException("can't find a port available excluding " + excluded);
        }

        if (!portRange.contains("-")) {
            final int port = Integer.parseInt(portRange.trim());
            return NetworkUtil.getNextAvailablePort(new int[]{port});
        }

        final String[] minMax = portRange.trim().split("-");
        final int min = Integer.parseInt(minMax[0]);
        final int max = Integer.parseInt(minMax[1]);
        return NetworkUtil.getNextAvailablePort(min, max, excluded);
    }

    public abstract void start() throws LifecycleException;

    @Override
    public void stop() throws LifecycleException {
        try {
            final Socket socket = new Socket(configuration.getStopHost(), configuration.getStopPort());
            final OutputStream out = socket.getOutputStream();
            out.write((configuration.getStopCommand() + Character.toString((char) 0)).getBytes());

            waitForShutdown(socket, 10);
        } catch (final Exception e) {
            throw new LifecycleException("Unable to stop TomEE", e);
        }
    }

    protected void waitForShutdown(final Socket socket, int tries) {
        try {
            final OutputStream out = socket.getOutputStream();
            out.close();
        } catch (final Exception e) {
            if (tries > 2) {
                Threads.sleep(2000);

                waitForShutdown(socket, --tries);
            }
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (final IOException ignored) {
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
        for (final WebAppInfo webApps : appInfo.webApps) {
            for (final ServletInfo servlet : webApps.servlets) {
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
    public ProtocolMetaData deploy(final Archive<?> archive) throws DeploymentException {
        try {
            final File file = dumpFile(archive);

            final String fileName = file.getName();
            if (fileName.endsWith(".war")) { // ??
                final File extracted = new File(file.getParentFile(), fileName.substring(0, fileName.length() - 4));
                if (extracted.exists()) {
                    extracted.deleteOnExit();
                }
            }

            final AppInfo appInfo;
            final String archiveName = archive.getName();
            try {
                final Properties deployerProperties = getDeployerProperties();
                if (deployerProperties == null) {
                    appInfo = deployer().deploy(file.getAbsolutePath());
                } else {
                    final Properties props = new Properties();
                    props.putAll(deployerProperties);

                    if ("true".equalsIgnoreCase(deployerProperties.getProperty(DeployerEjb.OPENEJB_USE_BINARIES, "false"))) {
                        final byte[] slurpBinaries = IO.slurpBytes(file);
                        props.put(DeployerEjb.OPENEJB_VALUE_BINARIES, slurpBinaries);
                        props.put(DeployerEjb.OPENEJB_PATH_BINARIES, archive.getName());
                    }

                    appInfo = deployer().deploy(file.getAbsolutePath(), props);
                }

                if (appInfo != null) {
                    moduleIds.put(archiveName, new DeployedApp(appInfo.path, file.getParentFile()));
                    Files.deleteOnExit(file); // "i" folder
                } else {
                    LOGGER.severe("appInfo was not found for " + file.getPath() + ", available are: " + apps());
                    throw new OpenEJBException("can't get appInfo");
                }
            } catch (final OpenEJBException re) { // clean up in undeploy needs it
                moduleIds.put(archiveName, new DeployedApp(file.getPath(), file.getParentFile()));
                throw re;
            }

            if (options.get("tomee.appinfo.output", false)) {
                Info.marshal(appInfo);
            }

            final HTTPContext httpContext = new HTTPContext(configuration.getHost(), configuration.getHttpPort());

            // Avoids "inconvertible types" error in windows build
            if (archiveName.endsWith(".war")) {
                httpContext.add(new Servlet("ArquillianServletRunner", "/" + getArchiveNameWithoutExtension(archive)));
            } else if (archiveName.endsWith(".ear") && appInfo.webApps.size() > 0) {
                final String contextRoot = System.getProperty("tomee.arquillian.ear.context", configuration.getWebContextToUseWithEars());
                if (contextRoot != null) {
                    httpContext.add(new Servlet("ArquillianServletRunner", ("/" + contextRoot).replace("//", "/")));
                } else {
                    for (final WebAppInfo web : appInfo.webApps) { // normally a single webapp is supported cause of arquillian resolution
                        httpContext.add(new Servlet("ArquillianServletRunner", ("/" + web.contextRoot).replace("//", "/")));
                    }
                }
            } else {
                httpContext.add(new Servlet("ArquillianServletRunner", "/arquillian-protocol")); // needs another jar to add the fake webapp
            }
            addServlets(httpContext, appInfo);

            return new ProtocolMetaData().addContext(httpContext);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new DeploymentException("Unable to deploy", e);
        }
    }

    protected Properties getDeployerProperties() {
        return null;
    }

    protected File dumpFile(final Archive<?> archive) {
        final String tmpDir = configuration.getAppWorkingDir();
        Files.deleteOnExit(new File(tmpDir));

        File file;
        int i = 0;
        do { // be sure we don't override something existing
            file = new File(tmpDir + File.separator + i++ + File.separator + archive.getName());
        } while (file.getParentFile().exists()); // we will delete the parent (to clean even complicated unpacking)
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            LOGGER.warning("can't create " + file.getParent());
        }

        Files.deleteOnExit(file.getParentFile());

        if (isTestable(archive, deployment.get())) {
            archiveWithTestInfo(archive).as(ZipExporter.class).exportTo(file, true);
        } else {
            archive.as(ZipExporter.class).exportTo(file, true);
        }

        return file;
    }

    private Collection<String> apps() {
        final Collection<String> paths = new ArrayList<String>();
        try {
            final Collection<AppInfo> appInfos = deployer().getDeployedApps();
            for (final AppInfo info : appInfos) {
                paths.add(info.path);
            }
        } catch (final Exception e) { // don't throw an exception just because of this log info
            // no-op
        }
        return paths;
    }

    protected Assignable archiveWithTestInfo(final Archive<?> archive) {
        String name = archive.getName();
        if (name.endsWith(".war") || name.endsWith(".ear")) {
            name = name.substring(0, name.length() - ".war".length());
        }
        return archive.add(
            new StringAsset(testClass.get().getJavaClass().getName() + '#' + name),
            ArchivePaths.create("arquillian-tomee-info.txt"));
    }

    protected Deployer deployer() throws NamingException {
        return lookupDeployerWithRetry(5);
    }

    protected Deployer lookupDeployerWithRetry(final int retry) throws NamingException {
        try {
            final Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
            properties.setProperty(Context.PROVIDER_URL, providerUrl());
            return (Deployer) new InitialContext(properties).lookup("openejb/DeployerBusinessRemote");
        } catch (final RuntimeException ne) { // surely "org.apache.openejb.client.ClientRuntimeException: Invalid response from server: -1"
            if (retry > 1) {
                try { // wait a bit before retrying
                    Thread.sleep(500);
                } catch (final InterruptedException ignored) {
                    // no-op
                }
                return lookupDeployerWithRetry(retry - 1);
            }
            if (Boolean.getBoolean("openejb.arquillian.debug") && retry >= 0) {
                try { // wait a lot to be sure that's not a timing issue
                    Thread.sleep(10000);
                } catch (final InterruptedException ignored) {
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
    public void undeploy(final Archive<?> archive) throws DeploymentException {
        final DeployedApp deployed = moduleIds.remove(archive.getName());
        if (null != deployed) {
            try {
                deployer().undeploy(deployed.path);
            } catch (final Exception e) {
                final String msg = "Unable to undeploy " + archive.getName();
                LOGGER.log(Level.SEVERE, msg, e);
                throw new DeploymentException(msg, e);
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
    }

    @Override
    public void deploy(final Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void undeploy(final Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Not implemented");
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
