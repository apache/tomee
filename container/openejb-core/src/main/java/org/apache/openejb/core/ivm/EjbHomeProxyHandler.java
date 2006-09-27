package org.apache.openejb.core.ivm;

import java.io.ObjectStreamException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.ejb.EJBException;

import org.apache.openejb.ProxyInfo;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ApplicationServer;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.util.proxy.ProxyManager;

public abstract class EjbHomeProxyHandler extends BaseEjbProxyHandler {
    protected final static org.apache.log4j.Category logger = org.apache.log4j.Category.getInstance("OpenEJB");

    static final java.util.HashMap dispatchTable;

    static {
        dispatchTable = new java.util.HashMap();
        dispatchTable.put("create", new Integer(1));
        dispatchTable.put("getEJBMetaData", new Integer(2));
        dispatchTable.put("getHomeHandle", new Integer(3));
        dispatchTable.put("remove", new Integer(4));
    }

    public EjbHomeProxyHandler(RpcContainer container, Object pk, Object depID) {
        super(container, pk, depID);
    }

    public void invalidateReference() {
        throw new IllegalStateException("A home reference must never be invalidated!");
    }

    protected Object createProxy(ProxyInfo proxyInfo) {

        Object newProxy = null;
        try {
            EjbObjectProxyHandler handler = newEjbObjectHandler(proxyInfo.getBeanContainer(), proxyInfo.getPrimaryKey(), proxyInfo.getDeploymentInfo().getDeploymentID());
            handler.setLocal(isLocal());
            handler.doIntraVmCopy = this.doIntraVmCopy;
            Class[] interfaces = new Class[]{proxyInfo.getInterface(), IntraVmProxy.class};
            newProxy = ProxyManager.newProxyInstance(interfaces, handler);
        } catch (IllegalAccessException iae) {
            throw new RuntimeException("Could not create IVM proxy for " + proxyInfo.getInterface() + " interface");
        }
        if (newProxy == null) throw new RuntimeException("Could not create IVM proxy for " + proxyInfo.getInterface() + " interface");

        return newProxy;
    }

    protected abstract EjbObjectProxyHandler newEjbObjectHandler(RpcContainer container, Object pk, Object depID);

    protected Object _invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (logger.isInfoEnabled()) {
            logger.info("invoking method " + method.getName() + " on " + deploymentID);
        }

        String methodName = method.getName();

        try {
            java.lang.Object retValue;
            Integer operation = (Integer) dispatchTable.get(methodName);

            if (operation == null) {
                if (methodName.startsWith("find")) {
                    retValue = findX(method, args, proxy);
                } else {

                    throw new UnsupportedOperationException("Unkown method: " + method);
                }
            } else {
                switch (operation.intValue()) {
                    /*-- CREATE ------------- <HomeInterface>.create(<x>) ---*/
                    case 1:
                        retValue = create(method, args, proxy);
                        break;
                        /*-- GET EJB METADATA ------ EJBHome.getEJBMetaData() ---*/
                    case 2:
                        retValue = getEJBMetaData(method, args, proxy);
                        break;
                        /*-- GET HOME HANDLE -------- EJBHome.getHomeHandle() ---*/
                    case 3:
                        retValue = getHomeHandle(method, args, proxy);
                        break;
                        /*-- REMOVE ------------------------ EJBHome.remove() ---*/
                    case 4: {
                        Class type = method.getParameterTypes()[0];

                        /*-- HANDLE ------- EJBHome.remove(Handle handle) ---*/
                        if (javax.ejb.Handle.class.isAssignableFrom(type)) {
                            retValue = removeWithHandle(method, args, proxy);
                        } else {
                            /*-- PRIMARY KEY ----- EJBHome.remove(Object key) ---*/
                            retValue = removeByPrimaryKey(method, args, proxy);
                        }
                        break;
                    }
                    default:
                        throw new RuntimeException("Inconsistent internal state: value " + operation.intValue() + " for operation " + methodName);
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("finished invoking method " + method.getName() + ". Return value:" + retValue);
            } else if (logger.isInfoEnabled()) {
                logger.info("finished invoking method " + method.getName());
            }

            return retValue;

            /*
            * The ire is thrown by the container system and propagated by
            * the server to the stub.
            */
        } catch (RemoteException re) {
            if (isLocal()) {
                throw new EJBException(re.getMessage(), (Exception) re.detail);
            } else {
                throw re;
            }

        } catch (org.apache.openejb.InvalidateReferenceException ire) {
            Throwable cause = ire.getRootCause();
            if (cause instanceof RemoteException && isLocal()) {
                RemoteException re = (RemoteException) cause;
                Throwable detail = (re.detail != null) ? re.detail : re;
                cause = new EJBException(re.getMessage(), (Exception) detail);
            }
            throw cause;
            /*
            * Application exceptions must be reported dirctly to the client. They
            * do not impact the viability of the proxy.
            */
        } catch (org.apache.openejb.ApplicationException ae) {
            throw ae.getRootCause();
            /*
            * A system exception would be highly unusual and would indicate a sever
            * problem with the container system.
            */
        } catch (org.apache.openejb.SystemException se) {
            if (isLocal()) {
                throw new EJBException("Container has suffered a SystemException", (Exception) se.getRootCause());
            } else {
                throw new RemoteException("Container has suffered a SystemException", se.getRootCause());
            }
        } catch (org.apache.openejb.OpenEJBException oe) {
            if (isLocal()) {
                throw new EJBException("Unknown Container Exception", (Exception) oe.getRootCause());
            } else {
                throw new RemoteException("Unknown Container Exception", oe.getRootCause());
            }
        } catch (Throwable t) {
            logger.info("finished invoking method " + method.getName() + " with exception:" + t, t);
            throw t;
        }
    }

    /*-------------------------------------------------*/
    /*  Home interface methods                         */
    /*-------------------------------------------------*/

    protected Object create(Method method, Object[] args, Object proxy) throws Throwable {
        ProxyInfo proxyInfo = (ProxyInfo) container.invoke(deploymentID, method, args, null, getThreadSpecificSecurityIdentity());
        return createProxy(proxyInfo);
    }

    protected abstract Object findX(Method method, Object[] args, Object proxy) throws Throwable;

    /*-------------------------------------------------*/
    /*  EJBHome methods                                */
    /*-------------------------------------------------*/

    protected Object getEJBMetaData(Method method, Object[] args, Object proxy) throws Throwable {
        checkAuthorization(method);
        IntraVmMetaData metaData = new IntraVmMetaData(deploymentInfo.getHomeInterface(), deploymentInfo.getRemoteInterface(), deploymentInfo.getPrimaryKeyClass(), deploymentInfo.getComponentType());
        metaData.setEJBHome((EJBHome) proxy);
        return metaData;
    }

    protected Object getHomeHandle(Method method, Object[] args, Object proxy) throws Throwable {
        checkAuthorization(method);
        return new IntraVmHandle(proxy);
    }

    public org.apache.openejb.ProxyInfo getProxyInfo() {
        return new org.apache.openejb.ProxyInfo(deploymentInfo, null, deploymentInfo.getHomeInterface(), container);
    }

    protected Object _writeReplace(Object proxy) throws ObjectStreamException {
        /*
         * If the proxy is being  copied between bean instances in a RPC
         * call we use the IntraVmArtifact
         */
        if (IntraVmCopyMonitor.isIntraVmCopyOperation()) {
            return new IntraVmArtifact(proxy);
            /*
            * If the proxy is referenced by a stateful bean that is  being
            * passivated by the container we allow this object to be serialized.
            */
        } else if (IntraVmCopyMonitor.isStatefulPassivationOperation()) {
            return proxy;
            /*
            * If the proxy is serialized outside the core container system,
            * we allow the application server to handle it.
            */
        } else {
            return ((ApplicationServer) SystemInstance.get().getComponent(ApplicationServer.class)).getEJBHome(this.getProxyInfo());
        }
    }

    protected Object removeWithHandle(Method method, Object[] args, Object proxy) throws Throwable {

        IntraVmHandle handle = (IntraVmHandle) args[0];
        Object primKey = handle.getPrimaryKey();
        EjbObjectProxyHandler stub;
        try {
            stub = (EjbObjectProxyHandler) ProxyManager.getInvocationHandler(handle.getEJBObject());
        } catch (IllegalArgumentException e) {

            stub = null;
        }

        container.invoke(deploymentID, method, args, primKey, ThreadContext.getThreadContext().getSecurityIdentity());

        /*
         * This operation takes care of invalidating all the EjbObjectProxyHanders associated with
         * the same RegistryId. See this.createProxy().
         */
        if (stub != null) {
            invalidateAllHandlers(stub.getRegistryId());
        }
        return null;
    }

    protected abstract Object removeByPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable;
}
