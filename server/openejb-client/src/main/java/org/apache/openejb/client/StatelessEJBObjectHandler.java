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

public class StatelessEJBObjectHandler extends EJBObjectHandler {

    public Object registryId;

    public StatelessEJBObjectHandler() {
    }

    public StatelessEJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client) {
        super(ejb, server, client);
    }

    public StatelessEJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client, Object primaryKey) {
        super(ejb, server, client, primaryKey);
    }

    public static Object createRegistryId(Object primKey, Object deployId, String containerID) {
        return "" + deployId + containerID;
    }

    public Object getRegistryId() {
        return this.ejb.deploymentID;
    }

    protected Object getPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable {
        throw new RemoteException("Session objects are private resources and do not have primary keys");
    }

    protected Object isIdentical(Method method, Object[] args, Object proxy) throws Throwable {

        Object arg = (args.length == 1) ? args[0] : null;

        if (arg == null || !(arg instanceof EJBObjectProxy)) return Boolean.FALSE;
        EJBObjectProxy proxy2 = (EJBObjectProxy) arg;
        EJBObjectHandler that = proxy2.getEJBObjectHandler();
        return new Boolean(this.ejb.deploymentID.equals(that.ejb.deploymentID));
    }

    protected Object remove(Method method, Object[] args, Object proxy) throws Throwable {
//      checkAuthorization(method);
        invalidateReference();
        return null;
    }

}
