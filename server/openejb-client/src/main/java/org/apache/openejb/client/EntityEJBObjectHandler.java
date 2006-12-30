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
package org.apache.openejb.client;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

public class EntityEJBObjectHandler extends EJBObjectHandler {

    public EntityEJBObjectHandler() {
    }

    public EntityEJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client) {
        super(ejb, server, client);
    }

    public EntityEJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client, Object primaryKey) {
        super(ejb, server, client, primaryKey);
        registryId = ejb.deploymentID + ":" + primaryKey;
        registerHandler(registryId, this);
    }

    public Object getRegistryId() {
        return registryId;
    }

    protected Object getPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable {
        return primaryKey;
    }

    protected Object isIdentical(Method method, Object[] args, Object proxy) throws Throwable {
        if (args[0] == null) return Boolean.FALSE;

        EJBObjectProxy ejbObject = (EJBObjectProxy) args[0];
        EJBObjectHandler that = ejbObject.getEJBObjectHandler();

        return new Boolean(this.registryId.equals(that.registryId));

    }

    protected Object remove(Method method, Object[] args, Object proxy) throws Throwable {

        EJBRequest req = new EJBRequest(RequestMethodConstants.EJB_OBJECT_REMOVE);

        req.setMethodParameters(args);
        req.setMethodInstance(method);
        req.setClientIdentity(client.getClientIdentity());
        req.setDeploymentCode(ejb.deploymentCode);
        req.setDeploymentId(ejb.deploymentID);
        req.setPrimaryKey(primaryKey);

        EJBResponse res = request(req);

        switch (res.getResponseCode()) {
            case ResponseCodes.EJB_ERROR:
                throw new SystemError((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_SYS_EXCEPTION:
                throw new SystemException((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_APP_EXCEPTION:
                throw new ApplicationException((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_OK:
                invalidateAllHandlers(getRegistryId());
                return null;
            default:
                throw new RemoteException("Received invalid response code from server: " + res.getResponseCode());
        }
    }

}
