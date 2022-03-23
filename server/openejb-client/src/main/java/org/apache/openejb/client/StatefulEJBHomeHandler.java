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

import jakarta.ejb.RemoveException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.concurrent.ThreadPoolExecutor;

public class StatefulEJBHomeHandler extends EJBHomeHandler {

    public StatefulEJBHomeHandler() {
    }

    public StatefulEJBHomeHandler(final ThreadPoolExecutor executor, final EJBMetaDataImpl ejb, final ServerMetaData server, final ClientMetaData client, final JNDIContext.AuthenticationInfo auth) {
        super(executor, ejb, server, client, auth);
    }

    @Override
    protected Object findX(final Method method, final Object[] args, final Object proxy) throws Throwable {
        throw new SystemException(new UnsupportedOperationException("Session beans may not have find methods"));
    }

    @Override
    protected Object removeByPrimaryKey(final Method method, final Object[] args, final Object proxy) throws Throwable {
        throw new ApplicationException(new RemoveException("Session objects are private resources and do not have primary keys"));
    }

    @Override
    protected Object removeWithHandle(final Method method, final Object[] args, final Object proxy) throws Throwable {

        final EJBObjectHandle handle = (EJBObjectHandle) args[0];

        if (handle == null) {
            throw new NullPointerException("The handle is null");
        }

        final EJBObjectHandler handler = handle.handler;
        final Object primKey = handler.primaryKey;

        if (!handler.ejb.deploymentID.equals(this.ejb.deploymentID)) {
            throw new SystemException(new IllegalArgumentException("The handle is not from the same deployment"));
        }

        final EJBRequest req = new EJBRequest(RequestMethodCode.EJB_HOME_REMOVE_BY_HANDLE, ejb, method, args, primKey, client.getSerializer());

        final EJBResponse res = request(req);

        switch (res.getResponseCode()) {
            case ResponseCodes.EJB_ERROR:
                throw new SystemError((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_SYS_EXCEPTION:
                throw new SystemException((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_APP_EXCEPTION:
                throw new ApplicationException((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_OK:
                invalidateAllHandlers(handler.getRegistryId());
                handler.invalidateReference();
                return null;
            default:
                throw new RemoteException("Received invalid response code from server: " + res.getResponseCode());
        }

    }
}
