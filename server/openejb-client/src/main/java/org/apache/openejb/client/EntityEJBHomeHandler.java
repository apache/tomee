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

import jakarta.ejb.EJBObject;
import jakarta.ejb.Handle;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.concurrent.ThreadPoolExecutor;

public class EntityEJBHomeHandler extends EJBHomeHandler {

    public EntityEJBHomeHandler() {
    }

    public EntityEJBHomeHandler(final ThreadPoolExecutor executor, final EJBMetaDataImpl ejb, final ServerMetaData server, final ClientMetaData client, final JNDIContext.AuthenticationInfo auth) {
        super(executor, ejb, server, client, auth);
    }

    @Override
    protected Object findX(final Method method, final Object[] args, final Object proxy) throws Throwable {
        final EJBRequest req = new EJBRequest(RequestMethodCode.EJB_HOME_FIND, ejb, method, args, null, client.getSerializer());

        final EJBResponse res = request(req);

        Object primKey;
        EJBObjectHandler handler;
        final Object[] primaryKeys;

        switch (res.getResponseCode()) {
            case ResponseCodes.EJB_ERROR:
                throw new SystemError((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_SYS_EXCEPTION:
                throw new SystemException((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_APP_EXCEPTION:
                throw new ApplicationException((ThrowableArtifact) res.getResult());

            case ResponseCodes.EJB_OK_FOUND:
                primKey = res.getResult();
                if (primKey == null) {
                    return null;
                } else {
                    handler = EJBObjectHandler.createEJBObjectHandler(executor, ejb, server, client, primKey, authenticationInfo);
                    handler.setEJBHomeProxy((EJBHomeProxy) proxy);
                    registerHandler(ejb.deploymentID + ":" + primKey, handler);
                    return handler.createEJBObjectProxy();
                }

            case ResponseCodes.EJB_OK_FOUND_COLLECTION:

                primaryKeys = (Object[]) res.getResult();

                for (int i = 0; i < primaryKeys.length; i++) {
                    primKey = primaryKeys[i];
                    if (primKey != null) {
                        handler = EJBObjectHandler.createEJBObjectHandler(executor, ejb, server, client, primKey, authenticationInfo);
                        handler.setEJBHomeProxy((EJBHomeProxy) proxy);
                        registerHandler(ejb.deploymentID + ":" + primKey, handler);
                        primaryKeys[i] = handler.createEJBObjectProxy();
                    }
                }
                return java.util.Arrays.asList(primaryKeys);
            case ResponseCodes.EJB_OK_FOUND_ENUMERATION:

                primaryKeys = (Object[]) res.getResult();

                for (int i = 0; i < primaryKeys.length; i++) {
                    primKey = primaryKeys[i];
                    if (primKey != null) {
                        handler = EJBObjectHandler.createEJBObjectHandler(executor, ejb, server, client, primKey, authenticationInfo);
                        handler.setEJBHomeProxy((EJBHomeProxy) proxy);
                        registerHandler(ejb.deploymentID + ":" + primKey, handler);
                        primaryKeys[i] = handler.createEJBObjectProxy();
                    }
                }

                return new ArrayEnumeration(java.util.Arrays.asList(primaryKeys));
            default:
                throw new RemoteException("Received invalid response code from server: " + res.getResponseCode());
        }
    }

    @Override
    protected Object removeByPrimaryKey(final Method method, final Object[] args, final Object proxy) throws Throwable {
        final Object primKey = args[0];

        if (primKey == null) {
            throw new NullPointerException("The primary key is null.");
        }

        final EJBRequest req = new EJBRequest(RequestMethodCode.EJB_HOME_REMOVE_BY_PKEY, ejb, method, args, primKey, client.getSerializer());

        final EJBResponse res = request(req);

        switch (res.getResponseCode()) {
            case ResponseCodes.EJB_ERROR:
                throw new SystemError((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_SYS_EXCEPTION:
                throw new SystemException((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_APP_EXCEPTION:
                throw new ApplicationException((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_OK:
                invalidateAllHandlers(ejb.deploymentID + ":" + primKey);
                return null;
            default:
                throw new RemoteException("Received invalid response code from server: " + res.getResponseCode());
        }
    }

    @Override
    protected Object removeWithHandle(final Method method, final Object[] args, final Object proxy) throws Throwable {
        if (args[0] == null) {
            throw new RemoteException("Handler is null");
        }

        final Handle handle = (Handle) args[0];

        final EJBObject ejbObject = handle.getEJBObject();
        if (ejbObject == null) {
            throw new NullPointerException("The handle.getEJBObject() is null.");
        }

        final Object primKey = ejbObject.getPrimaryKey();
        if (primKey == null) {
            throw new NullPointerException("The handle.getEJBObject().getPrimaryKey() is null.");
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
                invalidateAllHandlers(ejb.deploymentID + ":" + primKey);
                return null;
            default:
                throw new RemoteException("Received invalid response code from server: " + res.getResponseCode());
        }
    }
}
