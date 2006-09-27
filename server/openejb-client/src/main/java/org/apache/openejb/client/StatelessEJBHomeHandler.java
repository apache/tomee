package org.apache.openejb.client;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

public class StatelessEJBHomeHandler extends EJBHomeHandler {

    public StatelessEJBHomeHandler() {
    }

    public StatelessEJBHomeHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client) {
        super(ejb, server, client);
    }

    protected Object findX(Method method, Object[] args, Object proxy) throws Throwable {
        throw new UnsupportedOperationException("Stateful beans may not have find methods");
    }

    protected Object removeByPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable {
        throw new RemoteException("Session objects are private resources and do not have primary keys");
    }

    /*
    * TODO:3: Get a related quote from the specification to add here
    *
    * This method is differnt the the stateful and entity behavior because we only want the 
    * stateless session bean that created the proxy to be invalidated, not all the proxies. Special case
    * for the stateless session beans.
    */
    protected Object removeWithHandle(Method method, Object[] args, Object proxy) throws Throwable {

        EJBObjectHandle handle = (EJBObjectHandle) args[0];

        if (handle == null) throw new NullPointerException("The handle is null");

        EJBObjectHandler handler = (EJBObjectHandler) handle.ejbObjectProxy.getEJBObjectHandler();

        if (!handler.ejb.deploymentID.equals(this.ejb.deploymentID)) {
            throw new IllegalArgumentException("The handle is not from the same deployment");
        }
        handler.invalidateReference();

        return null;
    }

    protected EJBObjectHandler newEJBObjectHandler() {
        return new StatelessEJBObjectHandler();
    }

}
