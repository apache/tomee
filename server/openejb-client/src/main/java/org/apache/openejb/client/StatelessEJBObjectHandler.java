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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class StatelessEJBObjectHandler extends EJBObjectHandler {

    public Object registryId;

    public StatelessEJBObjectHandler() {
    }

    public StatelessEJBObjectHandler(final ThreadPoolExecutor executorService, final EJBMetaDataImpl ejb, final ServerMetaData server, final ClientMetaData client, final JNDIContext.AuthenticationInfo auth) {
        super(executorService, ejb, server, client, auth);
    }

    public StatelessEJBObjectHandler(final ThreadPoolExecutor executorService,
                                     final EJBMetaDataImpl ejb,
                                     final ServerMetaData server,
                                     final ClientMetaData client,
                                     final Object primaryKey,
                                     final JNDIContext.AuthenticationInfo auth) {
        super(executorService, ejb, server, client, primaryKey, auth);
    }

    public static Object createRegistryId(final Object primKey, final Object deployId, final String containerID) {
        return "" + deployId + containerID;
    }

    @Override
    public Object getRegistryId() {
        return this.ejb.deploymentID;
    }

    @Override
    protected Object getPrimaryKey(final Method method, final Object[] args, final Object proxy) throws Throwable {
        throw new RemoteException("Session objects are private resources and do not have primary keys");
    }

    @Override
    protected Object isIdentical(final Method method, final Object[] args, final Object proxy) throws Throwable {

        final Object arg = (args.length == 1) ? args[0] : null;

        if (arg == null || !(arg instanceof EJBObjectProxy)) {
            return Boolean.FALSE;
        }
        final EJBObjectProxy proxy2 = (EJBObjectProxy) arg;
        final EJBObjectHandler that = proxy2.getEJBObjectHandler();
        return this.ejb.deploymentID.equals(that.ejb.deploymentID);
    }

    @Override
    protected Object equals(final Method method, final Object[] args, final Object proxy) throws Throwable {
        return isIdentical(method, args, proxy);
    }

    @Override
    protected void invalidateReference() {
    }

    @Override
    protected Object remove(final Method method, final Object[] args, final Object proxy) throws Throwable {
        // you can't really remove a stateless handle
        return null;
    }

}
