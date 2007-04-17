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
package org.apache.openejb.core.cmp.cmp2;

import org.apache.openejb.core.ivm.EjbObjectProxyHandler;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.cmp.CmpContainer;
import org.apache.openejb.core.cmp.KeyGenerator;
import org.apache.openejb.util.proxy.ProxyManager;
import org.apache.openejb.InterfaceType;

import javax.ejb.EJBLocalObject;
import javax.ejb.EntityBean;
import javax.ejb.EJBObject;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class Cmp2Util {
    public static Object getPrimaryKey(CoreDeploymentInfo deploymentInfo, EntityBean entity){
        if (entity == null) return null;

        // build the primary key
        KeyGenerator kg = deploymentInfo.getKeyGenerator();
        Object primaryKey = kg.getPrimaryKey(entity);
        return primaryKey;
    }

    public static <Bean extends EntityBean> Bean getEntityBean(EJBLocalObject proxy) {
        if (proxy == null) return null;

        EjbObjectProxyHandler handler = (EjbObjectProxyHandler) ProxyManager.getInvocationHandler(proxy);
        if (handler.container == null) {
            return null;
        }
        if (!(handler.container instanceof CmpContainer)) {
            throw new IllegalArgumentException("Proxy is not connected to a CMP container but is conect to " + handler.container.getClass().getName());
        }
        CmpContainer container = (CmpContainer) handler.container;
        Bean entity = (Bean) container.getEjbInstance(handler.getDeploymentInfo(), handler.primaryKey);
        return entity;
    }

    public static <Bean extends EntityBean> Bean getEntityBean(EJBObject proxy) {
        if (proxy == null) return null;

        EjbObjectProxyHandler handler = (EjbObjectProxyHandler) ProxyManager.getInvocationHandler(proxy);
        if (handler.container == null) {
            return null;
        }
        if (!(handler.container instanceof CmpContainer)) {
            throw new IllegalArgumentException("Proxy is not connected to a CMP container but is conect to " + handler.container.getClass().getName());
        }
        CmpContainer container = (CmpContainer) handler.container;
        Bean entity = (Bean) container.getEjbInstance(handler.getDeploymentInfo(), handler.primaryKey);
        return entity;
    }

    public static <Proxy extends EJBLocalObject> Proxy getEjbProxy(CoreDeploymentInfo deploymentInfo, EntityBean entity){
        if (entity == null) return null;

        // build the primary key
        Object primaryKey = getPrimaryKey(deploymentInfo, entity);

        // get the cmp container
        if (!(deploymentInfo.getContainer() instanceof CmpContainer)) {
            throw new IllegalArgumentException("Proxy is not connected to a CMP container but is conect to " + deploymentInfo.getContainer().getClass().getName());
        }

        Proxy proxy = (Proxy) EjbObjectProxyHandler.createProxy(deploymentInfo, primaryKey, InterfaceType.EJB_LOCAL_HOME);
        return proxy;
    }

    public static CoreDeploymentInfo getDeploymentInfo(Class type) {
        CoreDeploymentInfo deploymentInfo;
        try {
            Field deploymentInfoField = type.getField("deploymentInfo");
            deploymentInfo = (CoreDeploymentInfo) deploymentInfoField.get(null);
        } catch (Exception e) {
            throw new IllegalArgumentException("EntityBean class " + type.getName() +
                    " does not contain a deploymentInfo field.  Is this a generated CMP 2 entity implementation?");
        }
        return deploymentInfo;
    }
}
