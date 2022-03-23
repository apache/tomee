/**
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
package org.apache.openejb.client;

import org.apache.openejb.client.proxy.ProxyManager;
import org.apache.openejb.client.serializer.SerializationWrapper;
import org.apache.openejb.client.util.ClassLoaderUtil;

import jakarta.ejb.EJBException;
import jakarta.ejb.EJBObject;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("NullArgumentToVariableArgMethod")
public abstract class EJBObjectHandler extends EJBInvocationHandler {
    protected static final Method GETEJBHOME = getMethod(EJBObject.class, "getEJBHome", null);
    protected static final Method GETHANDLE = getMethod(EJBObject.class, "getHandle", null);
    protected static final Method GETPRIMARYKEY = getMethod(EJBObject.class, "getPrimaryKey", null);
    protected static final Method ISIDENTICAL = getMethod(EJBObject.class, "isIdentical", EJBObject.class);
    protected static final Method REMOVE = getMethod(EJBObject.class, "remove", null);
    protected static final Method GETHANDLER = getMethod(EJBObjectProxy.class, "getEJBObjectHandler", null);
    protected static final Method CANCEL = getMethod(Future.class, "cancel", boolean.class);

    /*
    * The registryId is a logical identifier that is used as a key when placing EntityEJBObjectHandler into
    * the BaseEjbProxyHanlder's liveHandleRegistry.  EntityEJBObjectHandlers that represent the same
    * bean identity (keyed by the registry id) will be stored together so that they can be removed together
    * when the EJBInvocationHandler.invalidateAllHandlers is invoked. The EntityEJBObjectHandler uses a
    * compound key composed of the entity bean's primary key, deployment id, and
    * container id.  This uniquely identifies the bean identity that is proxied by this handler allowing it
    * to be removed with other handlers bound to the same registry id.
    */
    public Object registryId;

    EJBHomeProxy ejbHome = null;

    protected ThreadPoolExecutor executor = null;

    public EJBObjectHandler() {
    }

    public EJBObjectHandler(final ThreadPoolExecutor es, final EJBMetaDataImpl ejb, final ServerMetaData server, final ClientMetaData client, final JNDIContext.AuthenticationInfo auth) {
        super(ejb, server, client, null, auth);
        this.executor = es;
    }

    public EJBObjectHandler(final ThreadPoolExecutor es,
                            final EJBMetaDataImpl ejb,
                            final ServerMetaData server,
                            final ClientMetaData client,
                            final Object primaryKey,
                            final JNDIContext.AuthenticationInfo auth) {
        super(ejb, server, client, primaryKey, auth);
        this.executor = es;
    }

    protected void setEJBHomeProxy(final EJBHomeProxy ejbHome) {
        this.ejbHome = ejbHome;
    }

    public static EJBObjectHandler createEJBObjectHandler(final ThreadPoolExecutor executorService,
                                                          final EJBMetaDataImpl ejb,
                                                          final ServerMetaData server,
                                                          final ClientMetaData client,
                                                          final Object primaryKey,
                                                          final JNDIContext.AuthenticationInfo auth) {

        switch (ejb.type) {
            case EJBMetaDataImpl.BMP_ENTITY:
            case EJBMetaDataImpl.CMP_ENTITY:

                return new EntityEJBObjectHandler(executorService, ejb, server, client, primaryKey, auth);

            case EJBMetaDataImpl.STATEFUL:

                return new StatefulEJBObjectHandler(executorService, ejb, server, client, primaryKey, auth);

            case EJBMetaDataImpl.STATELESS:

                return new StatelessEJBObjectHandler(executorService, ejb, server, client, primaryKey, auth);

            case EJBMetaDataImpl.SINGLETON:

                return new SingletonEJBObjectHandler(executorService, ejb, server, client, primaryKey, auth);
        }

        throw new IllegalStateException("Uknown bean type code '" + ejb.type + "' : " + ejb.toString());
    }

    public abstract Object getRegistryId();

    public EJBObjectProxy createEJBObjectProxy() {

        EJBObjectProxy ejbObject = null;

        try {
            final List<Class> interfaces = new ArrayList<Class>();
            // Interface class must be listed first, before EJBObjectProxy,
            // otherwise the proxy code will select the openejb system class
            // loader for proxy creation instead of the application class loader
            if (ejb.remoteClass != null) {
                interfaces.add(ejb.remoteClass);
            } else if (ejb.businessClasses.size() > 0) {
                interfaces.addAll(ejb.businessClasses);
            }
            interfaces.add(EJBObjectProxy.class);

            final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            final boolean parent = ClassLoaderUtil.isParent(getClass().getClassLoader(), oldCl);
            if (!parent) {
                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            }
            try {
                ejbObject = (EJBObjectProxy) ProxyManager.newProxyInstance(interfaces.toArray(new Class[interfaces.size()]), this);
            } finally {
                if (!parent) {
                    Thread.currentThread().setContextClassLoader(oldCl);
                }
            }
        } catch (IllegalAccessException e) {

            e.printStackTrace();
        }
        return ejbObject;
    }

    @Override
    public Object _invoke(final Object p, final Method m, final Object[] a) throws Throwable {

        try {

            if (m.getDeclaringClass().equals(Object.class)) {

                if (m.equals(TOSTRING)) {
                    return "proxy=" + this;
                } else if (m.equals(EQUALS)) {
                    return equals(m, a, p);
                } else if (m.equals(HASHCODE)) {
                    return this.hashCode();
                } else {
                    throw new UnsupportedOperationException("Unkown method: " + m);
                }

            } else if (m.getDeclaringClass() == EJBObjectProxy.class) {

                if (m.equals(GETHANDLER)) {
                    return this;
                } else if (m.getName().equals("writeReplace")) {
                    return new EJBObjectProxyHandle(this);
                } else if (m.getName().equals("readResolve")) {
                    return null;
                } else {
                    throw new UnsupportedOperationException("Unkown method: " + m);
                }

            } else if (m.getDeclaringClass() == jakarta.ejb.EJBObject.class) {

                if (m.equals(GETHANDLE)) {
                    return getHandle(m, a, p);
                } else if (m.equals(GETPRIMARYKEY)) {
                    return getPrimaryKey(m, a, p);
                } else if (m.equals(ISIDENTICAL)) {
                    return isIdentical(m, a, p);
                } else if (m.equals(GETEJBHOME)) {
                    return getEJBHome(m, a, p);
                } else if (m.equals(REMOVE)) {
                    return remove(m, a, p);
                } else {
                    throw new UnsupportedOperationException("Unkown method: " + m);
                }

            } else {

                return businessMethod(m, a, p);

            }

        } catch (SystemException e) {
            invalidateAllHandlers(getRegistryId());
            throw convertException(getCause(e), m);
            /*
            * Application exceptions must be reported dirctly to the client. They
            * do not impact the viability of the proxy.
            */
        } catch (ApplicationException ae) {
            throw convertException(getCause(ae), m);
            /*
            * A system exception would be highly unusual and would indicate a sever
            * problem with the container system.
            */
        } catch (SystemError se) {
            invalidateReference();
            if (remote) {
                throw new RemoteException("Container has suffered a SystemException", getCause(se));
            } else {
                throw new EJBException("Container has suffered a SystemException").initCause(getCause(se));
            }
        } catch (Throwable throwable) {
            if (remote) {
                if (throwable instanceof RemoteException) {
                    throw throwable;
                }
                throw new RemoteException("Unknown Container Exception: " + throwable.getClass().getName() + ": " + throwable.getMessage(), getCause(throwable));
            } else {
                if (throwable instanceof EJBException) {
                    throw throwable;
                }
                throw new EJBException("Unknown Container Exception: " + throwable.getClass().getName() + ": " + throwable.getMessage()).initCause(getCause(throwable));
            }
        }
    }

    protected Object getEJBHome(final Method method, final Object[] args, final Object proxy) throws Throwable {
        if (ejbHome == null) {
            ejbHome = EJBHomeHandler.createEJBHomeHandler(executor, ejb, server, client, authenticationInfo).createEJBHomeProxy();
        }
        return ejbHome;
    }

    protected Object getHandle(final Method method, final Object[] args, final Object proxy) throws Throwable {
        return new EJBObjectHandle((EJBObjectProxy) proxy);
    }

    protected abstract Object getPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable;

    protected abstract Object isIdentical(Method method, Object[] args, Object proxy) throws Throwable;

    protected abstract Object equals(Method method, Object[] args, Object proxy) throws Throwable;

    protected abstract Object remove(Method method, Object[] args, Object proxy) throws Throwable;

    @SuppressWarnings("unchecked")
    protected Object businessMethod(final Method method, final Object[] args, final Object proxy) throws Throwable {

        if (ejb.isAsynchronousMethod(method)) {
            try {
                final String requestId = UUID.randomUUID().toString();
                final EJBResponse response = new EJBResponse();
                final AsynchronousCall asynchronousCall = new AsynchronousCall(method, args, proxy, requestId, response);
                if (executor == null) {
                    executor = JNDIContext.newExecutor(-1, null);
                }
                return new FutureAdapter(executor.submit(asynchronousCall), response, requestId);
            } catch (RejectedExecutionException e) {
                throw new EJBException("failed to allocate internal resource to execute the target task", e);
            }
        } else {
            return _businessMethod(method, args, proxy, null);
        }
    }

    private Object _businessMethod(final Method method, final Object[] args, final Object proxy, final String requestId) throws Throwable {
        final EJBRequest req = new EJBRequest(RequestMethodCode.EJB_OBJECT_BUSINESS_METHOD, ejb, method, args, primaryKey, client.getSerializer());

        //Currently, we only set the requestId while the asynchronous invocation is called
        final EJBRequest.Body body = req.getBody();
        body.setRequestId(requestId);
        body.setAuthentication(this.authenticationInfo);
        final EJBResponse res = request(req);
        return _handleBusinessMethodResponse(res);
    }

    private Object _businessMethod(final Method method, final Object[] args, final Object proxy, final String requestId, final EJBResponse response) throws Throwable {
        final EJBRequest req = new EJBRequest(RequestMethodCode.EJB_OBJECT_BUSINESS_METHOD, ejb, method, args, primaryKey, client.getSerializer());

        //Currently, we only set the request while the asynchronous invocation is called
        req.getBody().setRequestId(requestId);
        final EJBResponse res = request(req, response);
        return _handleBusinessMethodResponse(res);
    }

    private Object _handleBusinessMethodResponse(final EJBResponse res) throws Throwable {
        switch (res.getResponseCode()) {
            case ResponseCodes.EJB_ERROR:
                throw new SystemError((ThrowableArtifact) getResult(res));
            case ResponseCodes.EJB_SYS_EXCEPTION:
                throw new SystemException((ThrowableArtifact) getResult(res));
            case ResponseCodes.EJB_APP_EXCEPTION:
                throw new ApplicationException((ThrowableArtifact) getResult(res));
            case ResponseCodes.EJB_OK:
                return getResult(res);
            case ResponseCodes.AUTH_DENIED:
                final Object result = res.getResult();
                if (ThrowableArtifact.class.isInstance(result)) {
                    throw ThrowableArtifact.class.cast(result).getThrowable();
                } // else it be a remote exception
            default:
                throw new RemoteException("Received invalid response code from server: " + res.getResponseCode());
        }
    }

    private Object getResult(final EJBResponse res) throws ClassNotFoundException {
        final Object result = res.getResult();
        if (client.getSerializer() != null && SerializationWrapper.class.isInstance(result)) {
            final SerializationWrapper wrapper = SerializationWrapper.class.cast(result);
            return client.getSerializer().deserialize(wrapper.getData(), Thread.currentThread().getContextClassLoader().loadClass(wrapper.getClassname()));
        }
        return res.getResult();
    }

    private class AsynchronousCall implements Callable {

        private Method method;

        private Object[] args;

        private Object proxy;

        private String requestId;

        private EJBResponse response;

        public AsynchronousCall(final Method method, final Object[] args, final Object proxy, final String requestId, final EJBResponse response) {
            this.method = method;
            this.args = args;
            this.proxy = proxy;
            this.requestId = requestId;
            this.response = response;
        }

        @Override
        public Object call() throws Exception {
            try {
                return _businessMethod(method, args, proxy, requestId, response);
            } catch (Exception e) {
                throw e;
            } catch (Throwable error) {
                throw new SystemException(error);
            }
        }
    }

    private class FutureAdapter<T> implements Future<T> {

        private Future<T> target;

        private String requestId;

        private EJBResponse response;

        private volatile boolean canceled;

        private AtomicBoolean lastMayInterruptIfRunningValue = new AtomicBoolean(false);

        public FutureAdapter(final Future<T> target, final EJBResponse response, final String requestId) {
            this.target = target;
            this.requestId = requestId;
            this.response = response;
        }

        @SuppressWarnings({"SuspiciousMethodCalls", "UnnecessaryBoxing"})
        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            /* In EJB 3.1 spec 3.4.8.1.1
             * a. If a client calls cancel on its Future object, the container will attempt to cancel 
             *    the associated asynchronous invocation only if that invocation has not already been dispatched.
             *    There is no guarantee that an asynchronous invocation can be cancelled, regardless of how quickly 
             *    cancel is called after the client receives its Future object.
             *    If the asynchronous invocation can not be cancelled, the method must return false.
             *    If the asynchronous invocation is successfully cancelled, the method must return true.
             *
             * b. The meaning of parameter mayInterruptIfRunning is changed.
             *
             *  So, we should never call cancel(true), or the underlying Future object will try to interrupt the target thread.
            */

            /**
             * We use our own flag canceled to identify whether the task is canceled successfully.
             */
            if (canceled) {
                return true;
            }
            if (executor.getQueue().remove(target)) {
                // We successfully remove the task from the queue
                canceled = true;
                return true;
            } else {
                // Did not find the task in the queue, the status might be ran/canceled or running
                // Future.isDone() will return true when the task has been ran or canceled,
                // Since we never call the Future.cancel method, the isDone method will only return true when the task has ran
                if (!target.isDone()) {
                    //The task is in the running state
                    if (lastMayInterruptIfRunningValue.getAndSet(mayInterruptIfRunning) == mayInterruptIfRunning) {
                        return false;
                    }
                    final EJBRequest req = new EJBRequest(RequestMethodCode.FUTURE_CANCEL,
                        ejb,
                        CANCEL,
                        new Object[]{Boolean.valueOf(mayInterruptIfRunning)},
                        primaryKey,
                        client.getSerializer());
                    req.getBody().setRequestId(requestId);
                    try {
                        final EJBResponse res = request(req);
                        if (res.getResponseCode() != ResponseCodes.EJB_OK) {
                            //TODO how do we notify the user that we fail to configure the value ?
                            Logger.getLogger(this.getClass().getName()).info("Unexpected response on cancel: " + res);
                        }
                    } catch (Exception e) {
                        //TODO how to handle
                        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Unexpected error on cancel", e);
                        return false;
                    }
                }
                return false;
            }
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            if (canceled) {
                throw new CancellationException();
            }
            return target.get();
        }

        @Override
        public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            if (canceled) {
                throw new CancellationException();
            }
            return target.get(timeout, unit);
        }

        @Override
        public boolean isCancelled() {
            return canceled;
        }

        @Override
        public boolean isDone() {
            if (canceled) {
                return false;
            }
            return target.isDone();
        }
    }
}