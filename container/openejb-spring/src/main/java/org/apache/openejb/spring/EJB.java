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

import javax.annotation.PostConstruct;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

@Exported
public class EJB<T> implements FactoryBean {
    private Application application;
    private Object deploymentId;
    private Class<T> intf;

    public Application getApplication() {
        return application;
    }

    @Required
    public void setApplication(Application application) {
        this.application = application;
    }

    public Object getDeploymentId() {
        return deploymentId;
    }

    @Required
    public void setDeploymentId(Object deploymentId) {
        this.deploymentId = deploymentId;
    }

    public Class<T> getInterface() {
        return intf;
    }

    @Required
    public void setInterface(Class<T> intf) {
        this.intf = intf;
    }

    @PostConstruct
    public void start() throws OpenEJBException {
    }

    public T getObject() throws Exception {
        if (application == null) throw new NullPointerException("application is null");
        if (deploymentId == null) throw new NullPointerException("deploymentId is null");
        if (intf == null) throw new NullPointerException("intf is null");

        application.startEjb(deploymentId);

        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        DeploymentInfo deploymentInfo = containerSystem.getDeploymentInfo(deploymentId);
        if (deploymentInfo == null) {
            throw new IllegalArgumentException("Unknwon EJB " + deploymentInfo);
        }
        
        String jndiName = "java:openejb/Deployment/" + deploymentId + "/" + getInterface().getName();

        Object proxy = containerSystem.getJNDIContext().lookup(jndiName);
        if (!intf.isInstance(proxy)) {
            throw new IllegalArgumentException(
                    "EJB at " + jndiName + " is not an instance of " + intf.getName() + ", but is " + proxy.getClass().getName());
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
