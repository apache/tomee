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
import org.apache.openejb.loader.SystemInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;

/**
 * @org.apache.xbean.XBean element="containerSystem"
 */
public class CoreContainerSystem implements org.apache.openejb.spi.ContainerSystem {
    private final Map<Object, AppContext> apps = new ConcurrentHashMap<Object, AppContext>();
    private final Map<Object, BeanContext> deployments = new ConcurrentHashMap<Object, BeanContext>();
    private final Map<Object, Container> containers = new ConcurrentHashMap<Object, Container>();
    private final Map<String, WebContext> webDeployments = new ConcurrentHashMap<String, WebContext>();
    private final Context jndiContext;

    /**
     * Constructs a CoreContainerSystem and initializes the root JNDI context.
     * It also creates three sub contexts, namely
     * <ul>
     *  <li>java:openejb/local</li>
     *  <li>java:openejb/client</li>
     *  <li>java:openejb/Deployment</li>
     * </ul>
     *
     *@throws RuntimeException if there is a problem during initialization of the root context
     * @param jndiFactory
     */
    public CoreContainerSystem(JndiFactory jndiFactory) {

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
                throw new RuntimeException("core openejb naming context not properly initialized.  It must have subcontexts for openejb/local, openejb/remote, openejb/client, and openejb/Deployment already present");
            }
        }
        catch (javax.naming.NamingException exception) {
            throw new RuntimeException("core openejb naming context not properly initialized.  It must have subcontexts for openejb/local, openejb/remote, openejb/client, and openejb/Deployment already present", exception);
        }
        SystemInstance.get().setComponent(JndiFactory.class, jndiFactory);
    }
    /**
     * Returns the DeploymentInfo for an EJB with the given deploymentID.
     * 
     * @param deploymentID The deployment ID of an EJB
     */
    public BeanContext getBeanContext(Object deploymentID) {
        return deployments.get(deploymentID);
    }

    public BeanContext[] deployments() {
        return deployments.values().toArray(new BeanContext[deployments.size()]);
    }

    public void addDeployment(BeanContext deployment) {
        this.deployments.put(deployment.getDeploymentID(), deployment);
    }

    public void removeBeanContext(BeanContext info){
        this.deployments.remove(info.getDeploymentID());
    }

    public Container getContainer(Object id) {
        return containers.get(id);
    }

    public Container [] containers() {
        return containers.values().toArray(new Container [containers.size()]);
    }

    public void addContainer(Object id, Container c) {
        containers.put(id, c);
    }

    public void removeContainer(Object id) {
        containers.remove(id);
    }

    public WebContext getWebContext(String id) {
        return webDeployments.get(id);
    }

    public WebContext[] WebDeployments() {
        return webDeployments.values().toArray(new WebContext[webDeployments.size()]);
    }

    public void addWebContext(WebContext webDeployment) {
        this.webDeployments.put(webDeployment.getId(), webDeployment);
    }

    public void removeWebContext(WebContext info){
        this.webDeployments.remove(info.getId());
    }

    public Context getJNDIContext() {
        return jndiContext;
    }

    @Override
    public List<AppContext> getAppContexts() {
        return new ArrayList<AppContext>(apps.values());
    }

    @Override
    public AppContext getAppContext(Object id) {
        return apps.get(id);
    }

    public void addAppContext(AppContext appContext) {
        apps.put(appContext.getId(), appContext);
    }
    
    public void removeAppContext(Object id) {
        apps.remove(id);
    }
}
