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

import org.springframework.beans.factory.annotation.Required;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

public class EJB {
    private Application application;
    private Object deploymentId;

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

    @PostConstruct
    public void start() throws OpenEJBException {
        if (application == null) throw new NullPointerException("application is null");
        if (deploymentId == null) throw new NullPointerException("deploymentId is null");

        application.startEjb(deploymentId);

        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        DeploymentInfo deploymentInfo = containerSystem.getDeploymentInfo(deploymentId);
    }
}
