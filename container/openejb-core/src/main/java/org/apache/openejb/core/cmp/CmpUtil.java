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

import org.apache.openejb.core.ivm.EjbObjectProxyHandler;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.entity.EntityEjbHomeHandler;
import org.apache.openejb.util.proxy.ProxyManager;
import org.apache.openejb.alt.containers.castor_cmp11.KeyGenerator;
import org.apache.openejb.ProxyInfo;

import javax.ejb.EJBLocalObject;
import javax.ejb.EntityBean;
import javax.ejb.EJBLocalHome;

public class CmpUtil {
    public static EntityBean getEntityBean(CoreDeploymentInfo deploymentInfo, EJBLocalObject proxy) {
        if (proxy == null) return null;

        EjbObjectProxyHandler handler = (EjbObjectProxyHandler) ProxyManager.getInvocationHandler(proxy);
        if (!(handler.container instanceof CmpContainer)) {
            throw new IllegalArgumentException("Proxy is not connected to a CMP container but is conect to " + handler.container.getClass().getName());
        }
        CmpContainer container = (CmpContainer) handler.container;
        EntityBean entity = (EntityBean) container.getEjbInstance(handler.deploymentInfo, handler.primaryKey);
        return entity;
    }

    public static EJBLocalObject getEjbProxy(CoreDeploymentInfo deploymentInfo, EntityBean entity){
        if (entity == null) return null;

        // build the primary key
        KeyGenerator kg = deploymentInfo.getKeyGenerator();
        Object primaryKey = kg.getPrimaryKey(entity);

        // get the local interface
        Class localInterface = deploymentInfo.getLocalInterface();

        // get the cmp container
        if (!(deploymentInfo.getContainer() instanceof CmpContainer)) {
            throw new IllegalArgumentException("Proxy is not connected to a CMP container but is conect to " + deploymentInfo.getContainer().getClass().getName());
        }
        CmpContainer container = (CmpContainer) deploymentInfo.getContainer();

        // create a new ProxyInfo based on the deployment info and primary key
        ProxyInfo proxyInfo = new ProxyInfo(deploymentInfo, primaryKey, localInterface, container);

        // get the home proxy handler
        EJBLocalHome homeProxy = deploymentInfo.getEJBLocalHome();
        EntityEjbHomeHandler handler = (EntityEjbHomeHandler) ProxyManager.getInvocationHandler(homeProxy);

        // create the proxy
        EJBLocalObject proxy = (EJBLocalObject) handler.createProxy(proxyInfo);
        return proxy;
    }
}
