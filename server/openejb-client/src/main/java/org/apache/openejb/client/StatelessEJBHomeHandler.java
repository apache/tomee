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

import javax.ejb.RemoveException;

public class StatelessEJBHomeHandler extends EJBHomeHandler {

    public StatelessEJBHomeHandler() {
    }

    public StatelessEJBHomeHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client) {
        super(ejb, server, client);
    }

    protected Object findX(Method method, Object[] args, Object proxy) throws Throwable {
        throw new UnsupportedOperationException("Stateful beans may not have find methods");
    }

    protected Object removeByPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable {
        throw new RemoveException("Session objects are private resources and do not have primary keys");
    }

    /*
    * TODO:3: Get a related quote from the specification to add here
    *
    * This method is differnt the the stateful and entity behavior because we only want the 
    * stateless session bean that created the proxy to be invalidated, not all the proxies. Special case
    * for the stateless session beans.
    */
    protected Object removeWithHandle(Method method, Object[] args, Object proxy) throws Throwable {

        EJBObjectHandle handle = (EJBObjectHandle) args[0];

        if (handle == null) throw new NullPointerException("The handle is null");

        EJBObjectHandler handler = (EJBObjectHandler) handle.ejbObjectProxy.getEJBObjectHandler();

        if (!handler.ejb.deploymentID.equals(this.ejb.deploymentID)) {
            throw new IllegalArgumentException("The handle is not from the same deployment");
        }
        handler.invalidateReference();

        return null;
    }

    protected EJBObjectHandler newEJBObjectHandler() {
        return new StatelessEJBObjectHandler();
    }

}
