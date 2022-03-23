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

package org.apache.openejb.core.stateless;

import org.apache.openejb.BeanContext;
import org.apache.openejb.ContainerType;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.SystemException;
import org.apache.openejb.api.resource.DestroyableResource;
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
import org.apache.openejb.monitoring.StatsInterceptor;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.DaemonThreadFactory;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.Pool;
import org.apache.xbean.finder.ClassFinder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import jakarta.interceptor.AroundInvoke;
import javax.security.auth.login.LoginException;

import static org.apache.openejb.core.transaction.EjbTransactionUtil.afterInvoke;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.createTransactionPolicy;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.handleApplicationException;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.handleSystemException;

/**
 * @org.apache.xbean.XBean element="statelessContainer"
 */
public class StatelessContainer implements org.apache.openejb.RpcContainer, DestroyableResource {

    private final ConcurrentMap<Class<?>, List<Method>> interceptorCache = new ConcurrentHashMap<>();
    private final StatelessInstanceManager instanceManager;
    private final Map<String, BeanContext> deploymentRegistry = new ConcurrentHashMap<>();
    private final Object containerID;
    private final SecurityService securityService;

    public StatelessContainer(final Object id,
                              final SecurityService securityService,
                              final Duration accessTimeout,
                              final Duration closeTimeout,
                              final Pool.Builder poolBuilder,
                              final int callbackThreads,
                              final boolean useOneSchedulerThreadByBean,
                              final int evictionThreads) {
        this.containerID = id;
        this.securityService = securityService;
        this.instanceManager = new StatelessInstanceManager(
                securityService, accessTimeout, closeTimeout, poolBuilder, callbackThreads,
                useOneSchedulerThreadByBean ?
                        null :
                        Executors.newScheduledThreadPool(Math.max(evictionThreads, 1), new DaemonThreadFactory(id)));
    }

    @Override
    public BeanContext[] getBeanContexts() {
        return this.deploymentRegistry.values().toArray(new BeanContext[this.deploymentRegistry.size()]);
    }

    @Override
    public BeanContext getBeanContext(final Object deploymentID) {
        final String id = (String) deploymentID;
        return deploymentRegistry.get(id);
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.STATELESS;
    }

    @Override
    public Object getContainerID() {
        return containerID;
    }

    @Override
    public void deploy(final BeanContext beanContext) throws OpenEJBException {

        final String id = (String) beanContext.getDeploymentID();
        deploymentRegistry.put(id, beanContext);
        beanContext.setContainer(this);

        // add it before starting the timer (@PostCostruct)
        if (StatsInterceptor.isStatsActivated()) {
            final StatsInterceptor stats = new StatsInterceptor(beanContext.getBeanClass());
            beanContext.addFirstSystemInterceptor(stats);
        }
    }

    @Override
    public void start(final BeanContext beanContext) throws OpenEJBException {
        this.instanceManager.deploy(beanContext);

        final EjbTimerService timerService = beanContext.getEjbTimerService();
        if (timerService != null) {
            timerService.start();
        }
    }

    @Override
    public void stop(final BeanContext beanContext) throws OpenEJBException {
        beanContext.stop();
    }

    @Override
    public void undeploy(final BeanContext beanContext) {
        this.instanceManager.undeploy(beanContext);
        final String id = (String) beanContext.getDeploymentID();
        beanContext.setContainer(null);
        beanContext.setContainerData(null);
        this.deploymentRegistry.remove(id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(final Object deployID,
                         InterfaceType type,
                         final Class callInterface,
                         final Method callMethod,
                         final Object[] args,
                         final Object primKey) throws OpenEJBException {
        final BeanContext beanContext = this.getBeanContext(deployID);

        if (beanContext == null) {
            final String msg = "Deployment does not exist in this container. Deployment(id='" + deployID + "'), Container(id='" + containerID + "')";
            throw new OpenEJBException(msg);
        }

        // Use the backup way to determine call type if null was supplied.
        if (type == null) {
            type = beanContext.getInterfaceType(callInterface);
        }

        final Method runMethod = beanContext.getMatchingBeanMethod(callMethod);
        final ThreadContext callContext = new ThreadContext(beanContext, primKey);
        final ThreadContext oldCallContext = ThreadContext.enter(callContext);

        Instance bean = null;
        final CurrentCreationalContext currentCreationalContext = beanContext.get(CurrentCreationalContext.class);

        Object runAs = null;
        try {
            if (oldCallContext != null) {
                final BeanContext oldBc = oldCallContext.getBeanContext();
                if (oldBc.getRunAsUser() != null || oldBc.getRunAs() != null) {
                    runAs = AbstractSecurityService.class.cast(securityService).overrideWithRunAsContext(callContext, beanContext, oldBc);
                }
            }

            //Check auth before overriding context
            final boolean authorized = type == InterfaceType.TIMEOUT || this.securityService.isCallerAuthorized(callMethod, type);

            if (!authorized) {
                throw new org.apache.openejb.ApplicationException(new jakarta.ejb.EJBAccessException("Unauthorized Access by Principal Denied"));
            }

            final Class declaringClass = callMethod.getDeclaringClass();
            if (jakarta.ejb.EJBHome.class.isAssignableFrom(declaringClass) || jakarta.ejb.EJBLocalHome.class.isAssignableFrom(declaringClass)) {
                if (callMethod.getName().startsWith("create")) {
                    return new ProxyInfo(beanContext, null);
                } else {
                    return null; // EJBHome.remove( ) and other EJBHome methods are not process by the container
                }

            } else if (jakarta.ejb.EJBObject.class == declaringClass || jakarta.ejb.EJBLocalObject.class == declaringClass) {
                return null; // EJBObject.remove( ) and other EJBObject methods are not process by the container
            }

            bean = this.instanceManager.getInstance(callContext);

            callContext.setCurrentOperation(type == InterfaceType.TIMEOUT ? Operation.TIMEOUT : Operation.BUSINESS);
            callContext.set(Method.class, runMethod);
            callContext.setInvokedInterface(callInterface);
            if (currentCreationalContext != null) {
                currentCreationalContext.set(bean.creationalContext);
            }
            return _invoke(callMethod, runMethod, args, bean, callContext, type);
        } finally {
            if (runAs != null) {
                try {
                    securityService.associate(runAs);
                } catch (final LoginException e) {
                    // no-op
                }
            }
            if (bean != null) {
                if (callContext.isDiscardInstance()) {
                    this.instanceManager.discardInstance(callContext, bean);
                } else {
                    this.instanceManager.poolInstance(callContext, bean);
                }
            }

            ThreadContext.exit(oldCallContext);

            if (currentCreationalContext != null) {
                currentCreationalContext.remove();
            }
        }
    }

    @SuppressWarnings("ThrowFromFinallyBlock")
    private Object _invoke(final Method callMethod, final Method runMethod, final Object[] args, final Instance instance, final ThreadContext callContext, final InterfaceType type)
        throws OpenEJBException {
        final BeanContext beanContext = callContext.getBeanContext();
        final TransactionPolicy txPolicy = createTransactionPolicy(beanContext.getTransactionType(callMethod, type), callContext);

        Object returnValue = null;
        try {
            if (type == InterfaceType.SERVICE_ENDPOINT) {
                callContext.setCurrentOperation(Operation.BUSINESS_WS);
                returnValue = invokeWebService(args, beanContext, runMethod, instance);
            } else {
                final List<InterceptorData> interceptors = beanContext.getMethodInterceptors(runMethod);
                final Operation operation = type == InterfaceType.TIMEOUT ? Operation.TIMEOUT : Operation.BUSINESS;
                final InterceptorStack interceptorStack = new InterceptorStack(instance.bean, runMethod, operation, interceptors, instance.interceptors);
                returnValue = interceptorStack.invoke(args);
            }
        } catch (final Throwable re) {// handle reflection exception
            final ExceptionType exceptionType = beanContext.getExceptionType(re);
            if (exceptionType == ExceptionType.SYSTEM) {
                /* System Exception ****************************/

                // The bean instance is not put into the pool via instanceManager.poolInstance
                // and therefore the instance will be garbage collected and destroyed.
                // In case of StrictPooling flag being set to true we also release the semaphore
                // in the discardInstance method of the instanceManager.
                callContext.setDiscardInstance(true);
                handleSystemException(txPolicy, re, callContext);
            } else {
                /* Application Exception ***********************/
                handleApplicationException(txPolicy, re, exceptionType == ExceptionType.APPLICATION_ROLLBACK);
            }
        } finally {
            try {
                afterInvoke(txPolicy, callContext);
            } catch (final SystemException | RuntimeException e) {
                callContext.setDiscardInstance(true);
                throw e;
            }
        }
        return returnValue;
    }

    private Object invokeWebService(final Object[] args, final BeanContext beanContext, final Method runMethod, final Instance instance) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException("WebService calls must follow format {messageContext, interceptor, [arg...]}.");
        }

        final Object messageContext = args[0];

        // This object will be used as an interceptor in the stack and will be responsible
        // for unmarshalling the soap message parts into an argument list that will be
        // used for the actual method invocation.
        //
        // We just need to make it an interceptor in the OpenEJB sense and tack it on the end
        // of our stack.
        final Object interceptor = args[1];
        final Class<?> interceptorClass = interceptor.getClass();

        //  Add the webservice interceptor to the list of interceptor instances
        final Map<String, Object> interceptors = new HashMap<>(instance.interceptors);
        interceptors.put(interceptor.getClass().getName(), interceptor);

        //  Create an InterceptorData for the webservice interceptor to the list of interceptorDatas for this method
        final List<InterceptorData> interceptorDatas = new ArrayList<>();
        final InterceptorData providerData = new InterceptorData(interceptorClass);
        providerData.getAroundInvoke().addAll(retrieveAroundInvokes(interceptorClass));
        interceptorDatas.add(0, providerData);
        interceptorDatas.addAll(beanContext.getMethodInterceptors(runMethod));

        final InterceptorStack interceptorStack = new InterceptorStack(instance.bean, runMethod, Operation.BUSINESS_WS, interceptorDatas, interceptors);
        final Object[] params = new Object[runMethod.getParameterTypes().length];
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        Object returnValue = null;
        if (messageContext instanceof jakarta.xml.ws.handler.MessageContext) {
            AddressingSupport wsaSupport = NoAddressingSupport.INSTANCE;
            for (int i = 2; i < args.length; i++) {
                if (args[i] instanceof AddressingSupport) {
                    wsaSupport = (AddressingSupport) args[i];
                }
            }
            threadContext.set(AddressingSupport.class, wsaSupport);
            threadContext.set(jakarta.xml.ws.handler.MessageContext.class, (jakarta.xml.ws.handler.MessageContext) messageContext);
            returnValue = interceptorStack.invoke((jakarta.xml.ws.handler.MessageContext) messageContext, params);
        }
        return returnValue;
    }

    private List<Method> retrieveAroundInvokes(final Class<?> interceptorClass) {
        final List<Method> cached = this.interceptorCache.get(interceptorClass);
        if (cached != null) {
            return cached;
        }

        final ClassFinder finder = new ClassFinder(interceptorClass);
        List<Method> annotated = finder.findAnnotatedMethods(AroundInvoke.class);
        if (StatelessContainer.class.getClassLoader() == interceptorClass.getClassLoader()) { // use cache only for server classes
            final List<Method> value = new CopyOnWriteArrayList<>(annotated);
            annotated = this.interceptorCache.putIfAbsent(interceptorClass, annotated); // ensure it to be thread safe
            if (annotated == null) {
                annotated = value;
            }
        }
        return annotated;
    }

    @Override
    public void destroyResource() {
        this.instanceManager.destroy();
    }
}
