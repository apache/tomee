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
package org.apache.openejb.arquillian.openejb;

import org.apache.openejb.AppContext;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.arquillian.common.ArquillianUtil;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.OpenEjbConfigurationFactory;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentFilterable;
import org.apache.openejb.core.LocalInitialContext;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.web.LightweightWebAppBuilder;
import org.apache.webbeans.web.lifecycle.test.MockHttpSession;
import org.apache.webbeans.web.lifecycle.test.MockServletContext;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.context.annotation.DeploymentScoped;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.transaction.impl.configuration.TransactionConfiguration;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.openejb.cdi.ScopeHelper.startContexts;
import static org.apache.openejb.cdi.ScopeHelper.stopContexts;

public class OpenEJBDeployableContainer implements DeployableContainer<OpenEJBConfiguration> {
    private static final Properties PROPERTIES = new Properties();

    static {
        // global properties
        PROPERTIES.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        PROPERTIES.setProperty(LocalInitialContext.ON_CLOSE, LocalInitialContext.Close.DESTROY.name());
        PROPERTIES.setProperty(DeploymentFilterable.DEPLOYMENTS_CLASSPATH_PROPERTY, "false");
        try {
            OpenEJBDeployableContainer.class.getClassLoader().loadClass("org.apache.openejb.server.ServiceManager");
            PROPERTIES.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        } catch (Exception e) {
            // ignored
        }
    }

    // config
    private Properties properties;

    // system
    private Assembler assembler;
    private InitialContext initialContext;
    private ConfigurationFactory configurationFactory;
    private Collection<Archive<?>> containerArchives;

    // suite
    @Inject
    @DeploymentScoped
    private InstanceProducer<AppInfo> appInfoProducer;

    @Inject
    @DeploymentScoped
    private InstanceProducer<AppContext> appContextProducer;

    @Inject
    @SuiteScoped
    private InstanceProducer<Context> contextProducer;

    @Inject
    @DeploymentScoped
    private InstanceProducer<ServletContext> servletContextProducer;

    @Inject
    @DeploymentScoped
    private InstanceProducer<HttpSession> sessionProducer;

    @Inject
    @SuiteScoped
    private InstanceProducer<ClassLoader> classLoader;

    @Inject
    private Instance<ServletContext> servletContext;

    @Inject
    private Instance<HttpSession> session;

    @Inject
    private Instance<AppInfo> info;

    @Inject
    private Instance<AppContext> appContext;

    @Inject
    private Instance<TestClass> testClass;

    @Inject
    @ApplicationScoped
    private InstanceProducer<TransactionConfiguration> txConfig;

    @Override
    public Class<OpenEJBConfiguration> getConfigurationClass() {
        return OpenEJBConfiguration.class;
    }

    @Override
    public void setup(final OpenEJBConfiguration openEJBConfiguration) {
        properties = new Properties();

        final ByteArrayInputStream bais = new ByteArrayInputStream(openEJBConfiguration.getProperties().getBytes());
        try {
            properties.load(bais);
        } catch (IOException e) {
            throw new OpenEJBRuntimeException(e);
        } finally {
            IO.close(bais);
        }

        for (Map.Entry<Object, Object> defaultKey : PROPERTIES.entrySet()) {
            final String key = defaultKey.getKey().toString();
            if (!properties.containsKey(key)) {
                properties.setProperty(key, defaultKey.getValue().toString());
            }
        }
    }

    @Override
    public void start() throws LifecycleException {
        try {
            initialContext = new InitialContext(properties);
        } catch (NamingException e) {
            throw new LifecycleException("can't start the OpenEJB container", e);
        }

        assembler = SystemInstance.get().getComponent(Assembler.class);
        configurationFactory = (ConfigurationFactory) SystemInstance.get().getComponent(OpenEjbConfigurationFactory.class);

        if ("true".equalsIgnoreCase(PROPERTIES.getProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE))
                && SystemInstance.get().getComponent(WebAppBuilder.class) == null) {
            SystemInstance.get().setComponent(WebAppBuilder.class, new LightweightWebAppBuilder());
        }

        contextProducer.set(initialContext);

        containerArchives = ArquillianUtil.toDeploy(properties);
        for (Archive<?> archive : containerArchives) {
            try {
                quickDeploy(archive, testClass.get());
            } catch (DeploymentException e) {
                Logger.getLogger(OpenEJBDeployableContainer.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    @Override
    public ProtocolMetaData deploy(final Archive<?> archive) throws DeploymentException {
        try {
            final DeploymentInfo info = quickDeploy(archive, testClass.get());

            servletContextProducer.set(info.appServletContext);
            sessionProducer.set(info.appSession);
            appInfoProducer.set(info.appInfo);
            appContextProducer.set(info.appCtx);
            classLoader.set(info.appCtx.getClassLoader());
        } catch (Exception e) {
            throw new DeploymentException("can't deploy " + archive.getName(), e);
        }

        return new ProtocolMetaData();
    }

    private DeploymentInfo quickDeploy(final Archive<?> archive, final TestClass testClass) throws DeploymentException {
        try {
            final AppModule module = OpenEJBArchiveProcessor.createModule(archive, testClass);
            final AppInfo appInfo = configurationFactory.configureApplication(module);
            final AppContext appCtx = assembler.createApplication(appInfo, module.getClassLoader());

            final ServletContext appServletContext = new MockServletContext();
            final HttpSession appSession = new MockHttpSession();

            startContexts(appCtx.getWebBeansContext().getContextsService(), appServletContext, appSession);

            return new DeploymentInfo(appServletContext, appSession, appInfo, appCtx);
        } catch (Exception e) {
            throw new DeploymentException("can't deploy " + archive.getName(), e);
        }
    }

    @Override
    public void undeploy(final Archive<?> archive) throws DeploymentException {
        // reset classloader for next text
        // otherwise if it was closed something can fail
        classLoader.set(OpenEJBDeployableContainer.class.getClassLoader());

        if (appContext.get() == null) {
            return;
        }

        final ClassLoader cl = appContext.get().getClassLoader();
        if (cl instanceof SWClassLoader) {
            ((SWClassLoader) cl).close();
        }

        try {
            assembler.destroyApplication(info.get().path);
            stopContexts(appContext.get().getWebBeansContext().getContextsService(), servletContext.get(), session.get());
        } catch (Exception e) {
            throw new DeploymentException("can't undeploy " + archive.getName(), e);
        }
    }

    @Override
    public void stop() throws LifecycleException {
        ArquillianUtil.undeploy(this, containerArchives);

        try {
            if (initialContext != null) {
                initialContext.close();
            }
        } catch (NamingException e) {
            throw new LifecycleException("can't close the OpenEJB container", e);
        } finally {
            OpenEJB.destroy();
        }
    }

    @Override
    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription("Local");
    }

    @Override
    public void deploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void undeploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException();
    }

    private static class DeploymentInfo {
        public final ServletContext appServletContext;
        public final HttpSession appSession;
        public final AppInfo appInfo;
        public final AppContext appCtx;

        private DeploymentInfo(ServletContext appServletContext, HttpSession appSession, AppInfo appInfo, AppContext appCtx) {
            this.appServletContext = appServletContext;
            this.appSession = appSession;
            this.appInfo = appInfo;
            this.appCtx = appCtx;
        }
    }
}
