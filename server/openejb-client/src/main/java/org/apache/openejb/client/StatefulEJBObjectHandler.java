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

public class StatefulEJBObjectHandler extends EJBObjectHandler {

    public StatefulEJBObjectHandler() {
    }

    public StatefulEJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client) {
        super(ejb, server, client);
    }

    public StatefulEJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client, Object primaryKey) {
        super(ejb, server, client, primaryKey);
        registerHandler(primaryKey, this);
    }

    public Object getRegistryId() {
        return primaryKey;
    }

    protected Object getPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable {
        throw new RemoteException("Session objects are private resources and do not have primary keys");
    }

    protected Object isIdentical(Method method, Object[] args, Object proxy) throws Throwable {
        if (args[0] == null) return Boolean.FALSE;

        EJBObjectProxy ejbObject = (EJBObjectProxy) args[0];
        EJBObjectHandler that = ejbObject.getEJBObjectHandler();

        return new Boolean(this.primaryKey.equals(that.primaryKey));
    }

    protected Object remove(Method method, Object[] args, Object proxy) throws Throwable {

        EJBRequest req = new EJBRequest(RequestMethodConstants.EJB_OBJECT_REMOVE);
        req.setDeploymentCode(ejb.deploymentCode);
        req.setDeploymentId(ejb.deploymentID);
        req.setMethodInstance(method);
        req.setMethodParameters(args);
        req.setPrimaryKey(primaryKey);

        EJBResponse res = request(req);

        if (ResponseCodes.EJB_ERROR == res.getResponseCode()) {
            throw (Throwable) res.getResult();
        }

        invalidateAllHandlers(this.getRegistryId());
        this.invalidateReference();
        return null;
    }

}
