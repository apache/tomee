package org.openejb.core.stateless;

import org.openejb.Container;
import org.openejb.RpcContainer;
import org.openejb.core.ivm.EjbObjectProxyHandler;
import org.openejb.util.proxy.ProxyManager;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

public class StatelessEjbObjectHandler extends EjbObjectProxyHandler {
    public Object registryId;

    public StatelessEjbObjectHandler(RpcContainer container, Object pk, Object depID) {
        super(container, pk, depID, null);
    }

    public static Object createRegistryId(Object primKey, Object deployId, Container contnr) {
        return "" + deployId + contnr.getContainerID();
    }

    public Object getRegistryId() {
        if (registryId == null)
            registryId = createRegistryId(primaryKey, deploymentID, container);
        return registryId;
    }

    protected Object getPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable {
        throw new RemoteException("Session objects are private resources and do not have primary keys");
    }

    protected Object isIdentical(Method method, Object[] args, Object proxy) throws Throwable {
        checkAuthorization(method);

        try {
            EjbObjectProxyHandler handler = (EjbObjectProxyHandler) ProxyManager.getInvocationHandler(args[0]);
            return new Boolean(deploymentID.equals(handler.deploymentID));
        } catch (Throwable t) {
            return Boolean.FALSE;

        }
    }

    protected Object remove(Method method, Object[] args, Object proxy) throws Throwable {
        checkAuthorization(method);
        invalidateReference();
        return null;
    }

}
