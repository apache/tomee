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

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.client.EJBMetaDataImpl;
import org.apache.openejb.client.JNDIRequest;
import org.apache.openejb.client.JNDIResponse;
import org.apache.openejb.client.RequestMethods;
import org.apache.openejb.client.ResponseCodes;

class JndiRequestHandler implements ResponseCodes, RequestMethods {
    private final EjbDaemon daemon;

    javax.naming.Context clientJndi;

    JndiRequestHandler(EjbDaemon daemon) throws Exception {
        ContainerSystem containerSystem = (ContainerSystem) SystemInstance.get().getComponent(ContainerSystem.class);
        clientJndi = (javax.naming.Context) containerSystem.getJNDIContext().lookup("openejb/ejb");
        this.daemon = daemon;
    }

    public void processRequest(ObjectInputStream in, ObjectOutputStream out) throws Exception {
        JNDIRequest req = new JNDIRequest();
        JNDIResponse res = new JNDIResponse();
        req.readExternal(in);

        String name = req.getRequestString();
        if (name.startsWith("/")) name = name.substring(1);

        DeploymentInfo deployment = daemon.deploymentIndex.getDeployment(name);

        if (deployment == null) {
            try {
                Object obj = clientJndi.lookup(name);

                if (obj instanceof Context) {
                    res.setResponseCode(JNDI_CONTEXT);
                } else
                    res.setResponseCode(JNDI_NOT_FOUND);

            } catch (NameNotFoundException e) {
                res.setResponseCode(JNDI_NOT_FOUND);
            } catch (NamingException e) {
                res.setResponseCode(JNDI_NAMING_EXCEPTION);
                res.setResult(e);
            }
        } else {
            res.setResponseCode(JNDI_EJBHOME);
            EJBMetaDataImpl metaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                    deployment.getRemoteInterface(),
                    deployment.getPrimaryKeyClass(),
                    deployment.getComponentType(),
                    deployment.getDeploymentID().toString(),
                    this.daemon.deploymentIndex.getDeploymentIndex(name));
            res.setResult(metaData);
        }

        res.writeExternal(out);
    }
}