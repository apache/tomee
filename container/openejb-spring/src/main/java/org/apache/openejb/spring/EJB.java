/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.spring;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.assembler.classic.JndiBuilder;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

@Exported
public class EJB<T> implements FactoryBean {
    private Object deploymentId;
    private DeploymentInfo deploymentInfo;
    private Class<T> intf;

    public EJB() {
    }

    public EJB(DeploymentInfo deploymentInfo, Class<T> intf) {
        this.deploymentInfo = deploymentInfo;
        this.intf = intf;
    }

    public Object getDeploymentId() {
        if (deploymentId != null) {
            return deploymentId;
        } else if (deploymentInfo != null) {
            return deploymentInfo.getDeploymentID();
        }
        return null;
    }

    public void setDeploymentId(Object deploymentId) {
        this.deploymentId = deploymentId;
    }

    public DeploymentInfo getDeploymentInfo() {
        if (deploymentInfo != null) {
            return deploymentInfo;
        } else if (deploymentId != null) {
            ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
            DeploymentInfo deploymentInfo = containerSystem.getDeploymentInfo(deploymentId);
            return deploymentInfo;
        }
        return null;
    }

    public void setDeploymentInfo(DeploymentInfo deploymentInfo) {
        this.deploymentInfo = deploymentInfo;
    }

    public Class<T> getInterface() {
        return intf;
    }

    @Required
    public void setInterface(Class<T> intf) {
        this.intf = intf;
    }

    public T getObject() throws Exception {
        if (intf == null) throw new NullPointerException("intf is null");

        DeploymentInfo deploymentInfo = getDeploymentInfo();
        if (deploymentInfo == null) {
            throw new IllegalStateException("DeploymentInfo or DeploymentID must be set before EJB can be retrieved");
        }

        // this is the pattern for the internal jndi name
        String jndiName = "java:openejb/Deployment/" + JndiBuilder.format(deploymentInfo.getDeploymentID(), getInterface().getName());

        // perform the lookup against the jndi context in the container system
        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        Object proxy = containerSystem.getJNDIContext().lookup(jndiName);
        if (!intf.isInstance(proxy)) {
            throw new IllegalArgumentException("EJB at " + jndiName + " is not an instance of " + intf.getName() + ", but is " + proxy.getClass().getName());
        }
        return intf.cast(proxy);
    }

    public Class<T> getObjectType() {
        return getInterface();
    }

    public boolean isSingleton() {
        return false;
    }
}
