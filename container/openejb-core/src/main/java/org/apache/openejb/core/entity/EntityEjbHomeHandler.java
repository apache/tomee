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
package org.apache.openejb.core.entity;

import org.apache.openejb.BeanContext;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.core.ivm.EjbHomeProxyHandler;
import org.apache.openejb.core.ivm.EjbObjectProxyHandler;
import org.apache.openejb.util.ArrayEnumeration;
import org.apache.openejb.util.proxy.ProxyManager;

import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.RemoveException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;


public class EntityEjbHomeHandler extends EjbHomeProxyHandler {

    public EntityEjbHomeHandler(BeanContext beanContext, InterfaceType interfaceType, List<Class> interfaces, Class mainInterface) {
        super(beanContext, interfaceType, interfaces, mainInterface);
    }

    public Object createProxy(Object primaryKey, Class mainInterface) {
        Object proxy = super.createProxy(primaryKey, mainInterface);
        EjbObjectProxyHandler handler = (EjbObjectProxyHandler) ProxyManager.getInvocationHandler(proxy);

        /* 
        * Register the handle with the BaseEjbProxyHandler.liveHandleRegistry
        * If the bean is removed by its home or by an identical proxy, then the 
        * this proxy will be automatically invalidated because its properly registered
        * with the liveHandleRegistry.
        */
        registerHandler(handler.getRegistryId(), handler);

        return proxy;

    }

    protected Object findX(Class interfce, Method method, Object[] args, Object proxy) throws Throwable {
        Object retValue;
        try {
            retValue = container.invoke(deploymentID, interfaceType, interfce, method, args, null);
        } catch (OpenEJBException e) {
            logger.debug("entityEjbHomeHandler.containerInvocationFailure", e, e.getMessage());
            throw e;
        }

        if (retValue instanceof Collection) {
            Object [] proxyInfos = ((Collection) retValue).toArray();
            Vector proxies = new Vector();
            for (int i = 0; i < proxyInfos.length; i++) {
                ProxyInfo proxyInfo = (ProxyInfo) proxyInfos[i];
                proxies.addElement(createProxy(proxyInfo.getPrimaryKey(), getMainInterface()));
            }
            return proxies;
        } else if (retValue instanceof ArrayEnumeration) {
            ArrayEnumeration enumeration = (ArrayEnumeration) retValue;
            for (int i = enumeration.size() - 1; i >= 0; --i) {
                ProxyInfo proxyInfo = (ProxyInfo) enumeration.get(i);
                enumeration.set(i, createProxy(proxyInfo.getPrimaryKey(), getMainInterface()));
            }
            return enumeration;
        } else if (retValue instanceof Enumeration) {
            Enumeration enumeration = (Enumeration) retValue;

            List proxies = new ArrayList();
            while (enumeration.hasMoreElements()) {
                ProxyInfo proxyInfo = (ProxyInfo) enumeration.nextElement();
                proxies.add(createProxy(proxyInfo.getPrimaryKey(), getMainInterface()));
            }
            return new ArrayEnumeration(proxies);
        } else {
            ProxyInfo proxyInfo = (ProxyInfo) retValue;


            return createProxy(proxyInfo.getPrimaryKey(), getMainInterface());
        }

    }

    protected Object removeByPrimaryKey(Class interfce, Method method, Object[] args, Object proxy) throws Throwable {
        Object primKey = args[0];

        // Check for the common mistake of passing the ejbObject instead of ejbObject.getPrimaryKey()
        if (primKey instanceof EJBLocalObject) {
            Class ejbObjectProxyClass = primKey.getClass();

            String ejbObjectName = null;
            for (Class clazz : ejbObjectProxyClass.getInterfaces()) {
                if (EJBLocalObject.class.isAssignableFrom(clazz)) {
                    ejbObjectName = clazz.getSimpleName();
                    break;
                }
            }

            throw new RemoveException("Invalid argument '" + ejbObjectName + "', expected primary key.  Update to ejbLocalHome.remove(" + lcfirst(ejbObjectName) + ".getPrimaryKey())");

        } else if (primKey instanceof EJBObject) {
            Class ejbObjectProxyClass = primKey.getClass();

            String ejbObjectName = null;
            for (Class clazz : ejbObjectProxyClass.getInterfaces()) {
                if (EJBObject.class.isAssignableFrom(clazz)) {
                    ejbObjectName = clazz.getSimpleName();
                    break;
                }
            }

            throw new RemoveException("Invalid argument '" + ejbObjectName + "', expected primary key.  Update to ejbHome.remove(" + lcfirst(ejbObjectName) + ".getPrimaryKey())");
        }

        container.invoke(deploymentID, interfaceType, interfce, method, args, primKey);

        /* 
        * This operation takes care of invalidating all the EjbObjectProxyHanders associated with 
        * the same RegistryId. See this.createProxy().
        */
        invalidateAllHandlers(EntityEjbObjectHandler.getRegistryId(container, deploymentID, primKey));
        return null;
    }

    private static String lcfirst(String s){
        if (s == null || s.length() < 1) return s;

        StringBuilder sb = new StringBuilder(s);
        sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        return sb.toString();
    }

    protected EjbObjectProxyHandler newEjbObjectHandler(BeanContext beanContext, Object pk, InterfaceType interfaceType, List<Class> interfaces, Class mainInterface) {
        return new EntityEjbObjectHandler(getBeanContext(), pk, interfaceType, interfaces, mainInterface);
    }

}
