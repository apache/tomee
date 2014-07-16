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
package org.apache.openejb.server.ejbd;

import org.apache.openejb.BeanContext;
import org.apache.openejb.client.EJBRequest;
import org.apache.openejb.util.Messages;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class DeploymentIndex {

    Messages messages = new Messages("org.apache.openejb.server.ejbd");

    BeanContext[] deployments = null;

    Map index = null;

    @SuppressWarnings("unchecked")
    public DeploymentIndex(final BeanContext[] beanContexts) {

        deployments = new BeanContext[beanContexts.length + 1];

        System.arraycopy(beanContexts, 0, deployments, 1, beanContexts.length);

        index = new HashMap(deployments.length);
        for (int i = 1; i < deployments.length; i++) {
            index.put(deployments[i].getDeploymentID(), i);
        }
    }

    public BeanContext getDeployment(final EJBRequest req) throws RemoteException {

        final BeanContext info;

        final int deploymentCode = req.getDeploymentCode();
        if (deploymentCode > 0 && deploymentCode < deployments.length) {
            info = deployments[deploymentCode];
            req.setDeploymentId((String) info.getDeploymentID());
            return info;
        }

        if (req.getDeploymentId() == null) {
            throw new RemoteException(messages.format("invalidDeploymentIdAndCode", req.getDeploymentId(), req.getDeploymentCode()));
        }

        final int idCode = getDeploymentIndex(req.getDeploymentId());
        if (idCode == -1) {
            throw new RemoteException(messages.format("noSuchDeploymentIdAndCode", req.getDeploymentId(), req.getDeploymentCode()));
        }

        req.setDeploymentCode(idCode);

        if (req.getDeploymentCode() < 0 || req.getDeploymentCode() >= deployments.length) {
            throw new RemoteException(messages.format("invalidDeploymentIdAndCode", req.getDeploymentId(), req.getDeploymentCode()));
        }
        return deployments[req.getDeploymentCode()];
    }

    public int getDeploymentIndex(final BeanContext deployment) {
        return getDeploymentIndex((String) deployment.getDeploymentID());
    }

    public int getDeploymentIndex(final String deploymentID) {
        final Integer idCode = (Integer) index.get(deploymentID);

        return (idCode == null) ? -1 : idCode;
    }

    public BeanContext getDeployment(final String deploymentID) {
        return getDeployment(getDeploymentIndex(deploymentID));
    }

    public BeanContext getDeployment(final Integer index) {
        return (index == null) ? null : getDeployment(index.intValue());
    }

    public BeanContext getDeployment(final int index) {
        return deployments[index];
    }
}

