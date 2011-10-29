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

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.openejb.assembler.Deployer;
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

public abstract class TomEEContainer implements DeployableContainer<TomEEConfiguration> {
    protected static final String LOCALHOST = "localhost";
    protected static final String SHUTDOWN_COMMAND = "SHUTDOWN" + Character.toString((char) -1);
    protected TomEEConfiguration configuration;
    protected Map<String, String> moduleIds = new HashMap<String, String>();

    public Class<TomEEConfiguration> getConfigurationClass() {
        return TomEEConfiguration.class;
    }

    public void setup(TomEEConfiguration configuration) {
        this.configuration = configuration;
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
                try {
                    Thread.sleep(2000);
                } catch (Exception e2) {
                    e.printStackTrace();
                }

                waitForShutdown(--tries);
            }
        }
    }

    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription("Servlet 2.5");
    }

    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        try {
            String tmpDir = System.getProperty("java.io.tmpdir");
            File file = new File(tmpDir + File.separator + archive.getName());
            file.deleteOnExit();
            archive.as(ZipExporter.class).exportTo(file, true);

            Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
            properties.setProperty(Context.PROVIDER_URL, "http://" + LOCALHOST + ":" + configuration.getHttpPort() + "/openejb/ejb");
            InitialContext context = new InitialContext(properties);

            Deployer deployer = (Deployer) context.lookup("openejb/DeployerBusinessRemote");
            deployer.deploy(file.getAbsolutePath());

            moduleIds.put(archive.getName(), file.getAbsolutePath());

            HTTPContext httpContext = new HTTPContext(LOCALHOST, configuration.getHttpPort());
            if (archive instanceof WebArchive) {
            	httpContext.add(new Servlet("ArquillianServletRunner", "/" + getArchiveNameWithoutExtension(archive)));
            } else {
            	httpContext.add(new Servlet("ArquillianServletRunner", "/arquillian-protocol"));
            }
            
            // we should probably get all servlets and add them to the context
            return new ProtocolMetaData().addContext(httpContext);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DeploymentException("Unable to deploy", e);
        }
    }

    protected String getArchiveNameWithoutExtension(final Archive<?> archive) {
        final String archiveName = archive.getName();
        final int extensionOffset = archiveName.lastIndexOf('.');
        if (extensionOffset >= 0) {
            return archiveName.substring(0, extensionOffset);
        }
        return archiveName;
    }

    private Servlet getArquillianServlet() {
        Servlet servlet = new Servlet("ArquillianServletRunner", "/");
        return servlet;
    }

    public void undeploy(Archive<?> archive) throws DeploymentException {
        try {
            Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
            properties.setProperty(Context.PROVIDER_URL, "http://" + LOCALHOST + ":" + configuration.getHttpPort() + "/openejb/ejb");
            InitialContext context = new InitialContext(properties);
            String appId = moduleIds.get(archive.getName());
            Deployer deployer = (Deployer) context.lookup("openejb/DeployerBusinessRemote");
            deployer.undeploy(appId);
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
