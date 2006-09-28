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
package org.apache.openejb.entity.cmp;

import java.io.Serializable;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;

import org.apache.openejb.proxy.EJBProxyFactory;

/**
 *
 *
 * @version $Revision$ $Date$
 */
final class TranqlEJBProxyFactory implements org.tranql.ejb.EJBProxyFactory, Serializable {
    private static final long serialVersionUID = -5825855551226825417L;
    private final Class localClass;
    private final Class remoteClass;

    private transient EJBProxyFactory ejbProxyFactory;

    public TranqlEJBProxyFactory(Class localClass, Class remoteClass) {
        this.localClass = localClass;
        this.remoteClass = remoteClass;
    }

    public void setEjbProxyFactory(EJBProxyFactory ejbProxyFactory) {
        this.ejbProxyFactory = ejbProxyFactory;
    }

    public Class getLocalInterfaceClass() {
        return localClass;
    }

    public Class getRemoteInterfaceClass() {
        return remoteClass;
    }

    public EJBLocalObject getEJBLocalObject(Object pk) {
        return ejbProxyFactory.getEJBLocalObject(pk);
    }

    public EJBObject getEJBObject(Object pk) {
        return ejbProxyFactory.getEJBObject(pk);
    }
}
