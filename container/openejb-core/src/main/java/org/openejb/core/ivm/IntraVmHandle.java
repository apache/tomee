package org.openejb.core.ivm;

import java.io.ObjectStreamException;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;

import org.openejb.util.proxy.ProxyManager;

public class IntraVmHandle implements java.io.Serializable, javax.ejb.HomeHandle, javax.ejb.Handle {
    protected Object theProxy;

    public IntraVmHandle() {
        this(null);
    }

    public IntraVmHandle(Object proxy) {
        this.theProxy = proxy;
    }

    protected void setProxy(Object prxy) {
        theProxy = prxy;
    }

    public EJBHome getEJBHome() {
        return (EJBHome) theProxy;
    }

    public EJBObject getEJBObject() {
        return (EJBObject) theProxy;
    }

    public Object getPrimaryKey() {
        return ((BaseEjbProxyHandler) org.openejb.util.proxy.ProxyManager.getInvocationHandler(theProxy)).primaryKey;
    }

    protected Object writeReplace() throws ObjectStreamException {
        /*
         * If the handle is being  copied between bean instances in a RPC
         * call we use the IntraVmArtifact 
         */
        if (IntraVmCopyMonitor.isIntraVmCopyOperation()) {
            return new IntraVmArtifact(this);
            /*
            * If the handle is referenced by a stateful bean that is being
            * passivated by the container, we allow this object to be serialized.
            */
        } else if (IntraVmCopyMonitor.isStatefulPassivationOperation()) {
            return this;
            /*
            * If the handle is serialized outside the core container system, we
            * allow the application server to handle it.
            */
        } else {
            BaseEjbProxyHandler handler = (BaseEjbProxyHandler) ProxyManager.getInvocationHandler(theProxy);
            if (theProxy instanceof javax.ejb.EJBObject) {
                return org.openejb.OpenEJB.getApplicationServer().getHandle(handler.getProxyInfo());
            } else if (theProxy instanceof javax.ejb.EJBHome) {
                return org.openejb.OpenEJB.getApplicationServer().getHomeHandle(handler.getProxyInfo());
            } else {
                throw new RuntimeException("Invalid proxy type. Handles are only supported by EJBObject types in EJB 1.1");
            }
        }
    }

}