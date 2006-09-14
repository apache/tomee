package org.openejb.core.stateful;

import org.openejb.RpcContainer;
import org.openejb.core.ivm.EjbObjectProxyHandler;
import org.openejb.util.proxy.ProxyManager;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

public class StatefulEjbObjectHandler extends EjbObjectProxyHandler {

    public StatefulEjbObjectHandler(RpcContainer container, Object pk, Object depID) {
        super(container, pk, depID, null);
    }

    public Object getRegistryId() {
        return primaryKey;
    }

    protected Object getPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable {
        throw new RemoteException("Session objects are private resources and do not have primary keys");
    }

    protected Object isIdentical(Method method, Object[] args, Object proxy) throws Throwable {
        checkAuthorization(method);
        EjbObjectProxyHandler handler = (EjbObjectProxyHandler) ProxyManager.getInvocationHandler(proxy);
        return new Boolean(primaryKey.equals(handler.primaryKey));
    }

    protected Object remove(Method method, Object[] args, Object proxy) throws Throwable {
        checkAuthorization(method);
        Object value = container.invoke(deploymentID, method, args, primaryKey, getThreadSpecificSecurityIdentity());

        invalidateAllHandlers(getRegistryId());
        return value;
    }

}
