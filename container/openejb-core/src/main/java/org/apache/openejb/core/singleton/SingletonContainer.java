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

package org.apache.openejb.core.singleton;

import org.apache.openejb.BeanContext;
import org.apache.openejb.ContainerType;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.cdi.CurrentCreationalContext;
import org.apache.openejb.core.ExceptionType;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.interceptor.InterceptorStack;
import org.apache.openejb.core.security.AbstractSecurityService;
import org.apache.openejb.core.timer.EjbTimerService;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.webservices.AddressingSupport;
import org.apache.openejb.core.webservices.NoAddressingSupport;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Duration;
import org.apache.xbean.finder.ClassFinder;

import jakarta.ejb.ConcurrentAccessTimeoutException;
import jakarta.ejb.EJBAccessException;
import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBLocalHome;
import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.EJBObject;
import jakarta.interceptor.AroundInvoke;
import javax.security.auth.login.LoginException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;

import static org.apache.openejb.core.transaction.EjbTransactionUtil.afterInvoke;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.createTransactionPolicy;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.handleApplicationException;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.handleSystemException;

/**
 * @org.apache.xbean.XBean element="statelessContainer"
 */
public class SingletonContainer implements RpcContainer {

    private final SingletonInstanceManager instanceManager;

    private final HashMap<String, BeanContext> deploymentRegistry = new HashMap<>();

    private final ConcurrentMap<Class<?>, List<Method>> interceptorCache = new ConcurrentHashMap<>();

    private final Object containerID;
    private final SecurityService securityService;
    private Duration accessTimeout;

    public SingletonContainer(final Object id, final SecurityService securityService) throws OpenEJBException {
        this.containerID = id;
        this.securityService = securityService;

        instanceManager = new SingletonInstanceManager(securityService);

        for (final BeanContext beanContext : deploymentRegistry.values()) {
            beanContext.setContainer(this);
        }
    }

    public void setAccessTimeout(final Duration duration) {
        this.accessTimeout = duration;
    }

    @Override
    public synchronized BeanContext[] getBeanContexts() {
        return deploymentRegistry.values().toArray(new BeanContext[deploymentRegistry.size()]);
    }

    @Override
    public synchronized BeanContext getBeanContext(final Object deploymentID) {
        final String id = (String) deploymentID;
        return deploymentRegistry.get(id);
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.SINGLETON;
    }

    @Override
    public Object getContainerID() {
        return containerID;
    }

    @Override
    public void deploy(final BeanContext beanContext) throws OpenEJBException {
        instanceManager.deploy(beanContext);
        final String id = (String) beanContext.getDeploymentID();
        synchronized (this) {
            deploymentRegistry.put(id, beanContext);
            beanContext.setContainer(this);
        }
    }

    @Override
    public void start(final BeanContext info) throws OpenEJBException {
        instanceManager.start(info);

        final EjbTimerService timerService = info.getEjbTimerService();
        if (timerService != null) {
            timerService.start();
        }
    }

    @Override
    public void stop(final BeanContext info) throws OpenEJBException {
        info.stop();
    }

    @Override
    public void undeploy(final BeanContext beanContext) {
        final ThreadContext threadContext = new ThreadContext(beanContext, null);
        final ThreadContext old = ThreadContext.enter(threadContext);
        try {
            instanceManager.freeInstance(threadContext);
        } finally {
            ThreadContext.exit(old);
        }

        instanceManager.undeploy(beanContext);

        synchronized (this) {
            final String id = (String) beanContext.getDeploymentID();
            beanContext.setContainer(null);
            beanContext.setContainerData(null);
            deploymentRegistry.remove(id);
        }
    }

    @Override
    public Object invoke(final Object deployID,
                         InterfaceType type,
                         final Class callInterface,
                         final Method callMethod,
                         final Object[] args,
                         final Object primKey) throws OpenEJBException {
        final BeanContext beanContext = this.getBeanContext(deployID);

        if (beanContext == null) {
            throw new OpenEJBException("Deployment does not exist in this container. Deployment(id='" + deployID + "'), Container(id='" + containerID + "')");
        }

        // Use the backup way to determine call type if null was supplied.
        if (type == null) {
            type = beanContext.getInterfaceType(callInterface);
        }

        final Method runMethod = beanContext.getMatchingBeanMethod(callMethod);

        final ThreadContext callContext = new ThreadContext(beanContext, primKey);
        final ThreadContext oldCallContext = ThreadContext.enter(callContext);
        final CurrentCreationalContext currentCreationalContext = beanContext.get(CurrentCreationalContext.class);
        Object runAs = null;
        try {
            if (oldCallContext != null) {
                final BeanContext oldBc = oldCallContext.getBeanContext();
                if (oldBc.getRunAsUser() != null || oldBc.getRunAs() != null) {
                    runAs = AbstractSecurityService.class.cast(securityService).overrideWithRunAsContext(callContext, beanContext, oldBc);
                }
            }

            final boolean authorized = type == InterfaceType.TIMEOUT || getSecurityService().isCallerAuthorized(callMethod, type);

            if (!authorized) {
                throw new org.apache.openejb.ApplicationException(new EJBAccessException("Unauthorized Access by Principal Denied"));
            }

            final Class declaringClass = callMethod.getDeclaringClass();
            if (EJBHome.class.isAssignableFrom(declaringClass) || EJBLocalHome.class.isAssignableFrom(declaringClass)) {
                if (callMethod.getName().startsWith("create")) {
                    return createEJBObject(beanContext, callMethod);
                } else {
                    return null;// EJBHome.remove( ) and other EJBHome methods are not process by the container
                }
            } else if (EJBObject.class == declaringClass || EJBLocalObject.class == declaringClass) {
                return null;// EJBObject.remove( ) and other EJBObject methods are not process by the container
            }

            final Instance instance = instanceManager.getInstance(callContext);

            callContext.setCurrentOperation(type == InterfaceType.TIMEOUT ? Operation.TIMEOUT : Operation.BUSINESS);
            callContext.setCurrentAllowedStates(null);
            callContext.set(Method.class, runMethod);
            callContext.setInvokedInterface(callInterface);

            if (currentCreationalContext != null) {
                //noinspection unchecked
                currentCreationalContext.set(instance.creationalContext);
            }

            return _invoke(callMethod, runMethod, args, instance, callContext, type);

        } finally {
            if (runAs != null) {
                try {
                    securityService.associate(runAs);
                } catch (final LoginException e) {
                    // no-op
                }
            }
            ThreadContext.exit(oldCallContext);
            if (currentCreationalContext != null) {
                currentCreationalContext.remove();
            }
        }
    }

    private SecurityService getSecurityService() {
        return securityService;
    }

    protected Object _invoke(final Method callMethod,
                             final Method runMethod,
                             final Object[] args,
                             final Instance instance,
                             final ThreadContext callContext,
                             final InterfaceType callType) throws OpenEJBException {
        final BeanContext beanContext = callContext.getBeanContext();

        final Duration accessTimeout = getAccessTimeout(beanContext, runMethod);
        final boolean read = jakarta.ejb.LockType.READ.equals(beanContext.getConcurrencyAttribute(runMethod));

        final Lock lock = aquireLock(read, accessTimeout, instance, runMethod);

        Object returnValue;
        try {

            final TransactionPolicy txPolicy = createTransactionPolicy(beanContext.getTransactionType(callMethod, callType), callContext);

            returnValue = null;
            try {
                if (callType == InterfaceType.SERVICE_ENDPOINT) {
                    callContext.setCurrentOperation(Operation.BUSINESS_WS);
                    returnValue = invokeWebService(args, beanContext, runMethod, instance);
                } else {
                    final List<InterceptorData> interceptors = beanContext.getMethodInterceptors(runMethod);
                    final InterceptorStack interceptorStack = new InterceptorStack(instance.bean,
                        runMethod,
                        callType == InterfaceType.TIMEOUT ? Operation.TIMEOUT : Operation.BUSINESS,
                        interceptors,
                        instance.interceptors);
                    returnValue = interceptorStack.invoke(args);
                }
            } catch (final Throwable e) {// handle reflection exception
                final ExceptionType type = beanContext.getExceptionType(e);
                if (type == ExceptionType.SYSTEM) {
                    /* System Exception ****************************/

                    // The bean instance is not put into the pool via instanceManager.poolInstance
                    // and therefore the instance will be garbage collected and destroyed.
                    // For this reason the discardInstance method of the StatelessInstanceManager
                    // does nothing.
                    handleSystemException(txPolicy, e, callContext);
                } else {
                    /* Application Exception ***********************/

                    handleApplicationException(txPolicy, e, type == ExceptionType.APPLICATION_ROLLBACK);
                }
            } finally {
                afterInvoke(txPolicy, callContext);
            }
        } finally {
            lock.unlock();
        }

        return returnValue;
    }

    private Duration getAccessTimeout(final BeanContext beanContext, final Method callMethod) {
        Duration accessTimeout = beanContext.getAccessTimeout(callMethod);
        if (accessTimeout == null) {
            accessTimeout = beanContext.getAccessTimeout();
            if (accessTimeout == null) {
                accessTimeout = this.accessTimeout;
            }
        }
        return accessTimeout;
    }

    private Lock aquireLock(final boolean read, final Duration accessTimeout, final Instance instance, final Method runMethod) {
        final Lock lock;
        if (read) {
            lock = instance.lock.readLock();
        } else {
            lock = instance.lock.writeLock();
        }

        final boolean lockAcquired;
        if (accessTimeout == null || accessTimeout.getTime() < 0) {
            // wait indefinitely for a lock
            //noinspection LockAcquiredButNotSafelyReleased
            lock.lock();
            lockAcquired = true;
        } else if (accessTimeout.getTime() == 0) {
            // concurrent calls are not allowed, lock only once
            lockAcquired = lock.tryLock();
        } else {
            // try to get a lock within the specified period. 
            try {
                lockAcquired = lock.tryLock(accessTimeout.getTime(), accessTimeout.getUnit());
            } catch (final InterruptedException e) {
                throw (ConcurrentAccessTimeoutException) new ConcurrentAccessTimeoutException("Unable to get " +
                    (read ? "read" : "write") +
                    " lock within specified time on '" +
                    runMethod.getName() +
                    "' method for: " +
                    instance.bean.getClass().getName()).initCause(e);
            }
        }

        // Did we acquire the lock to the current execution?
        if (!lockAcquired) {
            throw new ConcurrentAccessTimeoutException("Unable to get " +
                (read ? "read" : "write") +
                " lock on '" +
                runMethod.getName() +
                "' method for: " +
                instance.bean.getClass().getName());
        }

        return lock;
    }

    private Object invokeWebService(final Object[] args, final BeanContext beanContext, final Method runMethod, final Instance instance) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException("WebService calls must follow format {messageContext, interceptor, [arg...]}.");
        }

        final Object messageContext = args[0];

        if (messageContext == null) {
            throw new IllegalArgumentException("MessageContext is null.");
        }

        // This object will be used as an interceptor in the stack and will be responsible
        // for unmarshalling the soap message parts into an argument list that will be
        // used for the actual method invocation.
        //
        // We just need to make it an interceptor in the OpenEJB sense and tack it on the end
        // of our stack.
        final Object interceptor = args[1];

        if (interceptor == null) {
            throw new IllegalArgumentException("Interceptor instance is null.");
        }

        final Class<?> interceptorClass = interceptor.getClass();

        //  Add the webservice interceptor to the list of interceptor instances
        final Map<String, Object> interceptors = new HashMap<>(instance.interceptors);
        {
            interceptors.put(interceptorClass.getName(), interceptor);
        }

        //  Create an InterceptorData for the webservice interceptor to the list of interceptorDatas for this method
        final List<InterceptorData> interceptorDatas = new ArrayList<>();
        {
            final InterceptorData providerData = new InterceptorData(interceptorClass);

            List<Method> aroundInvokes = interceptorCache.get(interceptorClass);
            if (aroundInvokes == null) {
                aroundInvokes = new ClassFinder(interceptorClass).findAnnotatedMethods(AroundInvoke.class);
                if (SingletonContainer.class.getClassLoader() == interceptorClass.getClassLoader()) { // use cache only for server classes
                    final List<Method> value = new CopyOnWriteArrayList<>(aroundInvokes);
                    aroundInvokes = interceptorCache.putIfAbsent(interceptorClass, value); // ensure it to be thread safe
                    if (aroundInvokes == null) {
                        aroundInvokes = value;
                    }
                }
            }

            providerData.getAroundInvoke().addAll(aroundInvokes);
            interceptorDatas.add(0, providerData);
            interceptorDatas.addAll(beanContext.getMethodInterceptors(runMethod));
        }

        final InterceptorStack interceptorStack = new InterceptorStack(instance.bean, runMethod, Operation.BUSINESS_WS, interceptorDatas, interceptors);
        final Object[] params = new Object[runMethod.getParameterTypes().length];
        if (messageContext instanceof jakarta.xml.ws.handler.MessageContext) {
            AddressingSupport wsaSupport = NoAddressingSupport.INSTANCE;
            for (int i = 2; i < args.length; i++) {
                if (args[i] instanceof AddressingSupport) {
                    wsaSupport = (AddressingSupport) args[i];
                }
            }
            ThreadContext.getThreadContext().set(AddressingSupport.class, wsaSupport);
            ThreadContext.getThreadContext().set(jakarta.xml.ws.handler.MessageContext.class, (jakarta.xml.ws.handler.MessageContext) messageContext);
            return interceptorStack.invoke((jakarta.xml.ws.handler.MessageContext) messageContext, params);
        }
        throw new IllegalArgumentException("Uknown MessageContext type: " + messageContext.getClass().getName());
    }

    protected ProxyInfo createEJBObject(final BeanContext beanContext, final Method callMethod) {
        return new ProxyInfo(beanContext, null);
    }
}
