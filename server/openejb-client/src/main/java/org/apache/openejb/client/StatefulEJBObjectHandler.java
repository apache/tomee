package org.apache.openejb.client;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

public class StatefulEJBObjectHandler extends EJBObjectHandler {

    public StatefulEJBObjectHandler() {
    }

    public StatefulEJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client) {
        super(ejb, server, client);
    }

    public StatefulEJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client, Object primaryKey) {
        super(ejb, server, client, primaryKey);
        registerHandler(primaryKey, this);
    }

    public Object getRegistryId() {
        return primaryKey;
    }

    protected Object getPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable {
        throw new RemoteException("Session objects are private resources and do not have primary keys");
    }

    protected Object isIdentical(Method method, Object[] args, Object proxy) throws Throwable {
        if (args[0] == null) return Boolean.FALSE;

        EJBObjectProxy ejbObject = (EJBObjectProxy) args[0];
        EJBObjectHandler that = ejbObject.getEJBObjectHandler();

        return new Boolean(this.primaryKey.equals(that.primaryKey));
    }

    protected Object remove(Method method, Object[] args, Object proxy) throws Throwable {

        EJBRequest req = new EJBRequest(EJB_OBJECT_REMOVE);
        req.setClientIdentity(client.getClientIdentity());
        req.setDeploymentCode(ejb.deploymentCode);
        req.setDeploymentId(ejb.deploymentID);
        req.setMethodInstance(method);
        req.setMethodParameters(args);
        req.setPrimaryKey(primaryKey);

        EJBResponse res = request(req);

        if (EJB_ERROR == res.getResponseCode()) {
            throw (Throwable) res.getResult();
        }

        invalidateAllHandlers(this.getRegistryId());
        this.invalidateReference();
        return null;
    }

}
