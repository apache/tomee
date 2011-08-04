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

import java.io.ObjectStreamException;
import java.lang.reflect.Method;
import java.rmi.AccessException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ejb.AccessLocalException;
import javax.ejb.ConcurrentAccessTimeoutException;
import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.NoSuchEJBException;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.spi.ApplicationServer;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

public abstract class EjbObjectProxyHandler extends BaseEjbProxyHandler {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");
    static final Map<String, Integer> dispatchTable;

    static {
        dispatchTable = new HashMap<String, Integer>();
        dispatchTable.put("getHandle", Integer.valueOf(1));
        dispatchTable.put("getPrimaryKey", Integer.valueOf(2));
        dispatchTable.put("isIdentical", Integer.valueOf(3));
        dispatchTable.put("remove", Integer.valueOf(4));
        dispatchTable.put("getEJBHome", Integer.valueOf(5));
        dispatchTable.put("getEJBLocalHome", Integer.valueOf(6));
    }

    public EjbObjectProxyHandler(BeanContext beanContext, Object pk, InterfaceType interfaceType, List<Class> interfaces, Class mainInterface) {
        super(beanContext, pk, interfaceType, interfaces, mainInterface);
    }

    public abstract Object getRegistryId();

    public Object _invoke(Object p, Class interfce, Method m, Object[] a) throws Throwable {
        java.lang.Object retValue = null;
        java.lang.Throwable exc = null;

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("invoking method " + m.getName() + " on " + deploymentID + " with identity " + primaryKey);
            }
            Integer operation = dispatchTable.get(m.getName());
            if(operation != null){
                if(operation.intValue() == 3){
                    if(m.getParameterTypes()[0] != EJBObject.class && m.getParameterTypes()[0] != EJBLocalObject.class ){
                        operation = null;
                    }
                } else {
                    operation = (m.getParameterTypes().length == 0)?operation:null;
                }
            }
            if (operation == null || !interfaceType.isComponent() ) {
                retValue = businessMethod(interfce, m, a, p);
            } else {
                switch (operation.intValue()) {
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
                        throw new RuntimeException("Inconsistent internal state");
                }
            }

            return retValue;

            /*
            * The ire is thrown by the container system and propagated by
            * the server to the stub.
            */
        } catch (org.apache.openejb.InvalidateReferenceException ire) {
            invalidateAllHandlers(getRegistryId());
            exc = (ire.getRootCause() != null) ? ire.getRootCause() : ire;
            throw exc;
            /*
            * Application exceptions must be reported dirctly to the client. They
            * do not impact the viability of the proxy.
            */
        } catch (org.apache.openejb.ApplicationException ae) {
            exc = (ae.getRootCause() != null) ? ae.getRootCause() : ae;
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
        } catch (org.apache.openejb.SystemException se) {
            invalidateReference();
            exc = (se.getRootCause() != null) ? se.getRootCause() : se;
            logger.debug("The container received an unexpected exception: ", exc);
            throw new RemoteException("Container has suffered a SystemException", exc);
        } catch (org.apache.openejb.OpenEJBException oe) {
            exc = (oe.getRootCause() != null) ? oe.getRootCause() : oe;
            logger.debug("The container received an unexpected exception: ", exc);
            throw new RemoteException("Unknown Container Exception", oe.getRootCause());
        } finally {
            if (logger.isDebugEnabled()) {
                if (exc == null) {
                    logger.debug("finished invoking method " + m.getName() + ". Return value:" + retValue);
                } else {
                    logger.debug("finished invoking method " + m.getName() + " with exception " + exc);
                }
            }
        }
    }

    protected Object getEJBHome(Method method, Object[] args, Object proxy) throws Throwable {
        checkAuthorization(method);
        return getBeanContext().getEJBHome();
    }

    protected Object getEJBLocalHome(Method method, Object[] args, Object proxy) throws Throwable {
        checkAuthorization(method);
        return getBeanContext().getEJBLocalHome();
    }

    protected Object getHandle(Method method, Object[] args, Object proxy) throws Throwable {
        checkAuthorization(method);
        return new IntraVmHandle(proxy);
    }

    public org.apache.openejb.ProxyInfo getProxyInfo() {
        return new org.apache.openejb.ProxyInfo(getBeanContext(), primaryKey, getInterfaces(), interfaceType, getMainInterface());
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
            * If the proxy is being copied between class loaders
            * we allow this object to be serialized.
            */
        } else if (IntraVmCopyMonitor.isCrossClassLoaderOperation()) {
            return proxy;
            /*
            * If the proxy is serialized outside the core container system,
            * we allow the application server to handle it.
            */
        } else {
            ApplicationServer applicationServer = ServerFederation.getApplicationServer();
            if (interfaceType.isBusiness()){
                return applicationServer.getBusinessObject(this.getProxyInfo());
            } else {
                return applicationServer.getEJBObject(this.getProxyInfo());
            }
        }
    }

    protected abstract Object getPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable;

    protected abstract Object isIdentical(Method method, Object[] args, Object proxy) throws Throwable;

    protected abstract Object remove(Class interfce, Method method, Object[] args, Object proxy) throws Throwable;

    protected Object businessMethod(Class<?> interfce, Method method, Object[] args, Object proxy) throws Throwable {
        BeanContext beanContext = getBeanContext();
        if (beanContext.isAsynchronous(method)) {
            return asynchronizedBusinessMethod(interfce, method, args, proxy);
        } else {
            return synchronizedBusinessMethod(interfce, method, args, proxy);
        }
    }

    protected Object asynchronizedBusinessMethod(Class<?> interfce, Method method, Object[] args, Object proxy) throws Throwable {
        BeanContext beanContext = getBeanContext();
        AtomicBoolean asynchronousCancelled = new AtomicBoolean(false);
        AsynchronousCall asynchronousCall = new AsynchronousCall(interfce, method, args, asynchronousCancelled);
        try {
            Future<Object> retValue = beanContext.getModuleContext().getAppContext().submitTask(asynchronousCall);
            if (method.getReturnType() == Void.TYPE) {
                return null;
            }
            return new FutureAdapter<Object>(retValue, asynchronousCancelled, beanContext.getModuleContext().getAppContext());
        } catch (RejectedExecutionException e) {
            throw new EJBException("fail to allocate internal resource to execute the target task", e);
        }
    }

    protected Object synchronizedBusinessMethod(Class<?> interfce, Method method, Object[] args, Object proxy) throws Throwable {
        return container.invoke(deploymentID, interfaceType, interfce, method, args, primaryKey);
    }

    public static Object createProxy(BeanContext beanContext, Object primaryKey, InterfaceType interfaceType, Class mainInterface) {
        return createProxy(beanContext, primaryKey, interfaceType, null, mainInterface);
    }

    public static Object createProxy(BeanContext beanContext, Object primaryKey, InterfaceType interfaceType, List<Class> interfaces, Class mainInterface) {
        if (!interfaceType.isHome()){
            interfaceType = interfaceType.getCounterpart();
        }
        EjbHomeProxyHandler homeHandler = EjbHomeProxyHandler.createHomeHandler(beanContext, interfaceType, interfaces, mainInterface);
        return homeHandler.createProxy(primaryKey, mainInterface);
    }

    private class AsynchronousCall implements Callable<Object> {

        private Class<?> interfce;

        private Method method;

        private Object[] args;

        private AtomicBoolean asynchronousCancelled;

        public AsynchronousCall(Class<?> interfce, Method method, Object[] args, AtomicBoolean asynchronousCancelled) {
            this.interfce = interfce;
            this.method = method;
            this.args = args;
            this.asynchronousCancelled = asynchronousCancelled;
        }

        @Override
        public Object call() throws Exception {
            try {
                ThreadContext.initAsynchronousCancelled(asynchronousCancelled);
                Object retValue = container.invoke(deploymentID, interfaceType, interfce, method, args, primaryKey);
                if (retValue == null) {
                    return null;
                } else if (retValue instanceof Future<?>) {
                    //TODO do we need to strictly check AsyncResult  or just Future ?
                    Future<?> asyncResult = (Future<?>) retValue;
                    return asyncResult.get();
                } else {
                    // The bean isn't returning the right result!
                    // We should never arrive here !
                    return null;
                }
            } finally {
                ThreadContext.removeAsynchronousCancelled();
            }
        }
    }

    private class FutureAdapter<T> implements Future<T> {

        private Future<T> target;

        private AtomicBoolean asynchronousCancelled;

        private AppContext appContext;

        private volatile boolean canceled;

        public FutureAdapter(Future<T> target, AtomicBoolean asynchronousCancelled, AppContext appContext) {
            this.target = target;
            this.asynchronousCancelled = asynchronousCancelled;
            this.appContext = appContext;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            /*In EJB 3.1 spec 3.4.8.1.1
             *a. If a client calls cancel on its Future object, the container will attempt to cancel the associated asynchronous invocation only if that invocation has not already been dispatched.
             *  There is no guarantee that an asynchronous invocation can be cancelled, regardless of how quickly cancel is called after the client receives its Future object.
             *  If the asynchronous invocation can not be cancelled, the method must return false.
             *  If the asynchronous invocation is successfully cancelled, the method must return true.
             *b. the meaning of parameter mayInterruptIfRunning is changed.
             *  So, we should never call cancel(true), or the underlying Future object will try to interrupt the target thread.
            */
            /**
             * We use our own flag canceled to identify whether the task is canceled successfully.
             */
            if(canceled) {
                return true;
            }
            if (appContext.removeTask((Runnable) target)) {
                //We successfully remove the task from the queue
                canceled = true;
                return true;
            } else {
                //Not find the task in the queue, the status might be ran/canceled or running
                //Future.isDone() will return true when the task has been ran or canceled,
                //since we never call the Future.cancel method, the isDone method will only return true when the task has ran
                if (!target.isDone()) {
                    //The task is in the running state
                    asynchronousCancelled.set(mayInterruptIfRunning);
                }
                return false;
            }
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            if(canceled) {
                throw new CancellationException();
            }
            
            T object = null;

            try {
                object = target.get();
            } catch (Throwable e) {
                handleException(e);
            }

            return object;
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            if (canceled) {
                throw new CancellationException();
            }
            
            T object = null;

            try {
                object = target.get(timeout, unit);
            } catch (Throwable e) {
                handleException(e);
            }

            return object;
            
        }
        
        private void handleException(Throwable e) throws ExecutionException {
            
            //unwarp the exception to find the root cause
            while (e.getCause() != null) {
                e = (Throwable) e.getCause();
            }
            
            /* 
             * StatefulContainer.obtainInstance(Object, ThreadContext, Method)
             * will return NoSuchObjectException instead of NoSuchEJBException             * 
             * when it can't obtain an instance.   Actually, the async client 
             * is expecting a NoSuchEJBException.  Wrap it here as a workaround.
             */
            if (e instanceof NoSuchObjectException) {
                e = new NoSuchEJBException(e.getMessage(), (Exception) e);
            }

            boolean isExceptionUnchecked = (e instanceof Error) || (e instanceof RuntimeException);

            // throw checked excpetion and EJBException directly.
            if (!isExceptionUnchecked || e instanceof EJBException) {
                throw new ExecutionException(e);
            }

            // wrap unchecked exception with EJBException before throwing.
            throw (e instanceof Exception) ? new ExecutionException(new EJBException((Exception) e))
                    : new ExecutionException(new EJBException(new Exception(e)));
            
        }

        @Override
        public boolean isCancelled() {
            return canceled;
        }

        @Override
        public boolean isDone() {
            if(canceled) {
                return false;
            }
            return target.isDone();
        }
    }
}
