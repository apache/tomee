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
package org.apache.openejb.arquillian.openejb;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.ModuleTestContext;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.arquillian.common.ArquillianUtil;
import org.apache.openejb.arquillian.common.TestObserver;
import org.apache.openejb.arquillian.openejb.server.ServiceManagers;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ClassListInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfigurationFactory;
import org.apache.openejb.assembler.classic.ServletInfo;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentFilterable;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.core.LocalInitialContext;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.httpd.session.SessionManager;
import org.apache.openejb.web.LightweightWebAppBuilder;
import org.apache.webbeans.web.lifecycle.test.MockHttpSession;
import org.apache.webbeans.web.lifecycle.test.MockServletContext;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.arquillian.container.spi.context.annotation.DeploymentScoped;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
        } catch (final Exception e) {
            // ignored
        }
    }

    private static final ConcurrentMap<String, DeploymentInfo> DEPLOYMENT_INFO = new ConcurrentHashMap<String, DeploymentInfo>();
    public static final AppContext NO_APP_CTX = new AppContext(null, SystemInstance.get(), null, null, null, false);

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
    @DeploymentScoped
    private InstanceProducer<Closeables> closeablesProducer;

    @Inject
    @SuiteScoped
    private InstanceProducer<TestObserver.ClassLoaders> classLoader;

    @Inject
    private Instance<Closeables> closeables;

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

    private OpenEJBConfiguration configuration;

    @Override
    public Class<OpenEJBConfiguration> getConfigurationClass() {
        return OpenEJBConfiguration.class;
    }

    @Override
    public void setup(final OpenEJBConfiguration openEJBConfiguration) {
        properties = new Properties();
        configuration = openEJBConfiguration;

        final ByteArrayInputStream bais = new ByteArrayInputStream(openEJBConfiguration.getProperties().getBytes());
        try {
            properties.load(bais);
        } catch (final IOException e) {
            throw new OpenEJBRuntimeException(e);
        } finally {
            IO.close(bais);
        }

        for (final Map.Entry<Object, Object> defaultKey : PROPERTIES.entrySet()) {
            final String key = defaultKey.getKey().toString();
            if (!properties.containsKey(key)) {
                properties.setProperty(key, defaultKey.getValue().toString());
            }
        }

        ArquillianUtil.preLoadClassesAsynchronously(openEJBConfiguration.getPreloadClasses());
    }

    @Override
    public void start() throws LifecycleException {
        try {
            initialContext = new InitialContext(properties);
        } catch (final NamingException e) {
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
        final Closeables globalScopeCloseables = new Closeables();
        SystemInstance.get().setComponent(Closeables.class, globalScopeCloseables);
        for (final Archive<?> archive : containerArchives) {
            try {
                quickDeploy(archive, testClass.get(), globalScopeCloseables);
            } catch (final DeploymentException e) {
                Logger.getLogger(OpenEJBDeployableContainer.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    @Override
    public ProtocolMetaData deploy(final Archive<?> archive) throws DeploymentException {
        final DeploymentInfo info;
        try {
            final Closeables cl = new Closeables();
            closeablesProducer.set(cl);
            info = quickDeploy(archive, testClass.get(), cl);

            // try to switch module context jndi to let test use java:module naming
            // we could put the managed bean in the war but then test class should respect all the
            // container rules (CDI) which is not the case with this solution
            if (archive.getName().endsWith(".war")) {
                final List<BeanContext> beanContexts = info.appCtx.getBeanContexts();
                if (beanContexts.size() > 1) {
                    final Iterator<BeanContext> it = beanContexts.iterator();
                    while (it.hasNext()) {
                        final BeanContext next = it.next();
                        if (ModuleTestContext.class.isInstance(next.getModuleContext()) && BeanContext.Comp.class != next.getBeanClass()) {
                            for (final BeanContext b : beanContexts) {
                                if (b.getModuleContext() != next.getModuleContext()) {
                                    ModuleTestContext.class.cast(next.getModuleContext())
                                            .setModuleJndiContextOverride(b.getModuleContext().getModuleJndiContext());
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
            }

            servletContextProducer.set(info.appServletContext);
            sessionProducer.set(info.appSession);
            appInfoProducer.set(info.appInfo);
            appContextProducer.set(info.appCtx);
            final ClassLoader loader = info.appCtx.getWebContexts().isEmpty() ? info.appCtx.getClassLoader() : info.appCtx.getWebContexts().iterator().next().getClassLoader();
            final ClassLoader classLoader = loader == null ? info.appCtx.getClassLoader() : loader;

            TestObserver.ClassLoaders classLoaders = this.classLoader.get();
            if (classLoaders == null) {
                classLoaders = new TestObserver.ClassLoaders();
                this.classLoader.set(classLoaders);
            }
            classLoaders.register(archive.getName(), classLoader);
        } catch (final Exception e) {
            throw new DeploymentException("can't deploy " + archive.getName(), e);
        }

        // if service manager is started allow @ArquillianResource URL injection
        if (PROPERTIES.containsKey(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE)) {
            final ProtocolMetaData metaData = ServiceManagers.protocolMetaData(appInfoProducer.get());
            HTTPContext http = null;
            for (final WebAppInfo webapp : info.appInfo.webApps) {
                for (final ServletInfo servletInfo : webapp.servlets) {
                    if (http == null) {
                        http = HTTPContext.class.cast(metaData.getContexts().iterator().next());
                        http.add(new Servlet(servletInfo.servletName, webapp.contextRoot));
                    }
                }
                for (final ClassListInfo classListInfo : webapp.webAnnotatedClasses) {
                    for (final String path : classListInfo.list) {
                        if (!path.contains("!")) {
                            continue;
                        }
                        if (http == null) {
                            http = HTTPContext.class.cast(metaData.getContexts().iterator().next());
                        }
                        http.add(new Servlet(path.substring(path.lastIndexOf('!') + 2).replace(".class", "").replace("/", "."), webapp.contextRoot));
                    }
                }
            }
            if (metaData != null) {
                return metaData;
            }
        }
        return new ProtocolMetaData();
    }

    private DeploymentInfo quickDeploy(final Archive<?> archive, final TestClass testClass, final Closeables cls) throws DeploymentException {
        final String name = archive.getName();
        DeploymentInfo info = DEPLOYMENT_INFO.get(name);
        if (info == null) {
            try {
                final AppModule module = OpenEJBArchiveProcessor.createModule(archive, testClass, cls);
                final AppInfo appInfo = configurationFactory.configureApplication(module);

                final WebAppBuilder webAppBuilder = SystemInstance.get().getComponent(WebAppBuilder.class);
                final boolean isEmbeddedWebAppBuilder = webAppBuilder != null && LightweightWebAppBuilder.class.isInstance(webAppBuilder);
                if (isEmbeddedWebAppBuilder) {
                    // for now we keep the same classloader, open to discussion if we should recreate it, not sure it does worth it
                    final LightweightWebAppBuilder lightweightWebAppBuilder = LightweightWebAppBuilder.class.cast(webAppBuilder);
                    for (final WebModule w : module.getWebModules()) {
                        final String moduleId = w.getModuleId();
                        lightweightWebAppBuilder.setClassLoader(moduleId, w.getClassLoader());
                        cls.add(new Closeable() {
                            @Override
                            public void close() throws IOException {
                                lightweightWebAppBuilder.removeClassLoader(moduleId);
                            }
                        });
                    }
                }
                final AppContext appCtx = assembler.createApplication(appInfo, module.getClassLoader());
                if (isEmbeddedWebAppBuilder && PROPERTIES.containsKey(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE) && !appCtx.getWebContexts().isEmpty()) {
                    cls.add(new Closeable() {
                        @Override
                        public void close() throws IOException {
                            try {
                                final SessionManager sessionManager = SystemInstance.get().getComponent(SessionManager.class);
                                if (sessionManager != null) {
                                    for (final WebContext web : appCtx.getWebContexts()) {
                                        sessionManager.destroy(web);
                                    }
                                }
                            } catch (final Throwable e) {
                                // no-op
                            }
                        }
                    });
                }

                final ServletContext appServletContext = new MockServletContext();
                final HttpSession appSession = new MockHttpSession();

                if (configuration.isStartDefaultScopes() && appCtx.getWebBeansContext() != null) {
                    startContexts(appCtx.getWebBeansContext().getContextsService(), appServletContext, appSession);
                }

                info = new DeploymentInfo(appServletContext, appSession, appInfo, appCtx);
                if (configuration.isSingleDeploymentByArchiveName(name)) {
                    DEPLOYMENT_INFO.putIfAbsent(name, info);
                }
            } catch (final Exception e) {
                throw new DeploymentException("can't deploy " + name, e);
            }
        }
        return info;
    }

    @Override
    public void undeploy(final Archive<?> archive) throws DeploymentException {
        final Closeables cl = closeables.get();
        if (cl != null) {
            try {
                cl.close();
            } catch (final IOException e) {
                // no-op
            }
        }

        // reset classloader for next text
        // otherwise if it was closed something can fail
        final TestObserver.ClassLoaders classLoaders = classLoader.get();
        if (classLoaders != null) {
            classLoaders.unregister(archive.getName());
        }

        final AppContext ctx = appContext.get();
        if (ctx == null) {
            return;
        } else {
            appContextProducer.set(NO_APP_CTX); // release all references of the previous one - classloaders whatever arquillian Instance impl is etc
        }

        try {
            if (!configuration.isSingleDeploymentByArchiveName(archive.getName())) {
                assembler.destroyApplication(info.get().path);
            }
            if (configuration.isStartDefaultScopes() && ctx.getWebBeansContext() != null) {
                stopContexts(ctx.getWebBeansContext().getContextsService(), servletContext.get(), session.get());
            }
        } catch (final Exception e) {
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
            Closeables closeables = SystemInstance.get().getComponent(Closeables.class);
            if (closeables != null) {
                closeables.close();
            }
        } catch (final NamingException e) {
            throw new LifecycleException("can't close the OpenEJB container", e);
        } catch (final IOException e) {
            // no-op: close() of classloaders, not a big deal at this moment
        } finally {
            OpenEJB.destroy();
        }
    }

    @Override
    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription("Local");
    }

    @Override
    public void deploy(final Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void undeploy(final Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException();
    }

    private static final class DeploymentInfo {
        public final ServletContext appServletContext;
        public final HttpSession appSession;
        public final AppInfo appInfo;
        public final AppContext appCtx;

        private DeploymentInfo(final ServletContext appServletContext, final HttpSession appSession, final AppInfo appInfo, final AppContext appCtx) {
            this.appServletContext = appServletContext;
            this.appSession = appSession;
            this.appInfo = appInfo;
            this.appCtx = appCtx;
        }
    }
}
