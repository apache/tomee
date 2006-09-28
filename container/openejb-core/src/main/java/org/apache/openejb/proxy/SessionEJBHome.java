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
package org.apache.openejb.proxy;

import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;

import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.RemoveException;

import org.apache.openejb.DeploymentNotFoundException;


/**
 *
 */
public abstract class SessionEJBHome extends EJBHomeImpl{

    public SessionEJBHome(EJBMethodInterceptor handler) {
        super(handler);
    }
    
    public void remove(Handle handle) throws RemoteException, RemoveException {
        if (getEJBMetaData().isStatelessSession()){
            if (handle == null) {
                throw new RemoveException("Handle is null");
            }
            ProxyInfo proxyInfo = null;
            try {
                proxyInfo = ejbHandler.getProxyInfo();
            } catch (DeploymentNotFoundException e) {
                throw new NoSuchObjectException(e.getMessage());
            }
            Class remoteInterface = proxyInfo.getRemoteInterface();
            if (!remoteInterface.isInstance(handle.getEJBObject())) {
                throw new RemoteException("Handle does not hold a " + remoteInterface.getName());
            }
        } else {
            EJBObject ejbObject = handle.getEJBObject();
            ejbObject.remove();
        }
    }
    
    public void remove(Object primaryKey) throws RemoteException, RemoveException {
        throw new RemoveException("Session objects are private resources and do not have primary keys");
    }
}
