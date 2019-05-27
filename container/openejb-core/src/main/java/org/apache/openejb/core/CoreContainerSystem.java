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

package org.apache.openejb.core;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.Container;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

import javax.naming.Context;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @org.apache.xbean.XBean element="containerSystem"
 */
public class CoreContainerSystem implements ContainerSystem {

    private final Map<Object, AppContext> apps = new ConcurrentHashMap<>();
    private final Map<Object, BeanContext> deployments = new ConcurrentHashMap<>();
    private final Map<Object, Container> containers = new ConcurrentHashMap<>();
    private final Map<String, List<WebContext>> webDeployments = new ConcurrentHashMap<>();
    private final Context jndiContext;

    /**
     * Constructs a CoreContainerSystem and initializes the root JNDI context.
     * It also creates three sub contexts, namely
     * <ul>
     * <li>java:openejb/local</li>
     * <li>java:openejb/client</li>
     * <li>java:openejb/Deployment</li>
     * </ul>
     *
     * @param jndiFactory JndiFactory
     * @throws RuntimeException if there is a problem during initialization of the root context
     */
    public CoreContainerSystem(final JndiFactory jndiFactory) {

        if (jndiFactory == null) {
            throw new NullPointerException("JndiFactory required");
        }
        jndiContext = jndiFactory.createRootContext();
        try {
            if (!(jndiContext.lookup("openejb/local") instanceof Context)
                || !(jndiContext.lookup("openejb/remote") instanceof Context)
                || !(jndiContext.lookup("openejb/client") instanceof Context)
                || !(jndiContext.lookup("openejb/Deployment") instanceof Context)
                || !(jndiContext.lookup("openejb/global") instanceof Context)) {
                throw new OpenEJBRuntimeException("core openejb naming context not properly initialized.  It must have subcontexts for openejb/local, openejb/remote, openejb/client, and openejb/Deployment already present");
            }
        } catch (final NamingException exception) {
            throw new OpenEJBRuntimeException("core openejb naming context not properly initialized.  It must have subcontexts for openejb/local, openejb/remote, openejb/client, and openejb/Deployment already present", exception);
        }
        SystemInstance.get().setComponent(JndiFactory.class, jndiFactory);
    }

    /**
     * Returns the DeploymentInfo for an EJB with the given deploymentID.
     *
     * @param deploymentID The deployment ID of an EJB
     */
    @Override
    public BeanContext getBeanContext(final Object deploymentID) {
        return deployments.get(deploymentID);
    }

    @Override
    public BeanContext[] deployments() {
        return deployments.values().toArray(new BeanContext[deployments.size()]);
    }

    public void addDeployment(final BeanContext deployment) {
        this.deployments.put(deployment.getDeploymentID(), deployment);
    }

    public void removeBeanContext(final BeanContext info) {
        this.deployments.remove(info.getDeploymentID());
    }

    @Override
    public Container getContainer(final Object id) {
        return containers.get(id);
    }

    @Override
    public Container[] containers() {
        return containers.values().toArray(new Container[containers.size()]);
    }

    public void addContainer(final Object id, final Container c) {
        containers.put(id, c);
    }

    public void removeContainer(final Object id) {
        containers.remove(id);
    }

    @Override
    public WebContext getWebContextByHost(final String id, final String host) {
        final List<WebContext> webContexts = webDeployments.get(id);
        if (webContexts == null || webContexts.isEmpty()) {
            return null;
        }
        if (webContexts.size() == 1 && webContexts.get(0).getHost() == null) {
            return webContexts.get(0);
        }
        for (final WebContext web : webContexts) {
            if (web.getHost() != null && web.getHost().equals(host)) {
                return web;
            }
        }
        return null;
    }

    @Override
    public WebContext getWebContext(final String id) {
        final List<WebContext> webContexts = webDeployments.get(id);
        return webContexts != null && !webContexts.isEmpty() ? webContexts.get(0) : null;
    }

    public WebContext[] WebDeployments() {
        final Collection<WebContext> all = new ArrayList<>(webDeployments.size());
        for (final Collection<WebContext> list : webDeployments.values()) {
            all.addAll(list);
        }
        return all.toArray(new WebContext[all.size()]);
    }

    public void addWebContext(final WebContext webDeployment) {
        final String id = webDeployment.getId();
        List<WebContext> list = this.webDeployments.computeIfAbsent(id, k -> new ArrayList<>());
        list.add(webDeployment);
    }

    public void removeWebContext(final WebContext info) {
        this.webDeployments.remove(info.getId());
    }

    @Override
    public Context getJNDIContext() {
        return jndiContext;
    }

    @Override
    public List<AppContext> getAppContexts() {
        return new ArrayList<>(apps.values());
    }

    @Override
    public AppContext getAppContext(final Object id) {

        AppContext context = apps.get(id);

        if (null == context && null != id) {
            context = apps.get(id.toString().toLowerCase(Locale.ENGLISH));
        }

        return context;
    }

    public void addAppContext(final AppContext appContext) {
        apps.put(appContext.getId().toLowerCase(Locale.ENGLISH), appContext);
    }

    public AppContext removeAppContext(final Object id) {
        AppContext context = apps.remove(id);

        if (null == context && null != id) {
            context = apps.remove(id.toString().toLowerCase(Locale.ENGLISH));
        }

        return context;
    }

    public synchronized Object[] getAppContextKeys() {
        return apps.keySet().toArray();
    }
}
