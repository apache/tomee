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
import java.util.concurrent.ThreadPoolExecutor;

public class EntityEJBObjectHandler extends EJBObjectHandler {

    public EntityEJBObjectHandler() {
    }

    public EntityEJBObjectHandler(final ThreadPoolExecutor executorService, final EJBMetaDataImpl ejb, final ServerMetaData server, final ClientMetaData client, final JNDIContext.AuthenticationInfo auth) {
        super(executorService, ejb, server, client, auth);
    }

    public EntityEJBObjectHandler(final ThreadPoolExecutor executorService,
                                  final EJBMetaDataImpl ejb,
                                  final ServerMetaData server,
                                  final ClientMetaData client,
                                  final Object primaryKey,
                                  final JNDIContext.AuthenticationInfo auth) {
        super(executorService, ejb, server, client, primaryKey, auth);
        registryId = ejb.deploymentID + ":" + primaryKey;
        registerHandler(registryId, this);
    }

    @Override
    public Object getRegistryId() {
        return registryId;
    }

    @Override
    protected Object getPrimaryKey(final Method method, final Object[] args, final Object proxy) throws Throwable {
        return primaryKey;
    }

    @Override
    protected Object isIdentical(final Method method, final Object[] args, final Object proxy) throws Throwable {
        if (args[0] == null) {
            return Boolean.FALSE;
        }

        final EJBObjectProxy ejbObject = (EJBObjectProxy) args[0];
        final EJBObjectHandler that = ejbObject.getEJBObjectHandler();

        return this.registryId.equals(that.registryId);

    }

    @Override
    protected Object equals(final Method method, final Object[] args, final Object proxy) throws Throwable {
        return isIdentical(method, args, proxy);
    }

    @Override
    protected Object remove(final Method method, final Object[] args, final Object proxy) throws Throwable {

        final EJBRequest req = new EJBRequest(RequestMethodCode.EJB_OBJECT_REMOVE, ejb, method, args, primaryKey, client.getSerializer());

        final EJBResponse res = request(req);

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

    @Override
    protected void invalidateReference() {
        // entity bean object references should not be invalidated since they
        // will automatically hook up to a new instance of the bean using the
        // primary key (we will load a new instance from the db)
    }
}
