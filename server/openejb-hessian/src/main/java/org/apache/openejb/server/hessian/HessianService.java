/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server.hessian;

import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.event.AssemblerAfterApplicationCreated;
import org.apache.openejb.assembler.classic.event.AssemblerBeforeApplicationDestroyed;
import org.apache.openejb.assembler.classic.event.NewEjbAvailableAfterApplicationCreated;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.server.SelfManaging;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.httpd.HttpListenerRegistry;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.proxy.ProxyEJB;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Properties;

public class HessianService implements ServerService, SelfManaging {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_SERVER.createChild("hessian"), HessianService.class);

    private boolean disabled;
    private boolean debug;
    private boolean sendCollectionType;
    private String serializerFactory;
    private String realmName;
    private String virtualHost;
    private String transportGuarantee;
    private String authMethod;

    private HessianRegistry registry;

    private void deploy(final AppInfo app, final Collection<BeanContext> beanContexts) {
        if (disabled) {
            return;
        }

        for (final BeanContext beanContext : beanContexts) {
            final Class<?> remoteItf = beanContext.getBusinessRemoteInterface();
            if (remoteItf == null) {
                continue;
            }

            final ClassLoader old = Thread.currentThread().getContextClassLoader();
            final ClassLoader classLoader = beanContext.getClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);

            try {
                final HessianServer server;
                try {
                    server = new HessianServer(classLoader).debug(debug);
                } catch (final HessianServer.HessianIsMissingException e) {
                    LOGGER.info("Hessian is not available so openejb-hessian will not deploy any service");
                    break;
                }

                if (serializerFactory != null) {
                    try {
                        server.serializerFactory(classLoader.loadClass(serializerFactory).newInstance()).sendCollectionType(sendCollectionType);
                    } catch (final Exception e) {
                        throw new OpenEJBRuntimeException(e);
                    }
                } else {
                    server.sendCollectionType(sendCollectionType);
                }

                server.createSkeleton(ProxyEJB.simpleProxy(beanContext, new Class<?>[]{remoteItf}), remoteItf);

                try {
                    LOGGER.info("Hessian(url=" + registry.deploy(classLoader, server, virtualHost, appName(app, beanContext), authMethod, transportGuarantee, realmName, String.class.cast(beanContext.getDeploymentID())) + ", interface=" + remoteItf.getName() + ")");
                } catch (final URISyntaxException e) {
                    throw new OpenEJBRuntimeException(e);
                }
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
        }
    }

    @Override
    public void start() throws ServiceException {
        SystemInstance.get().addObserver(this);
        SystemInstance.get().setComponent(HessianService.class, this);
        registry = setRegistry();

        final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
        if (assembler != null) {
            for (final AppInfo appInfo : assembler.getDeployedApplications()) {
                deploy(new AssemblerAfterApplicationCreated(appInfo, SystemInstance.get().getComponent(ContainerSystem.class).getAppContext(appInfo.appId), null));
            }
        }
    }

    private HessianRegistry setRegistry() {
        HessianRegistry registry = SystemInstance.get().getComponent(HessianRegistry.class);
        if (registry == null) {
            try { // if tomcat
                HessianService.class.getClassLoader().loadClass("org.apache.catalina.Context");
                registry = new TomcatHessianRegistry();
            } catch (final Throwable t) { // else if embedded
                if (SystemInstance.get().getComponent(HttpListenerRegistry.class) != null) {
                    registry = new HessianRegistryImpl();
                } else {
                    throw new IllegalStateException("openejb-http is missing at classpath");
                }
            }
            SystemInstance.get().setComponent(HessianRegistry.class, registry);
        }
        return registry;
    }

    public HessianRegistry getRegistry() {
        return registry;
    }

    @Override
    public void stop() throws ServiceException {
        SystemInstance.get().removeObserver(this);
    }

    @Override
    public void service(final InputStream in, final OutputStream out) throws ServiceException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void service(final Socket socket) throws ServiceException, IOException {
        throw new UnsupportedOperationException();
    }

    public void newEjbToDeploy(final @Observes NewEjbAvailableAfterApplicationCreated event) {
        deploy(event.getApp(), event.getBeanContexts());
    }

    public void deploy(final @Observes AssemblerAfterApplicationCreated event) {
        final AppInfo appInfo = event.getApp();
        deploy(appInfo, event.getContext().getBeanContexts());
    }

    public void undeploy(@Observes final AssemblerBeforeApplicationDestroyed event) {
        if (disabled) {
            return;
        }

        for (final BeanContext beanContext : event.getContext().getBeanContexts()) {
            final Class<?> remoteItf = beanContext.getBusinessRemoteInterface();
            if (remoteItf == null) {
                continue;
            }

            final String name = String.class.cast(beanContext.getDeploymentID());
            registry.undeploy(virtualHost, appName(event.getApp(), beanContext), name);
            LOGGER.info("Undeployed hessian service " + name);
        }
    }

    public static String appName(final AppInfo app, final BeanContext beanContext) {
        if (!app.webApps.isEmpty()) {
            for (final EjbJarInfo ejbJar : app.ejbJars) {
                for (final EnterpriseBeanInfo bean : ejbJar.enterpriseBeans) {
                    if (bean.ejbName.equals(beanContext.getEjbName())) {
                        if (ejbJar.webapp) {
                            return ejbJar.moduleName;
                        }
                    }
                }
            }
        }
        return app.appId;
    }

    @Override
    public String getName() {
        return "hessian";
    }

    @Override
    public String getIP() {
        return "n/a";
    }

    @Override
    public int getPort() {
        return -1;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(final boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    public boolean isSendCollectionType() {
        return sendCollectionType;
    }

    public void setSendCollectionType(final boolean sendCollectionType) {
        this.sendCollectionType = sendCollectionType;
    }

    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(final String realmName) {
        this.realmName = realmName;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(final String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public String getTransportGuarantee() {
        return transportGuarantee;
    }

    public void setTransportGuarantee(final String transportGuarantee) {
        this.transportGuarantee = transportGuarantee;
    }

    public String getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(final String authMethod) {
        this.authMethod = authMethod;
    }

    @Override
    public void init(final Properties props) throws Exception {
        disabled = Boolean.parseBoolean(props.getProperty("disabled", "false"));
        debug = Boolean.parseBoolean(props.getProperty("debug", "false"));
        sendCollectionType = Boolean.parseBoolean(props.getProperty("sendCollectionType", "false"));
        realmName = props.getProperty("realmName");
        transportGuarantee = props.getProperty("transportGuarantee", "NONE");
        virtualHost = props.getProperty("virtualHost", "localhost");
        authMethod = props.getProperty("authMethod", "NONE");
        serializerFactory = props.getProperty("serializerFactory", null);
    }
}
