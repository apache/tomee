/*
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

package org.apache.openejb.core.cmp;

import org.apache.openejb.BeanContext;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.core.entity.EntityEjbHomeHandler;
import org.apache.openejb.util.proxy.ProxyManager;

import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBLocalHome;
import jakarta.ejb.EntityBean;

public class ProxyFactory {
    private final BeanContext beanContext;
    private final KeyGenerator keyGenerator;
    private final Class remoteInterface;
    private final EntityEjbHomeHandler remoteHandler;
    private final Class localInterface;
    private final EntityEjbHomeHandler localHandler;

    public ProxyFactory(final BeanContext beanContext) {
        this.beanContext = beanContext;
        keyGenerator = beanContext.getKeyGenerator();

        remoteInterface = beanContext.getRemoteInterface();
        if (remoteInterface != null) {
            final EJBHome homeProxy = beanContext.getEJBHome();
            remoteHandler = (EntityEjbHomeHandler) ProxyManager.getInvocationHandler(homeProxy);
        } else {
            remoteHandler = null;
        }

        localInterface = beanContext.getLocalInterface();
        if (localInterface != null) {
            final EJBLocalHome localHomeProxy = beanContext.getEJBLocalHome();
            localHandler = (EntityEjbHomeHandler) ProxyManager.getInvocationHandler(localHomeProxy);
        } else {
            localHandler = null;
        }
    }

    public Object createRemoteProxy(final EntityBean bean, final RpcContainer container) {
        // The KeyGenerator creates a new primary key and populates its fields with the
        // primary key fields of the bean instance.  Each deployment has its own KeyGenerator.
        final Object primaryKey = keyGenerator.getPrimaryKey(bean);

        // create the proxy
        final Object proxy = remoteHandler.createProxy(primaryKey, beanContext.getRemoteInterface());
        return proxy;
    }

    public Object createLocalProxy(final EntityBean bean, final RpcContainer container) {
        // The KeyGenerator creates a new primary key and populates its fields with the
        // primary key fields of the bean instance.  Each deployment has its own KeyGenerator.
        final Object primaryKey = keyGenerator.getPrimaryKey(bean);

        // create the proxy
        final Object proxy = localHandler.createProxy(primaryKey, beanContext.getLocalInterface());
        return proxy;

    }
}
