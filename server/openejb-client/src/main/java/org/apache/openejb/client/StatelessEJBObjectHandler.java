package org.apache.openejb.client;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

public class StatelessEJBObjectHandler extends EJBObjectHandler {

    public Object registryId;

    public StatelessEJBObjectHandler() {
    }

    public StatelessEJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client) {
        super(ejb, server, client);
    }

    public StatelessEJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client, Object primaryKey) {
        super(ejb, server, client, primaryKey);
    }

    public static Object createRegistryId(Object primKey, Object deployId, String containerID) {
        return "" + deployId + containerID;
    }

    public Object getRegistryId() {
        return this.ejb.deploymentID;
    }

    protected Object getPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable {
        throw new RemoteException("Session objects are private resources and do not have primary keys");
    }

    protected Object isIdentical(Method method, Object[] args, Object proxy) throws Throwable {

        Object arg = (args.length == 1) ? args[0] : null;

        if (arg == null || !(arg instanceof EJBObjectProxy)) return Boolean.FALSE;
        EJBObjectProxy proxy2 = (EJBObjectProxy) arg;
        EJBObjectHandler that = proxy2.getEJBObjectHandler();
        return new Boolean(this.ejb.deploymentID.equals(that.ejb.deploymentID));
    }

    protected Object remove(Method method, Object[] args, Object proxy) throws Throwable {
//      checkAuthorization(method);
        invalidateReference();
        return null;
    }

}
