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

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.apache.openejb.BeanContext;
import org.apache.openejb.client.EJBRequest;
import org.apache.openejb.util.Messages;

public class DeploymentIndex {

    Messages messages = new Messages("org.apache.openejb.server.ejbd");

    BeanContext[] deployments = null;

    Map index = null;

    public DeploymentIndex(BeanContext[] beanContexts) {
        BeanContext[] ds = beanContexts;

        deployments = new BeanContext[ ds.length + 1 ];

        System.arraycopy(ds, 0, deployments, 1, ds.length);

        index = new HashMap(deployments.length);
        for (int i = 1; i < deployments.length; i++) {
            index.put(deployments[i].getDeploymentID(), new Integer(i));
        }
    }

    public BeanContext getDeployment(EJBRequest req) throws RemoteException {

        BeanContext info = null;

        int deploymentCode = req.getDeploymentCode();
        if (deploymentCode > 0 && deploymentCode < deployments.length) {
            info = deployments[deploymentCode];
            req.setDeploymentId((String) info.getDeploymentID());
            return info;
        }

        if (req.getDeploymentId() == null) {
            throw new RemoteException(messages.format("invalidDeploymentIdAndCode", req.getDeploymentId(), req.getDeploymentCode()));
        }

        int idCode = getDeploymentIndex(req.getDeploymentId());
        if (idCode == -1) {
            throw new RemoteException(messages.format("noSuchDeploymentIdAndCode", req.getDeploymentId(), req.getDeploymentCode()));
        }

        req.setDeploymentCode(idCode);

        if (req.getDeploymentCode() < 0 || req.getDeploymentCode() >= deployments.length) {
            throw new RemoteException(messages.format("invalidDeploymentIdAndCode", req.getDeploymentId(), req.getDeploymentCode()));
        }
        return deployments[req.getDeploymentCode()];
    }

    public int getDeploymentIndex(BeanContext deployment) {
        return getDeploymentIndex((String) deployment.getDeploymentID());
    }

    public int getDeploymentIndex(String deploymentID) {
        Integer idCode = (Integer) index.get(deploymentID);

        return (idCode == null) ? -1 : idCode.intValue();
    }

    public BeanContext getDeployment(String deploymentID) {
        return getDeployment(getDeploymentIndex(deploymentID));
    }

    public BeanContext getDeployment(Integer index) {
        return (index == null) ? null : getDeployment(index.intValue());
    }

    public BeanContext getDeployment(int index) {
        return deployments[index];
    }
}

