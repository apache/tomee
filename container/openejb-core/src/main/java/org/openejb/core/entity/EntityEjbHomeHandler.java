package org.openejb.core.entity;

import java.lang.reflect.Method;
import java.util.Vector;

import org.openejb.ProxyInfo;
import org.openejb.RpcContainer;
import org.openejb.core.ivm.EjbHomeProxyHandler;
import org.openejb.core.ivm.EjbObjectProxyHandler;
import org.openejb.util.proxy.ProxyManager;


public class EntityEjbHomeHandler extends EjbHomeProxyHandler {

    public EntityEjbHomeHandler(RpcContainer container, Object pk, Object depID) {
        super(container, pk, depID);
    }

    protected Object createProxy(ProxyInfo proxyInfo) {
        Object proxy = super.createProxy(proxyInfo);
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

    protected Object findX(Method method, Object[] args, Object proxy) throws Throwable {
        Object retValue = container.invoke(deploymentID, method, args, null, getThreadSpecificSecurityIdentity());

        if (retValue instanceof java.util.Collection) {
            Object [] proxyInfos = ((java.util.Collection) retValue).toArray();
            Vector proxies = new Vector();
            for (int i = 0; i < proxyInfos.length; i++) {
                proxies.addElement(createProxy((ProxyInfo) proxyInfos[i]));
            }
            return proxies;
        } else if (retValue instanceof org.openejb.util.ArrayEnumeration) {
            org.openejb.util.ArrayEnumeration enum = (org.openejb.util.ArrayEnumeration) retValue;
            for (int i = enum.size() - 1; i >= 0; --i) {
                enum.set(i, createProxy((ProxyInfo) enum.get(i)));
            }
            return enum;
        } else if (retValue instanceof java.util.Enumeration) {
            java.util.Enumeration enum = (java.util.Enumeration) retValue;

            java.util.List proxies = new java.util.ArrayList();
            while (enum.hasMoreElements()) {
                proxies.add(createProxy((ProxyInfo) enum.nextElement()));
            }
            return new org.openejb.util.ArrayEnumeration(proxies);
        } else {
            org.openejb.ProxyInfo proxyInfo = (org.openejb.ProxyInfo) retValue;


            return createProxy(proxyInfo);
        }

    }

    protected Object removeByPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable {
        Object primKey = args[0];
        container.invoke(deploymentID, method, args, primKey, getThreadSpecificSecurityIdentity());

        /* 
        * This operation takes care of invalidating all the EjbObjectProxyHanders associated with 
        * the same RegistryId. See this.createProxy().
        */
        invalidateAllHandlers(EntityEjbObjectHandler.getRegistryId(primKey, deploymentID, container));
        return null;
    }

    protected EjbObjectProxyHandler newEjbObjectHandler(RpcContainer container, Object pk, Object depID) {
        return new EntityEjbObjectHandler(container, pk, depID);
    }

}
