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
package org.apache.openejb.cdi.internal;

import org.apache.openejb.AppContext;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.cdi.OpenEJBLifecycle;
import org.apache.openejb.cdi.api.Hessian;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.hessian.HessianRegistry;
import org.apache.openejb.server.hessian.HessianServer;
import org.apache.openejb.server.hessian.HessianService;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.AppFinder;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.container.InjectableBeanManager;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.BeforeShutdown;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessBean;
import jakarta.enterprise.inject.spi.ProcessSessionBean;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

public class HessianExtension implements Extension {
    private static final Logger LOGGER = Logger.getLogger(HessianExtension.class.getName());

    private final Collection<Deployment> toDeploy = new ArrayList<>();
    private final Collection<DeployedEndpoint> deployed = new ArrayList<>();

    private AppInfo appInfo;

    protected void init(final @Observes BeforeBeanDiscovery beforeBeanDiscovery) {
        appInfo = OpenEJBLifecycle.CURRENT_APP_INFO.get();
        if (appInfo == null) {
            throw new IllegalStateException("Without OpenEJBLifecycle this Extension can't work correctly");
        }
    }

    protected <X> void findHessianWebServices(final @Observes ProcessBean<X> processBean) {
        if (ProcessSessionBean.class.isInstance(processBean)) {
            return;
        }

        final Bean<X> bean = processBean.getBean();
        final Class<?> beanClass = bean.getBeanClass();
        for (final Class<?> itf : beanClass.getInterfaces()) {
            final Hessian hessian = itf.getAnnotation(Hessian.class);
            final String key = "openejb.hessian." + beanClass.getName() + "_" + itf.getName() + ".path";
            final String path = appInfo.properties.getProperty(key, SystemInstance.get().getProperty(key));
            if (hessian != null || path != null) {
                toDeploy.add(new Deployment(itf, path, bean));
            }
        }
    }

    protected void deploy(final @Observes AfterDeploymentValidation afterDeploymentValidation, final BeanManager bm) {
        final HessianService service = SystemInstance.get().getComponent(HessianService.class);
        if (service == null) {
            LOGGER.severe("HessianService not yet started, no hessian CDI webservices will be deployed");
            return;
        }

        final HessianRegistry registry = service.getRegistry();

        final String appName = findAppName();
        for (final Deployment deployment : toDeploy) {
            final Hessian hessian = deployment.itf.getAnnotation(Hessian.class);
            final HessianServer server;
            try {
                server = new HessianServer(deployment.bean.getBeanClass().getClassLoader());
            } catch (final HessianServer.HessianIsMissingException e) {
                LOGGER.info("Hessian is not available so openejb-hessian will not deploy any service");
                break;
            }

            try {
                if (hessian != null && hessian.serializerFactory() != Object.class && !hessian.serializerFactory().isInstance(server.getSerializerFactory())) {
                    server.serializerFactory(hessian.serializerFactory().newInstance());
                }
            } catch (final Exception e) {
                throw new OpenEJBRuntimeException(e);
            }
            if (hessian != null) {
                server.sendCollectionType(hessian.sendCollectionType());
            }
            if (Dependent.class.equals(deployment.bean.getScope())) {
                LOGGER.warning("@Dependent can lead to memory leaks ATM");
            }
            server.createSkeleton(bm.getReference(deployment.bean, deployment.itf, null), deployment.itf);

            final String name = getName(deployment.path, deployment.itf);
            try {
                LOGGER.info("Hessian(url=" + registry.deploy(deployment.itf.getClassLoader(), server,
                    service.getVirtualHost(), appName,
                    service.getAuthMethod(), service.getTransportGuarantee(),
                    service.getRealmName(), name) + ", interface=" + name + ")");
                deployed.add(new DeployedEndpoint(appName, name));
            } catch (final URISyntaxException e) {
                throw new OpenEJBRuntimeException(e);
            }
        }
        toDeploy.clear();
    }

    private String findAppName() {
        if (appInfo.webAppAlone) {
            return appInfo.webApps.iterator().next().contextRoot;
        }

        for (final AppContext app : SystemInstance.get().getComponent(ContainerSystem.class).getAppContexts()) {
            for (final WebContext webContext : app.getWebContexts()) {
                if (isSameContext(webContext.getWebBeansContext())) {
                    String contextRoot = webContext.getContextRoot();
                    if (contextRoot != null) {
                        if (contextRoot.startsWith("/")) {
                            return contextRoot.substring(1);
                        }
                        return contextRoot;
                    }
                    return webContext.getId();
                }
            }
            if (isSameContext(app.getWebBeansContext())) {
                return app.getId();
            }
        }
        throw new IllegalArgumentException("Can't find application matching bean manager");
    }

    private boolean isSameContext(final WebBeansContext app) {
        final BeanManagerImpl bm = app.getBeanManagerImpl();
        try {
            return bm.isInUse() && equals(bm.getExtension(HessianExtension.class));
        } catch (final Exception e) {
            return false;
        }
    }

    protected void shutdown(final @Observes BeforeShutdown unused) {
        final HessianService service = SystemInstance.get().getComponent(HessianService.class);
        if (service == null) {
            return;
        }

        final HessianRegistry registry = service.getRegistry();
        for (final DeployedEndpoint pair : deployed) {
            registry.undeploy(service.getVirtualHost(), pair.app, pair.name);
            LOGGER.info("Undeployed CDI hessian service " + pair.name);
        }
        deployed.clear();
    }

    private static String getName(final String path, final Class<?> itf) {
        if (path != null) {
            return path;
        }

        final Hessian hessian = itf.getAnnotation(Hessian.class);
        if (hessian != null) {
            final String name = hessian.path();
            if (name.isEmpty()) {
                return itf.getName();
            }
            return name;
        }
        return itf.getName();
    }

    protected static class Deployment {
        private final Class<?> itf;
        private final String path;
        private final Bean<?> bean;

        public Deployment(final Class<?> itf, final String path, final Bean<?> bean) {
            this.itf = itf;
            this.path = path;
            this.bean = bean;
        }
    }

    protected static class DeployedEndpoint {
        private final String app;
        private final String name;

        protected DeployedEndpoint(final String app, final String name) {
            this.app = app;
            this.name = name;
        }
    }
}
