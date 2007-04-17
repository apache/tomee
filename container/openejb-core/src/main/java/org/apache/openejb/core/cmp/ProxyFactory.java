/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.cmp;

import org.apache.openejb.RpcContainer;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.entity.EntityEjbHomeHandler;
import org.apache.openejb.util.proxy.ProxyManager;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EntityBean;

public class ProxyFactory {
    private final CoreDeploymentInfo deploymentInfo;
    private final KeyGenerator keyGenerator;
    private final Class remoteInterface;
    private final EntityEjbHomeHandler remoteHandler;
    private final Class localInterface;
    private final EntityEjbHomeHandler localHandler;

    public ProxyFactory(CoreDeploymentInfo deploymentInfo) {
        this.deploymentInfo = deploymentInfo;
        keyGenerator = deploymentInfo.getKeyGenerator();

        remoteInterface = deploymentInfo.getRemoteInterface();
        if (remoteInterface != null) {
            EJBHome homeProxy = deploymentInfo.getEJBHome();
            remoteHandler = (EntityEjbHomeHandler) ProxyManager.getInvocationHandler(homeProxy);
        } else {
            remoteHandler = null;
        }

        localInterface = deploymentInfo.getLocalInterface();
        if (localInterface != null) {
            EJBLocalHome localHomeProxy = deploymentInfo.getEJBLocalHome();
            localHandler = (EntityEjbHomeHandler) ProxyManager.getInvocationHandler(localHomeProxy);
        } else {
            localHandler = null;
        }
    }

    public Object createRemoteProxy(EntityBean bean, RpcContainer container) {
        // The KeyGenerator creates a new primary key and populates its fields with the
        // primary key fields of the bean instance.  Each deployment has its own KeyGenerator.
        Object primaryKey = keyGenerator.getPrimaryKey(bean);

        // create the proxy
        Object proxy = remoteHandler.createProxy(primaryKey);
        return proxy;
    }

    public Object createLocalProxy(EntityBean bean, RpcContainer container) {
        // The KeyGenerator creates a new primary key and populates its fields with the
        // primary key fields of the bean instance.  Each deployment has its own KeyGenerator.
        Object primaryKey = keyGenerator.getPrimaryKey(bean);

        // create the proxy
        Object proxy = localHandler.createProxy(primaryKey);
        return proxy;

    }
}
