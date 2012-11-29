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
package org.apache.openejb.arquillian.embedded;

import org.apache.openejb.arquillian.common.Files;
import org.apache.openejb.arquillian.common.TestClassDiscoverer;
import org.apache.openejb.arquillian.common.TomEEContainer;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.config.AdditionalBeanDiscoverer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.tomee.embedded.Configuration;
import org.apache.tomee.embedded.Container;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EmbeddedTomEEContainer extends TomEEContainer<EmbeddedTomEEConfiguration> {

    private static final Map<Archive<?>, File> ARCHIVES = new ConcurrentHashMap<Archive<?>, File>();

    private Container container;

    @Override
    public Class<EmbeddedTomEEConfiguration> getConfigurationClass() {
        return EmbeddedTomEEConfiguration.class;
    }

    @Override
    public void setup(final EmbeddedTomEEConfiguration configuration) {
        super.setup(configuration);
        this.container = new Container();
        this.container.setup(this.convertConfiguration(configuration));
    }

    /*
     * Not exactly as elegant as I'd like. Maybe we could have the EmbeddedServer configuration in openejb-core so all the adapters can use it.
     * Depending on tomee-embedded is fine in this adapter, but less desirable in the others, as we'd get loads of stuff in the classpath we don't need.
     */
    private Configuration convertConfiguration(final EmbeddedTomEEConfiguration tomeeConfiguration) {
    	final Configuration configuration = new Configuration();
    	configuration.setDir(tomeeConfiguration.getDir());
    	configuration.setHttpPort(tomeeConfiguration.getHttpPort());
    	configuration.setStopPort(tomeeConfiguration.getStopPort());
        configuration.setHost(tomeeConfiguration.getHost());
        configuration.setServerXml(tomeeConfiguration.getServerXml());
        configuration.setProperties(tomeeConfiguration.systemPropertiesAsProperties());
        configuration.setQuickSession(tomeeConfiguration.isQuickSession());
		return configuration;
	}

    @Override
    public void start() throws LifecycleException {
        try {
            this.container.start();
            SystemInstance.get().setComponent(AdditionalBeanDiscoverer.class, new TestClassDiscoverer());
        } catch (Exception e) {
            e.printStackTrace();
            throw new LifecycleException("Something went wrong", e);
        }
    }

    @Override
    public void stop() throws LifecycleException {
        try {
            this.container.stop();
        } catch (Exception e) {
            throw new LifecycleException("Unable to stop server", e);
        }
    }

    @Override
    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription("Local");
    }

    @Override
    public ProtocolMetaData deploy(final Archive<?> archive) throws DeploymentException {
    	try {
            /* don't do it since it should be configurable
            final File tempDir = Files.createTempDir();
            final File file = new File(tempDir, name);
            */
            final String name = archive.getName();
            final File file = this.dumpFile(archive);
            ARCHIVES.put(archive, file);
            this.archiveWithTestInfo(archive).as(ZipExporter.class).exportTo(file, true);

            this.container.deploy(name, file);
            final AppInfo info = this.container.getInfo(name);
            final String context = this.getArchiveNameWithoutExtension(archive);

            final HTTPContext httpContext = new HTTPContext(this.configuration.getHost(), this.configuration.getHttpPort());
            httpContext.add(new Servlet("ArquillianServletRunner", "/" + context));
            this.addServlets(httpContext, info);

            return new ProtocolMetaData().addContext(httpContext);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DeploymentException("Unable to deploy", e);
        }
    }

    @Override
    public void undeploy(final Archive<?> archive) throws DeploymentException {
    	try {
            final String name = archive.getName();
            this.container.undeploy(name);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DeploymentException("Unable to undeploy", e);
        }
        final File file = ARCHIVES.remove(archive);
        final File folder = new File(file.getParentFile(), file.getName().substring(0, file.getName().length() - 5));
        if (folder.exists()) {
            Files.delete(folder);
        }
        Files.delete(file);
    }
}
