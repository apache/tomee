/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.assembler.spring;

import org.apache.openejb.DeploymentInfo;
import org.springframework.beans.factory.FactoryBean;

import java.util.Map;
import java.util.HashMap;

/**
 * @org.apache.xbean.XBean element="addDeployments"
 */
public class AddDeploymentsToContainerFactory implements FactoryBean {
    private String containerId;
    private Map<String, DeploymentInfo> availableDeployments;

    public String getTo() {
        return containerId;
    }

    public void setTo(String to) {
        this.containerId = to;
    }

    public Map<String, DeploymentInfo> getFrom() {
        return availableDeployments;
    }

    public void setFrom(Map<String, DeploymentInfo> from) {
        this.availableDeployments = from;
    }

    public Object getObject() throws Exception {
        Map<String, DeploymentInfo> assigned = new HashMap();
        for (DeploymentInfo deployment : availableDeployments.values().toArray(new DeploymentInfo[]{})) {
            if (containerId.equals(deployment.getContainer().getContainerID())){
                availableDeployments.remove(deployment.getDeploymentID());
                assigned.put(deployment.getDeploymentID()+"", deployment);
            }
        }
        return assigned;
    }

    public Class getObjectType() {
        return Map.class;
    }

    public boolean isSingleton() {
        return false;
    }

}
