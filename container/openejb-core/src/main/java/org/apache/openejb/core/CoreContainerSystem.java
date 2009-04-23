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
package org.apache.openejb.core;

import org.apache.openejb.Container;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.core.ivm.naming.IvmContext;
import org.apache.openejb.loader.SystemInstance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @org.apache.xbean.XBean element="containerSystem"
 */
public class CoreContainerSystem implements org.apache.openejb.spi.ContainerSystem {
    Map<Object, DeploymentInfo> deployments = new ConcurrentHashMap<Object, DeploymentInfo>();
    Map<Object, Container> containers = new ConcurrentHashMap<Object, Container>();
    Map<String, WebDeploymentInfo> webDeployments = new ConcurrentHashMap<String, WebDeploymentInfo>();
    IvmContext jndiRootContext = null;
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
     */
    public CoreContainerSystem() {

        try {

            jndiRootContext = IvmContext.createRootContext();

            jndiRootContext.bind("openejb/local/.", "");
            jndiRootContext.bind("openejb/remote/.", "");
            jndiRootContext.bind("openejb/client/.", "");
            jndiRootContext.bind("openejb/Deployment/.", "");
        }
        catch (javax.naming.NamingException exception) {
            throw new RuntimeException();
        }

        // todo this should be in a start method because publishing an external reference in the constructor is very dangerous
        SystemInstance.get().setComponent(org.apache.openejb.spi.ContainerSystem.class, this);
    }
    /**
     * Returns the DeploymentInfo for an EJB with the given deploymentID.
     * 
     * @param deploymentID The deployment ID of an EJB
     */
    public DeploymentInfo getDeploymentInfo(Object deploymentID) {
        return deployments.get(deploymentID);
    }

    public DeploymentInfo [] deployments() {
        return deployments.values().toArray(new DeploymentInfo [deployments.size()]);
    }

    public void addDeployment(DeploymentInfo deployment) {
        this.deployments.put(deployment.getDeploymentID(), deployment);
    }

    public void removeDeploymentInfo(DeploymentInfo info){
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

    public WebDeploymentInfo getWebDeploymentInfo(String id) {
        return webDeployments.get(id);
    }

    public WebDeploymentInfo [] WebDeployments() {
        return webDeployments.values().toArray(new WebDeploymentInfo [webDeployments.size()]);
    }

    public void addWebDeployment(WebDeploymentInfo webDeployment) {
        this.webDeployments.put(webDeployment.getId(), webDeployment);
    }

    public void removeWebDeploymentInfo(WebDeploymentInfo info){
        this.webDeployments.remove(info.getId());
    }

    public javax.naming.Context getJNDIContext() {
        return jndiRootContext;
    }
}
