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

import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.EJBObject;
import jakarta.ejb.RemoveException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;


public class EntityEjbHomeHandler extends EjbHomeProxyHandler {

    public EntityEjbHomeHandler(final BeanContext beanContext, final InterfaceType interfaceType, final List<Class> interfaces, final Class mainInterface) {
        super(beanContext, interfaceType, interfaces, mainInterface);
    }

    public Object createProxy(final Object primaryKey, final Class mainInterface) {
        final Object proxy = super.createProxy(primaryKey, mainInterface);
        final EjbObjectProxyHandler handler = (EjbObjectProxyHandler) ProxyManager.getInvocationHandler(proxy);

        /* 
        * Register the handle with the BaseEjbProxyHandler.liveHandleRegistry
        * If the bean is removed by its home or by an identical proxy, then the 
        * this proxy will be automatically invalidated because its properly registered
        * with the liveHandleRegistry.
        */
        registerHandler(handler.getRegistryId(), handler);

        return proxy;

    }

    protected Object findX(final Class interfce, final Method method, final Object[] args, final Object proxy) throws Throwable {
        final Object retValue;
        try {
            retValue = container.invoke(deploymentID, interfaceType, interfce, method, args, null);
        } catch (final OpenEJBException e) {
            logger.debug("entityEjbHomeHandler.containerInvocationFailure", e, e.getMessage());
            throw e;
        }

        if (retValue instanceof Collection) {
            final Object[] proxyInfos = ((Collection) retValue).toArray();
            final Vector<Object> proxies = new Vector<>();
            for (Object proxyInfo1 : proxyInfos) {
                final ProxyInfo proxyInfo = (ProxyInfo) proxyInfo1;
                proxies.addElement(createProxy(proxyInfo.getPrimaryKey(), getMainInterface()));
            }
            return proxies;
        } else if (retValue instanceof ArrayEnumeration) {
            @SuppressWarnings("unchecked") final ArrayEnumeration<Object> enumeration = (ArrayEnumeration<Object>) retValue;
            for (int i = enumeration.size() - 1; i >= 0; --i) {
                final ProxyInfo proxyInfo = (ProxyInfo) enumeration.get(i);
                enumeration.set(i, createProxy(proxyInfo.getPrimaryKey(), getMainInterface()));
            }
            return enumeration;
        } else if (retValue instanceof Enumeration) {
            final Enumeration enumeration = (Enumeration) retValue;

            final List<Object> proxies = new ArrayList<>();
            while (enumeration.hasMoreElements()) {
                final ProxyInfo proxyInfo = (ProxyInfo) enumeration.nextElement();
                proxies.add(createProxy(proxyInfo.getPrimaryKey(), getMainInterface()));
            }
            return new ArrayEnumeration<>(proxies);
        } else {
            final ProxyInfo proxyInfo = (ProxyInfo) retValue;


            return createProxy(proxyInfo.getPrimaryKey(), getMainInterface());
        }

    }

    protected Object removeByPrimaryKey(final Class interfce, final Method method, final Object[] args, final Object proxy) throws Throwable {
        final Object primKey = args[0];

        // Check for the common mistake of passing the ejbObject instead of ejbObject.getPrimaryKey()
        if (primKey instanceof EJBLocalObject) {
            final Class ejbObjectProxyClass = primKey.getClass();

            String ejbObjectName = null;
            for (final Class clazz : ejbObjectProxyClass.getInterfaces()) {
                if (EJBLocalObject.class.isAssignableFrom(clazz)) {
                    ejbObjectName = clazz.getSimpleName();
                    break;
                }
            }

            throw new RemoveException("Invalid argument '" + ejbObjectName + "', expected primary key.  Update to ejbLocalHome.remove(" + lcfirst(ejbObjectName) + ".getPrimaryKey())");

        } else if (primKey instanceof EJBObject) {
            final Class ejbObjectProxyClass = primKey.getClass();

            String ejbObjectName = null;
            for (final Class clazz : ejbObjectProxyClass.getInterfaces()) {
                if (EJBObject.class.isAssignableFrom(clazz)) {
                    ejbObjectName = clazz.getSimpleName();
                    break;
                }
            }

            throw new RemoveException("Invalid argument '" + ejbObjectName + "', expected primary key.  Update to ejbHome.remove(" + lcfirst(ejbObjectName) + ".getPrimaryKey())");
        }

        container.invoke(deploymentID, interfaceType, interfce, method, args, primKey);

        /* 
        * This operation takes care of invalidating all the EjbObjectProxyHandlers associated with
        * the same RegistryId. See this.createProxy().
        */
        invalidateAllHandlers(EntityEjbObjectHandler.getRegistryId(container, deploymentID, primKey));
        return null;
    }

    private static String lcfirst(final String s) {
        if (s == null || s.length() < 1) {
            return s;
        }

        final StringBuilder sb = new StringBuilder(s);
        sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        return sb.toString();
    }

    protected EjbObjectProxyHandler newEjbObjectHandler(final BeanContext beanContext, final Object pk, final InterfaceType interfaceType, final List<Class> interfaces, final Class mainInterface) {
        return new EntityEjbObjectHandler(getBeanContext(), pk, interfaceType, interfaces, mainInterface);
    }

}
