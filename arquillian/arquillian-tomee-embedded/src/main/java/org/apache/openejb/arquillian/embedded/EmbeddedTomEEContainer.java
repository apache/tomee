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
package org.apache.openejb.arquillian.embedded;

import org.apache.openejb.arquillian.common.ArquillianFilterRunner;
import org.apache.openejb.arquillian.common.Files;
import org.apache.openejb.arquillian.common.TestClassDiscoverer;
import org.apache.openejb.arquillian.common.TomEEContainer;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.config.AdditionalBeanDiscoverer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.tomee.embedded.Configuration;
import org.apache.tomee.embedded.Container;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.web.lifecycle.test.MockHttpSession;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.shrinkwrap.api.Archive;

import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EmbeddedTomEEContainer extends TomEEContainer<EmbeddedTomEEConfiguration> {

    private static final Map<Archive<?>, File> ARCHIVES = new ConcurrentHashMap<Archive<?>, File>();

    private static final Map<String, MockHttpSession> SESSIONS = new ConcurrentHashMap<String, MockHttpSession>();

    private Container container;

    @Inject
    @SuiteScoped
    private InstanceProducer<ClassLoader> classLoader;

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

        configuration.setSsl(tomeeConfiguration.isSsl());
        configuration.setHttpsPort(tomeeConfiguration.getHttpsPort());
        configuration.setKeystoreFile(tomeeConfiguration.getKeystoreFile());
        configuration.setKeystorePass(tomeeConfiguration.getKeystorePass());
        configuration.setKeyAlias(tomeeConfiguration.getKeyAlias());
        configuration.setKeystoreType(tomeeConfiguration.getKeystoreType());
        configuration.setClientAuth(tomeeConfiguration.getClientAuth());
        configuration.setKeyAlias(tomeeConfiguration.getKeyAlias());
        configuration.setSslProtocol(tomeeConfiguration.getSslProtocol());

        return configuration;
    }

    @Override
    public void start() throws LifecycleException {
        try {
            this.container.start();
            SystemInstance.get().setComponent(AdditionalBeanDiscoverer.class, new TestClassDiscoverer());
            // this property is not mandatory by default but depending the protocol it can be relevant so adding it by default
            SystemInstance.get().setProperty("org.apache.openejb.servlet.filters", ArquillianFilterRunner.class.getName() + "=/ArquillianServletRunner");
        } catch (final Exception e) {
            e.printStackTrace();
            throw new LifecycleException("Something went wrong", e);
        }
    }

    @Override
    public void stop() throws LifecycleException {
        try {
            this.container.stop();
        } catch (final Exception e) {
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

            this.container.deploy(name, file);
            final AppInfo info = this.container.getInfo(name);
            final String context = this.getArchiveNameWithoutExtension(archive);

            final HTTPContext httpContext = new HTTPContext(this.configuration.getHost(), this.configuration.getHttpPort());
            httpContext.add(new Servlet("ArquillianServletRunner", "/" + context));
            this.addServlets(httpContext, info);

            startCdiContexts(name); // ensure tests can use request/session scopes even if we don't have a request

            classLoader.set(SystemInstance.get().getComponent(ContainerSystem.class).getAppContext(info.appId).getClassLoader());

            return new ProtocolMetaData().addContext(httpContext);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new DeploymentException("Unable to deploy", e);
        }
    }

    @Override
    public void undeploy(final Archive<?> archive) throws DeploymentException {
        final String name = archive.getName();
        stopCdiContexts(name);
        try {
            this.container.undeploy(name);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new DeploymentException("Unable to undeploy", e);
        }
        final File file = ARCHIVES.remove(archive);
        final File folder = new File(file.getParentFile(), file.getName().substring(0, file.getName().length() - 4));
        if (folder.exists()) {
            Files.delete(folder);
        }
        Files.delete(file);
        if (!configuration.isSingleDumpByArchiveName()) {
            final File parentFile = file.getParentFile();
            final File[] parentChildren = parentFile.listFiles();
            if (parentChildren == null || parentChildren.length == 0) {
                Files.delete(file.getParentFile());
            }
        }
    }

    private void startCdiContexts(final String name) {
        final WebBeansContext wbc = this.container.getAppContexts(name).getWebBeansContext();
        if (wbc.getBeanManagerImpl().isInUse()) {
            final MockHttpSession session = new MockHttpSession();
            wbc.getContextsService().startContext(RequestScoped.class, null);
            wbc.getContextsService().startContext(SessionScoped.class, session);
            wbc.getContextsService().startContext(ConversationScoped.class, null);

            SESSIONS.put(name, session);
        }
    }

    private void stopCdiContexts(final String name) {
        try {
            final HttpSession session = SESSIONS.remove(name);
            if (session != null) {
                final WebBeansContext wbc = container.getAppContexts(container.getInfo(name).appId).getWebBeansContext();
                if (wbc.getBeanManagerImpl().isInUse()) {
                    wbc.getContextsService().startContext(RequestScoped.class, null);
                    wbc.getContextsService().startContext(SessionScoped.class, session);
                    wbc.getContextsService().startContext(ConversationScoped.class, null);
                }
            }
        } catch (final Exception e) {
            // no-op
        }
    }
}
