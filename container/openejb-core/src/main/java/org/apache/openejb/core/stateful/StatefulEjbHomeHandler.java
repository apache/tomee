package org.apache.openejb.core.stateful;

import org.apache.openejb.ProxyInfo;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.core.ivm.EjbHomeProxyHandler;
import org.apache.openejb.core.ivm.EjbObjectProxyHandler;
import org.apache.openejb.util.proxy.ProxyManager;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

public class StatefulEjbHomeHandler extends EjbHomeProxyHandler {

    public StatefulEjbHomeHandler(RpcContainer container, Object pk, Object depID) {
        super(container, pk, depID);
    }

    protected Object createProxy(ProxyInfo proxyInfo) {
        Object proxy = super.createProxy(proxyInfo);
        EjbObjectProxyHandler handler = (EjbObjectProxyHandler) ProxyManager.getInvocationHandler(proxy);

        registerHandler(handler.getRegistryId(), handler);

        return proxy;

    }

    protected Object findX(Method method, Object[] args, Object proxy) throws Throwable {
        throw new UnsupportedOperationException("Stateful beans may not have find methods");
    }

    protected Object removeByPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable {
        throw new RemoteException("Session objects are private resources and do not have primary keys");
    }

    protected EjbObjectProxyHandler newEjbObjectHandler(RpcContainer container, Object pk, Object depID) {
        return new StatefulEjbObjectHandler(container, pk, depID);
    }

}
