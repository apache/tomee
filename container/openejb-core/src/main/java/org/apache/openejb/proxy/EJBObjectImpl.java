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

import java.io.ObjectStreamException;
import java.rmi.RemoteException;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.Handle;


/**
 * @version $Revision$ $Date$
 */
public abstract class EJBObjectImpl extends BaseEJB implements EJBObject {

    public EJBObjectImpl(EJBMethodInterceptor handler) {
        super(handler);
    }

    public EJBHome getEJBHome() throws RemoteException {
        return getProxyFactory().getEJBHome();
    }

    public Handle getHandle() throws RemoteException {
        return new HandleImpl(this, HandleImpl.HANDLE);
    }

    public Object writeReplace() throws ObjectStreamException {
        return SerializationHandler.writeReplace(this, getProxyInfo());
    }

}

