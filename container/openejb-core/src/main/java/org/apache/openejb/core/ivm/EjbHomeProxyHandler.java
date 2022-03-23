/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.core.ivm;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.BeanContext;
import org.apache.openejb.BeanType;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.InvalidateReferenceException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.core.entity.EntityEjbHomeHandler;
import org.apache.openejb.core.managed.ManagedHomeHandler;
import org.apache.openejb.core.singleton.SingletonEjbHomeHandler;
import org.apache.openejb.core.stateful.StatefulEjbHomeHandler;
import org.apache.openejb.core.stateless.StatelessEjbHomeHandler;
import org.apache.openejb.spi.ApplicationServer;
import org.apache.openejb.threads.task.CUCallable;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.proxy.LocalBeanProxyFactory;
import org.apache.openejb.util.proxy.ProxyManager;

import jakarta.ejb.AccessLocalException;
import jakarta.ejb.EJBAccessException;
import jakarta.ejb.EJBException;
import jakarta.ejb.EJBHome;
import jakarta.ejb.Handle;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public abstract class EjbHomeProxyHandler extends BaseEjbProxyHandler {

    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");

    private final Map<String, MethodType> dispatchTable;

    private static enum MethodType {
        CREATE,
        FIND,
        HOME_HANDLE,
        META_DATA,
        REMOVE
    }

    public EjbHomeProxyHandler(final BeanContext beanContext, final InterfaceType interfaceType, final List<Class> interfaces, final Class mainInterface) {
        super(beanContext, null, interfaceType, interfaces, mainInterface);
        dispatchTable = new HashMap<>();
        dispatchTable.put("create", MethodType.CREATE);
        dispatchTable.put("getEJBMetaData", MethodType.META_DATA);
        dispatchTable.put("getHomeHandle", MethodType.HOME_HANDLE);
        dispatchTable.put("remove", MethodType.REMOVE);

        if (interfaceType.isHome()) {
            final Class homeInterface = beanContext.getInterface(interfaceType);
            final Method[] methods = homeInterface.getMethods();
            for (final Method method : methods) {
                if (method.getName().startsWith("create")) {
                    dispatchTable.put(method.getName(), MethodType.CREATE);
                } else if (method.getName().startsWith("find")) {
                    dispatchTable.put(method.getName(), MethodType.FIND);
                }
            }
        }

    }

    @Override
    public void invalidateReference() {
        throw new IllegalStateException("A home reference must never be invalidated!");
    }

    protected static EjbHomeProxyHandler createHomeHandler(final BeanContext beanContext,
                                                           final InterfaceType interfaceType,
                                                           final List<Class> interfaces,
                                                           final Class mainInterface) {
        switch (beanContext.getComponentType()) {
            case STATEFUL:
                return new StatefulEjbHomeHandler(beanContext, interfaceType, interfaces, mainInterface);
            case STATELESS:
                return new StatelessEjbHomeHandler(beanContext, interfaceType, interfaces, mainInterface);
            case SINGLETON:
                return new SingletonEjbHomeHandler(beanContext, interfaceType, interfaces, mainInterface);
            case MANAGED:
                return new ManagedHomeHandler(beanContext, interfaceType, interfaces, mainInterface);
            case CMP_ENTITY:
            case BMP_ENTITY:
                return new EntityEjbHomeHandler(beanContext, interfaceType, interfaces, mainInterface);
            default:
                throw new IllegalStateException("Component type does not support rpc interfaces: " + beanContext.getComponentType());
        }
    }

    public static Object createHomeProxy(final BeanContext beanContext, final InterfaceType interfaceType) {
        return createHomeProxy(beanContext, interfaceType, null, interfaceType.isRemote() ? beanContext.getRemoteInterface() : beanContext.getLocalInterface());
    }

    public static Object createHomeProxy(final BeanContext beanContext, final InterfaceType interfaceType, final List<Class> objectInterfaces, final Class mainInterface) {
        if (!interfaceType.isHome()) {
            throw new IllegalArgumentException("InterfaceType is not a Home type: " + interfaceType);
        }

        try {
            final EjbHomeProxyHandler handler = createHomeHandler(beanContext, interfaceType, objectInterfaces, mainInterface);

            final List<Class> proxyInterfaces = new ArrayList<>(2);

            final Class homeInterface = beanContext.getInterface(interfaceType);
            proxyInterfaces.add(homeInterface);
            proxyInterfaces.add(IntraVmProxy.class);
            if (BeanType.STATEFUL.equals(beanContext.getComponentType()) || BeanType.MANAGED.equals(beanContext.getComponentType())) {
                proxyInterfaces.add(BeanContext.Removable.class);
            }

            return ProxyManager.newProxyInstance(proxyInterfaces.toArray(new Class[proxyInterfaces.size()]), handler);
        } catch (final Exception e) {
            throw new OpenEJBRuntimeException("Can't create EJBHome stub" + e.getMessage(), e);
        }
    }

    public Object createProxy(final Object primaryKey, final Class mainInterface) {
        try {

            final InterfaceType objectInterfaceType = this.interfaceType.getCounterpart();
            final BeanType type = getBeanContext().getComponentType();

            final EjbObjectProxyHandler handler = newEjbObjectHandler(getBeanContext(), primaryKey, objectInterfaceType, getInterfaces(), mainInterface);

            // TODO Is it correct for ManagedBean injection via managed bean class?
            if ((InterfaceType.LOCALBEAN.equals(objectInterfaceType) || getBeanContext().getComponentType().equals(BeanType.MANAGED))
                && !getBeanContext().isDynamicallyImplemented()) {
                return LocalBeanProxyFactory.constructProxy(handler.getBeanContext().get(BeanContext.ProxyClass.class).getProxy(), handler);
            } else {
                final List<Class> proxyInterfaces = new ArrayList<>(handler.getInterfaces().size() + 2);
                proxyInterfaces.addAll(handler.getInterfaces());
                proxyInterfaces.add(Serializable.class);
                proxyInterfaces.add(IntraVmProxy.class);
                if (BeanType.STATEFUL.equals(type) || BeanType.MANAGED.equals(type)) {
                    proxyInterfaces.add(BeanContext.Removable.class);
                }
                return ProxyManager.newProxyInstance(proxyInterfaces.toArray(new Class[proxyInterfaces.size()]), handler);
            }

        } catch (final IllegalAccessException iae) {
            throw new OpenEJBRuntimeException("Could not create IVM proxy for " + getInterfaces().get(0), iae);
        }
    }

    protected abstract EjbObjectProxyHandler newEjbObjectHandler(BeanContext beanContext, Object pk, InterfaceType interfaceType, List<Class> interfaces, Class mainInterface);

    @Override
    protected Object _invoke(final Object proxy, final Class interfce, final Method method, final Object[] args) throws Throwable {

        final String methodName = method.getName();

        if (logger.isDebugEnabled()) {
            logger.debug("EjbHomeProxyHandler: invoking method " + methodName + " on " + deploymentID);
        }

        try {
            final Object retValue;
            final MethodType operation = dispatchTable.get(methodName);

            if (operation == null) {
                retValue = homeMethod(interfce, method, args, proxy);
            } else {
                switch (operation) {
                    /*-- CREATE ------------- <HomeInterface>.create(<x>) ---*/
                    case CREATE:
                        retValue = create(interfce, method, args, proxy);
                        break;
                    case FIND:
                        retValue = findX(interfce, method, args, proxy);
                        break;
                        /*-- GET EJB METADATA ------ EJBHome.getEJBMetaData() ---*/
                    case META_DATA:
                        retValue = getEJBMetaData(method, args, proxy);
                        break;
                        /*-- GET HOME HANDLE -------- EJBHome.getHomeHandle() ---*/
                    case HOME_HANDLE:
                        retValue = getHomeHandle(method, args, proxy);
                        break;
                        /*-- REMOVE ------------------------ EJBHome.remove() ---*/
                    case REMOVE: {
                        final Class type = method.getParameterTypes()[0];

                        /*-- HANDLE ------- EJBHome.remove(Handle handle) ---*/
                        if (Handle.class.isAssignableFrom(type)) {
                            retValue = removeWithHandle(interfce, method, args, proxy);
                        } else {
                            /*-- PRIMARY KEY ----- EJBHome.remove(Object key) ---*/
                            retValue = removeByPrimaryKey(interfce, method, args, proxy);
                        }
                        break;
                    }
                    default:
                        throw new OpenEJBRuntimeException("Inconsistent internal state: value " + operation + " for operation " + methodName);
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("EjbHomeProxyHandler: finished invoking method " + method.getName() + ". Return value:" + retValue);
            }

            return retValue;

            /*
            * The ire is thrown by the container system and propagated by
            * the server to the stub.
            */
        } catch (final RemoteException re) {
            if (interfaceType.isLocal()) {
                throw new EJBException(re.getMessage()).initCause(re.detail);
            } else {
                throw re;
            }

        } catch (final InvalidateReferenceException ire) {
            Throwable cause = ire.getRootCause();
            if (cause instanceof RemoteException && interfaceType.isLocal()) {
                final RemoteException re = (RemoteException) cause;
                final Throwable detail = re.detail != null ? re.detail : re;
                cause = new EJBException(re.getMessage()).initCause(detail);
            }
            throw cause;
            /*
            * Application exceptions must be reported dirctly to the client. They
            * do not impact the viability of the proxy.
            */
        } catch (final ApplicationException ae) {
            final Throwable exc = ae.getRootCause() != null ? ae.getRootCause() : ae;
            if (exc instanceof EJBAccessException) {
                if (interfaceType.isBusiness()) {
                    throw exc;
                } else {
                    if (interfaceType.isLocal()) {
                        throw (AccessLocalException) new AccessLocalException(exc.getMessage()).initCause(exc);
                    } else {
                        try {
                            throw new AccessException(exc.getMessage()).initCause(exc);
                        } catch (final IllegalStateException vmbug) {
                            // Sun JDK 1.5 bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4871783
                            // bug affects using initCause on any RemoteException subclasses in Sun 1.5_07 or lower
                            throw new AccessException(exc.getMessage(), (Exception) exc);
                        }
                    }
                }

            }
            throw exc;
            /*
            * A system exception would be highly unusual and would indicate a sever
            * problem with the container system.
            */
        } catch (final SystemException se) {
            if (interfaceType.isLocal()) {
                throw new EJBException("Container has suffered a SystemException").initCause(se.getRootCause());
            } else {
                throw new RemoteException("Container has suffered a SystemException", se.getRootCause());
            }
        } catch (final OpenEJBException oe) {
            if (interfaceType.isLocal()) {
                throw new EJBException("Unknown Container Exception").initCause(oe.getRootCause());
            } else {
                throw new RemoteException("Unknown Container Exception", oe.getRootCause());
            }
        } catch (final Throwable t) {
            logger.debug("EjbHomeProxyHandler: finished invoking method " + method.getName() + " with exception:" + t, t);
            throw t;
        }
    }

    /*-------------------------------------------------*/
    /*  Home interface methods                         */
    /*-------------------------------------------------*/

    protected Object homeMethod(final Class interfce, final Method method, final Object[] args, final Object proxy) throws Throwable {
        checkAuthorization(method);

        final BeanContext beanContext = getBeanContext();

        if (beanContext.isAsynchronous(method)) {
            return beanContext.getModuleContext()
                .getAppContext()
                .getAsynchronousPool()
                .invoke(new CUCallable<Object>(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        try {
                            return homeMethodInvoke(interfce, method, args);
                        } catch (final ApplicationException ae) {
                            logger.error("EjbHomeProxyHandler: Asynchronous call to '" + interfce.getSimpleName() + "' on '" + method.getName() + "' failed", ae);
                            throw ae;
                        }
                    }
                }), method.getReturnType() == Void.TYPE);
        } else {
            return homeMethodInvoke(interfce, method, args);
        }
    }

    private Object homeMethodInvoke(final Class interfce, final Method method, final Object[] args) throws OpenEJBException {
        return container.invoke(deploymentID, interfaceType, interfce, method, args, null);
    }

    protected Object create(final Class interfce, final Method method, final Object[] args, final Object proxy) throws Throwable {
        if (container.getBeanContext(deploymentID) == null) {
            final BeanContext bc = getBeanContext();
            synchronized (bc.getId()) {
                if (container.getBeanContext(deploymentID) == null) {
                    container.deploy(bc);
                    container.start(bc);
                }
            }
        }

        final ProxyInfo proxyInfo = (ProxyInfo) container.invoke(deploymentID, interfaceType, interfce, method, args, null);
        assert proxyInfo != null : "Container returned a null ProxyInfo: ContainerID=" + container.getContainerID();
        return createProxy(proxyInfo.getPrimaryKey(), getMainInterface());
    }

    protected abstract Object findX(Class interfce, Method method, Object[] args, Object proxy) throws Throwable;

    /*-------------------------------------------------*/
    /*  EJBHome methods                                */
    /*-------------------------------------------------*/

    protected Object getEJBMetaData(final Method method, final Object[] args, final Object proxy) throws Throwable {
        checkAuthorization(method);
        final IntraVmMetaData metaData = new IntraVmMetaData(getBeanContext().getHomeInterface(),
            getBeanContext().getRemoteInterface(),
            getBeanContext().getPrimaryKeyClass(),
            getBeanContext().getComponentType());
        metaData.setEJBHome((EJBHome) proxy);
        return metaData;
    }

    protected Object getHomeHandle(final Method method, final Object[] args, final Object proxy) throws Throwable {
        checkAuthorization(method);
        return new IntraVmHandle(proxy);
    }

    @Override
    public ProxyInfo getProxyInfo() {
        if (getMainInterface() == null) {
            throw new IllegalStateException("no main interface");
        }
        return new ProxyInfo(getBeanContext(), null, getBeanContext().getInterfaces(interfaceType), interfaceType, getMainInterface());
    }

    @Override
    protected Object _writeReplace(final Object proxy) throws ObjectStreamException {
        /*
         * If the proxy is being copied between bean instances in a RPC
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
            * If the proxy is being copied between class loaders
            * we allow this object to be serialized.
            */
        } else if (IntraVmCopyMonitor.isCrossClassLoaderOperation()) {
            return proxy;
            /*
            * If the proxy is serialized outside the core container system,
            * we allow the application server to handle it.
            */
        } else if (!interfaceType.isRemote()) {
            return proxy;

        } else {
            final ApplicationServer applicationServer = ServerFederation.getApplicationServer();
            return applicationServer.getEJBHome(this.getProxyInfo());
        }
    }

    protected Object removeWithHandle(final Class interfce, final Method method, final Object[] args, final Object proxy) throws Throwable {

        final IntraVmHandle handle = (IntraVmHandle) args[0];
        final Object primKey = handle.getPrimaryKey();
        EjbObjectProxyHandler stub;
        try {
            stub = (EjbObjectProxyHandler) ProxyManager.getInvocationHandler(handle.getEJBObject());
        } catch (final IllegalArgumentException e) {

            stub = null;
        }

        container.invoke(deploymentID, interfaceType, interfce, method, args, primKey);

        /*
         * This operation takes care of invalidating all the EjbObjectProxyHanders associated with
         * the same RegistryId. See this.createProxy().
         */
        if (stub != null) {
            invalidateAllHandlers(stub.getRegistryId());
        }
        return null;
    }

    protected abstract Object removeByPrimaryKey(Class interfce, Method method, Object[] args, Object proxy) throws Throwable;
}
