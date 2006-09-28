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

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.client.EJBRequest;
import org.apache.openejb.util.Messages;

public class DeploymentIndex {

    Messages messages = new Messages("org.apache.openejb.server.ejbd");

    DeploymentInfo[] deployments = null;

    HashMap index = null;

    public DeploymentIndex(DeploymentInfo[] deploymentInfos) {
        DeploymentInfo[] ds = deploymentInfos;

        deployments = new DeploymentInfo[ ds.length + 1 ];

        System.arraycopy(ds, 0, deployments, 1, ds.length);

        index = new HashMap(deployments.length);
        for (int i = 1; i < deployments.length; i++) {
            index.put(deployments[i].getDeploymentID(), new Integer(i));
        }
    }

    public DeploymentInfo getDeployment(EJBRequest req) throws RemoteException {

        DeploymentInfo info = null;

        if (req.getDeploymentCode() > 0 && req.getDeploymentCode() < deployments.length) {
            info = deployments[req.getDeploymentCode()];
            if (info == null) {
                throw new RemoteException("The deployement with this ID is null");
            }
            req.setDeploymentId((String) info.getDeploymentID());
            return info;
        }

        if (req.getDeploymentId() == null) {
            throw new RemoteException("Invalid deployment id and code: id=" + req.getDeploymentId() + ": code=" + req.getDeploymentCode());
        }

        int idCode = getDeploymentIndex(req.getDeploymentId());

        if (idCode == -1) {
            throw new RemoteException("No such deployment id and code: id=" + req.getDeploymentId() + ": code=" + req.getDeploymentCode());
        }

        req.setDeploymentCode(idCode);

        if (req.getDeploymentCode() < 0 || req.getDeploymentCode() >= deployments.length) {
            throw new RemoteException("Invalid deployment id and code: id=" + req.getDeploymentId() + ": code=" + req.getDeploymentCode());
        }

        info = deployments[req.getDeploymentCode()];
        if (info == null) {
            throw new RemoteException("The deployement with this ID is null");
        }
        return info;
    }

    public int getDeploymentIndex(DeploymentInfo deployment) {
        return getDeploymentIndex((String) deployment.getDeploymentID());
    }

    public int getDeploymentIndex(String deploymentID) {
        Integer idCode = (Integer) index.get(deploymentID);

        return (idCode == null) ? -1 : idCode.intValue();
    }

    public DeploymentInfo getDeployment(String deploymentID) {
        return getDeployment(getDeploymentIndex(deploymentID));
    }

    public DeploymentInfo getDeployment(Integer index) {
        return (index == null) ? null : getDeployment(index.intValue());
    }

    public DeploymentInfo getDeployment(int index) {
        return deployments[index];
    }
}

