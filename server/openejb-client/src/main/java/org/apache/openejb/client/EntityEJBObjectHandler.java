package org.apache.openejb.client;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

public class EntityEJBObjectHandler extends EJBObjectHandler {

    public EntityEJBObjectHandler() {
    }

    public EntityEJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client) {
        super(ejb, server, client);
    }

    public EntityEJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client, Object primaryKey) {
        super(ejb, server, client, primaryKey);
        registryId = ejb.deploymentID + ":" + primaryKey;
        registerHandler(registryId, this);
    }

    public Object getRegistryId() {
        return registryId;
    }

    protected Object getPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable {
        return primaryKey;
    }

    protected Object isIdentical(Method method, Object[] args, Object proxy) throws Throwable {
        if (args[0] == null) return Boolean.FALSE;

        EJBObjectProxy ejbObject = (EJBObjectProxy) args[0];
        EJBObjectHandler that = ejbObject.getEJBObjectHandler();

        return new Boolean(this.registryId.equals(that.registryId));

    }

    protected Object remove(Method method, Object[] args, Object proxy) throws Throwable {

        EJBRequest req = new EJBRequest(EJB_OBJECT_REMOVE);

        req.setMethodParameters(args);
        req.setMethodInstance(method);
        req.setClientIdentity(client.getClientIdentity());
        req.setDeploymentCode(ejb.deploymentCode);
        req.setDeploymentId(ejb.deploymentID);
        req.setPrimaryKey(primaryKey);

        EJBResponse res = request(req);

        switch (res.getResponseCode()) {
            case EJB_ERROR:
                throw new SystemError((ThrowableArtifact) res.getResult());
            case EJB_SYS_EXCEPTION:
                throw new SystemException((ThrowableArtifact) res.getResult());
            case EJB_APP_EXCEPTION:
                throw new ApplicationException((ThrowableArtifact) res.getResult());
            case EJB_OK:
                invalidateAllHandlers(getRegistryId());
                return null;
            default:
                throw new RemoteException("Received invalid response code from server: " + res.getResponseCode());
        }
    }

}
