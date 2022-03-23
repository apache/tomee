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
import org.apache.openejb.InterfaceType;
import org.apache.openejb.InvalidateReferenceException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.spi.ApplicationServer;
import org.apache.openejb.threads.task.CUCallable;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.ejb.AccessLocalException;
import jakarta.ejb.EJBAccessException;
import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.EJBObject;
import java.io.ObjectStreamException;
import java.lang.reflect.Method;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public abstract class EjbObjectProxyHandler extends BaseEjbProxyHandler {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");
    static final Map<String, Integer> dispatchTable;

    static {
        dispatchTable = new HashMap<>();
        dispatchTable.put("getHandle", 1);
        dispatchTable.put("getPrimaryKey", 2);
        dispatchTable.put("isIdentical", 3);
        dispatchTable.put("remove", 4);
        dispatchTable.put("getEJBHome", 5);
        dispatchTable.put("getEJBLocalHome", 6);
    }

    public EjbObjectProxyHandler(final BeanContext beanContext, final Object pk, final InterfaceType interfaceType, final List<Class> interfaces, final Class mainInterface) {
        super(beanContext, pk, interfaceType, interfaces, mainInterface);
    }

    public abstract Object getRegistryId();

    @Override
    public Object _invoke(final Object p, final Class interfce, final Method m, final Object[] a) throws Throwable {
        Object retValue = null;
        Throwable exc = null;

        final String methodName = m.getName();
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("EjbObjectProxyHandler: invoking method " + methodName + " on " + deploymentID + " with identity " + primaryKey);
            }
            Integer operation = dispatchTable.get(methodName);
            if (operation != null) {
                if (operation == 3) {
                    if (m.getParameterTypes()[0] != EJBObject.class && m.getParameterTypes()[0] != EJBLocalObject.class) {
                        operation = null;
                    }
                } else {
                    operation = m.getParameterTypes().length == 0 ? operation : null;
                }
            }
            if (operation == null || !interfaceType.isComponent()) {
                retValue = businessMethod(interfce, m, a, p);
            } else {
                switch (operation) {
                    case 1:
                        retValue = getHandle(m, a, p);
                        break;
                    case 2:
                        retValue = getPrimaryKey(m, a, p);
                        break;
                    case 3:
                        retValue = isIdentical(m, a, p);
                        break;
                    case 4:
                        retValue = remove(interfce, m, a, p);
                        break;
                    case 5:
                        retValue = getEJBHome(m, a, p);
                        break;
                    case 6:
                        retValue = getEJBLocalHome(m, a, p);
                        break;
                    default:
                        throw new OpenEJBRuntimeException("Inconsistent internal state");
                }
            }

            return retValue;

            /*
            * The ire is thrown by the container system and propagated by
            * the server to the stub.
            */
        } catch (final InvalidateReferenceException ire) {
            invalidateAllHandlers(getRegistryId());
            exc = ire.getRootCause() != null ? ire.getRootCause() : new RemoteException("InvalidateReferenceException: " + ire);
            throw exc;
            /*
            * Application exceptions must be reported dirctly to the client. They
            * do not impact the viability of the proxy.
            */
        } catch (final ApplicationException ae) {
            exc = ae.getRootCause() != null ? ae.getRootCause() : ae;
            if (exc instanceof EJBAccessException) {
                if (interfaceType.isBusiness()) {
                    throw exc;
                } else {
                    if (interfaceType.isLocal()) {
                        throw new AccessLocalException(exc.getMessage()).initCause(exc.getCause());
                    } else {
                        throw new AccessException(exc.getMessage());
                    }
                }

            }
            throw exc;

            /*
            * A system exception would be highly unusual and would indicate a sever
            * problem with the container system.
            */
        } catch (final SystemException se) {
            invalidateReference();
            exc = se.getRootCause() != null ? se.getRootCause() : se;
            logger.debug("The container received an unexpected exception: ", exc);
            throw new RemoteException("Container has suffered a SystemException", exc);
        } catch (final OpenEJBException oe) {
            exc = oe.getRootCause() != null ? oe.getRootCause() : oe;
            logger.debug("The container received an unexpected exception: ", exc);
            throw new RemoteException("Unknown Container Exception", oe.getRootCause());
        } finally {
            if (logger.isDebugEnabled()) {
                if (exc == null) {
                    String ret = "void";
                    if (null != retValue) {
                        try {
                            ret = retValue.toString();
                        } catch (final Exception e) {
                            ret = "toString() failed on (" + e.getMessage() + ")";
                        }
                    }
                    logger.debug("EjbObjectProxyHandler: finished invoking method " + methodName + ". Return value:" + ret);
                } else {
                    logger.debug("EjbObjectProxyHandler: finished invoking method " + methodName + " with exception " + exc);
                }
            }
        }
    }

    protected Object getEJBHome(final Method method, final Object[] args, final Object proxy) throws Throwable {
        checkAuthorization(method);
        return getBeanContext().getEJBHome();
    }

    protected Object getEJBLocalHome(final Method method, final Object[] args, final Object proxy) throws Throwable {
        checkAuthorization(method);
        return getBeanContext().getEJBLocalHome();
    }

    protected Object getHandle(final Method method, final Object[] args, final Object proxy) throws Throwable {
        checkAuthorization(method);
        return new IntraVmHandle(proxy);
    }

    @Override
    public ProxyInfo getProxyInfo() {
        return new ProxyInfo(getBeanContext(), primaryKey, getInterfaces(), interfaceType, getMainInterface());
    }

    @Override
    protected Object _writeReplace(final Object proxy) throws ObjectStreamException {
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
            if (interfaceType.isBusiness()) {
                return applicationServer.getBusinessObject(this.getProxyInfo());
            } else {
                return applicationServer.getEJBObject(this.getProxyInfo());
            }
        }
    }

    protected abstract Object getPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable;

    protected abstract Object isIdentical(Method method, Object[] args, Object proxy) throws Throwable;

    protected abstract Object remove(Class interfce, Method method, Object[] args, Object proxy) throws Throwable;

    protected Object businessMethod(final Class<?> interfce, final Method method, final Object[] args, final Object proxy) throws Throwable {
        final BeanContext beanContext = getBeanContext();

        if (beanContext.isAsynchronous(method)) {
            return beanContext.getModuleContext()
                .getAppContext()
                .getAsynchronousPool()
                .invoke(new CUCallable<Object>(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        try {
                            return synchronizedBusinessMethod(interfce, method, args);
                        } catch (final ApplicationException ae) {
                            logger.error("EjbObjectProxyHandler: Asynchronous call to '" + interfce.getSimpleName() + "' on '" + method.getName() + "' failed", ae);
                            throw ae;
                        }
                    }
                }), method.getReturnType() == Void.TYPE);
        } else {
            return synchronizedBusinessMethod(interfce, method, args);
        }
    }

    protected Object synchronizedBusinessMethod(final Class<?> interfce, final Method method, final Object[] args) throws OpenEJBException {
        return container.invoke(deploymentID, interfaceType, interfce, method, args, primaryKey);
    }

    public static Object createProxy(final BeanContext beanContext, final Object primaryKey, final InterfaceType interfaceType, final Class mainInterface) {
        return createProxy(beanContext, primaryKey, interfaceType, null, mainInterface);
    }

    public static Object createProxy(final BeanContext beanContext, final Object primaryKey, InterfaceType interfaceType, final List<Class> interfaces, final Class mainInterface) {
        if (!interfaceType.isHome()) {
            interfaceType = interfaceType.getCounterpart();
        }
        final EjbHomeProxyHandler homeHandler = EjbHomeProxyHandler.createHomeHandler(beanContext, interfaceType, interfaces, mainInterface);
        return homeHandler.createProxy(primaryKey, mainInterface);
    }

}
