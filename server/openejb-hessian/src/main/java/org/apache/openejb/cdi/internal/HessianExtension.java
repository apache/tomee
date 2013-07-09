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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.container.InjectableBeanManager;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessSessionBean;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

public class HessianExtension implements Extension {
    private static final Logger LOGGER = Logger.getLogger(HessianExtension.class.getName());

    private final Collection<Pair<Class<?>, Bean<?>>> toDeploy = new ArrayList<Pair<Class<?>, Bean<?>>>();
    private final Collection<DeployedEndpoint> deployed = new ArrayList<DeployedEndpoint>();

    protected <X> void findHessianWebServices(final @Observes ProcessBean<X> processBean) {
        if (ProcessSessionBean.class.isInstance(processBean)) {
            return;
        }

        final Bean<X> bean = processBean.getBean();
        for (final Class<?> itf : bean.getBeanClass().getInterfaces()) {
            final Hessian hessian = itf.getAnnotation(Hessian.class);
            if (hessian != null) {
                toDeploy.add(new ImmutablePair<Class<?>, Bean<?>>(itf, bean));
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

        for (final Pair<Class<?>, Bean<?>> pair : toDeploy) {
            final Class<?> itf = pair.getKey();
            final Hessian hessian = itf.getAnnotation(Hessian.class);
            final Bean<?> bean = pair.getValue();
            final HessianServer server = new HessianServer(bean.getBeanClass().getClassLoader());
            try {
                if (!hessian.serializerFactory().isInstance(server.getSerializerFactory())) {
                    server.serializerFactory(hessian.serializerFactory().newInstance());
                }
            } catch (final Exception e) {
                throw new OpenEJBRuntimeException(e);
            }
            server.sendCollectionType(hessian.sendCollectionType());
            if (Dependent.class.equals(bean.getScope())) {
                LOGGER.warning("@Dependent can lead to memory leaks ATM");
            }
            server.createSkeleton(bm.getReference(bean, itf, null), itf);

            final String name = getName(itf);
            final String appName = findAppName(bm);
            try {
                LOGGER.info("Hessian(url=" + registry.deploy(itf.getClassLoader(), server,
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

    private static String findAppName(final BeanManager bm) {
        final AppInfo currentApp = OpenEJBLifecycle.CURRENT_APP_INFO.get();
        if (currentApp == null) {
            throw new IllegalStateException("Without OpenEJBLifecycle this Extension can't work correctly");
        }

        if (currentApp.webAppAlone) {
            return currentApp.webApps.iterator().next().contextRoot;
        }

        for (final AppContext app : SystemInstance.get().getComponent(ContainerSystem.class).getAppContexts()) {
            for (final WebContext webContext : app.getWebContexts()) {
                if (isSameContext(bm, webContext.getWebBeansContext())) {
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
            if (isSameContext(bm, app.getWebBeansContext())) {
                return app.getId();
            }
        }
        throw new IllegalArgumentException("Can't find application matching bean manager " + bm);
    }

    private static boolean isSameContext(final BeanManager bm, WebBeansContext app) {
        return InjectableBeanManager.class.isInstance(bm) && app == InjectableBeanManager.class.cast(bm).getWebBeansContext()
                || BeanManagerImpl.class.isInstance(bm) && app == BeanManagerImpl.class.cast(bm).getWebBeansContext();
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

    private static String getName(final Class<?> itf) {
        final Hessian hessian = itf.getAnnotation(Hessian.class);
        final String name = hessian.path();
        if (name.isEmpty()) {
            return itf.getName();
        }
        return name;
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
