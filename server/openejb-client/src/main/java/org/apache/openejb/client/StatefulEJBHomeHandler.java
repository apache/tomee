package org.apache.openejb.client;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

public class StatefulEJBHomeHandler extends EJBHomeHandler {

    public StatefulEJBHomeHandler() {
    }

    public StatefulEJBHomeHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client) {
        super(ejb, server, client);
    }

    protected Object findX(Method method, Object[] args, Object proxy) throws Throwable {
        throw new UnsupportedOperationException("Stateful beans may not have find methods");
    }

    protected Object removeByPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable {
        throw new RemoteException("Session objects are private resources and do not have primary keys");
    }

    protected Object removeWithHandle(Method method, Object[] args, Object proxy) throws Throwable {

        EJBObjectHandle handle = (EJBObjectHandle) args[0];

        if (handle == null) throw new NullPointerException("The handle is null");

        EJBObjectHandler handler = handle.handler;
        Object primKey = handler.primaryKey;

        if (!handler.ejb.deploymentID.equals(this.ejb.deploymentID)) {
            throw new IllegalArgumentException("The handle is not from the same deployment");
        }

        EJBRequest req = new EJBRequest(EJB_HOME_REMOVE_BY_HANDLE);
        req.setClientIdentity(client.getClientIdentity());
        req.setDeploymentCode(handler.ejb.deploymentCode);
        req.setDeploymentId(handler.ejb.deploymentID);
        req.setMethodInstance(method);
        req.setMethodParameters(args);
        req.setPrimaryKey(primKey);

        EJBResponse res = request(req);

        if (res.getResponseCode() == res.EJB_ERROR) {
            throw (Throwable) res.getResult();
        }

        invalidateAllHandlers(handler.getRegistryId());
        handler.invalidateReference();
        return null;
    }
}
