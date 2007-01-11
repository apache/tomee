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

import java.util.HashMap;

import org.apache.openejb.Container;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.core.ivm.naming.IvmContext;

/**
 * @org.apache.xbean.XBean element="containerSystem"
 */
public class CoreContainerSystem implements org.apache.openejb.spi.ContainerSystem {

    HashMap deployments = new HashMap();
    HashMap containers = new HashMap();
    IvmContext jndiRootContext = null;

    public CoreContainerSystem() {

        try {

            jndiRootContext = IvmContext.createRootContext();

            jndiRootContext.createSubcontext("java:openejb/ejb");
        }
        catch (javax.naming.NamingException exception) {
            throw new RuntimeException();
        }

        // todo this should be in a start method because publishing an external reference in the constructor is very dangerous
        SystemInstance.get().setComponent(org.apache.openejb.spi.ContainerSystem.class, this);
    }

    public DeploymentInfo getDeploymentInfo(Object id) {
        return (DeploymentInfo) deployments.get(id);
    }

    public DeploymentInfo [] deployments() {
        return (DeploymentInfo []) deployments.values().toArray(new DeploymentInfo [deployments.size()]);
    }

    public Container getContainer(Object id) {
        return (Container) containers.get(id);
    }

    public Container [] containers() {
        return (Container []) containers.values().toArray(new Container [containers.size()]);
    }

    public void addContainer(Object id, Container c) {
        containers.put(id, c);
    }

    public void addDeployment(DeploymentInfo deployment) {

        this.deployments.put(deployment.getDeploymentID(), deployment);

    }


    public javax.naming.Context getJNDIContext() {
        return jndiRootContext;
    }
}
