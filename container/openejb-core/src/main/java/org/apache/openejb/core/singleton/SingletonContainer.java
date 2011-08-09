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

import static org.apache.openejb.core.transaction.EjbTransactionUtil.afterInvoke;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.createTransactionPolicy;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.handleApplicationException;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.handleSystemException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import javax.ejb.ConcurrentAccessTimeoutException;
import javax.ejb.EJBAccessException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.interceptor.AroundInvoke;

import org.apache.openejb.BeanContext;
import org.apache.openejb.ContainerType;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.core.ExceptionType;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.interceptor.InterceptorStack;
import org.apache.openejb.core.timer.EjbTimerService;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.webservices.AddressingSupport;
import org.apache.openejb.core.webservices.NoAddressingSupport;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Duration;
import org.apache.xbean.finder.ClassFinder;

/**
 * @org.apache.xbean.XBean element="statelessContainer"
 */
public class SingletonContainer implements RpcContainer {

    private SingletonInstanceManager instanceManager;

    private HashMap<String, BeanContext> deploymentRegistry = new HashMap<String, BeanContext>();

    private Object containerID = null;
    private SecurityService securityService;
    private Duration accessTimeout;

    public SingletonContainer(Object id, SecurityService securityService) throws OpenEJBException {
        this.containerID = id;
        this.securityService = securityService;

        instanceManager = new SingletonInstanceManager(securityService);

        for (BeanContext beanContext : deploymentRegistry.values()) {
            beanContext.setContainer(this);
        }
    }

    public void setAccessTimeout(Duration duration){
        this.accessTimeout = duration;
    }

    public synchronized BeanContext[] getBeanContexts() {
        return deploymentRegistry.values().toArray(new BeanContext[deploymentRegistry.size()]);
    }

    public synchronized BeanContext getBeanContext(Object deploymentID) {
        String id = (String) deploymentID;
        return deploymentRegistry.get(id);
    }

    public ContainerType getContainerType() {
        return ContainerType.SINGLETON;
    }

    public Object getContainerID() {
        return containerID;
    }

    public void deploy(BeanContext beanContext) throws OpenEJBException {
        instanceManager.deploy(beanContext);
        String id = (String) beanContext.getDeploymentID();
        synchronized (this) {
            deploymentRegistry.put(id, beanContext);
            beanContext.setContainer(this);
        }

        EjbTimerService timerService = beanContext.getEjbTimerService();
        if (timerService != null) {
            timerService.start();
        }
        
    }
    
    public void start(BeanContext info) throws OpenEJBException {
        instanceManager.start(info);
    }
    
    public void stop(BeanContext info) throws OpenEJBException {
    }
    
    public void undeploy(BeanContext beanContext) {
        ThreadContext threadContext = new ThreadContext(beanContext, null);
        ThreadContext old = ThreadContext.enter(threadContext);
        try {
            instanceManager.freeInstance(threadContext);
        } finally{
            ThreadContext.exit(old);
        }
        
        EjbTimerService timerService = beanContext.getEjbTimerService();
        if (timerService != null) {
            timerService.stop();
        }
        
        instanceManager.undeploy(beanContext);
        
        synchronized (this) {
            String id = (String) beanContext.getDeploymentID();
            beanContext.setContainer(null);
            beanContext.setContainerData(null);
            deploymentRegistry.remove(id);
        }
    }

    /**
     * @deprecated use invoke signature without 'securityIdentity' argument.
     */
    public Object invoke(Object deployID, Method callMethod, Object[] args, Object primKey, Object securityIdentity) throws OpenEJBException {
        return invoke(deployID, null, callMethod.getDeclaringClass(), callMethod, args, primKey);
    }

    public Object invoke(Object deployID, Class callInterface, Method callMethod, Object[] args, Object primKey) throws OpenEJBException {
        return invoke(deployID, null, callInterface, callMethod, args, primKey);
    }

    public Object invoke(Object deployID, InterfaceType type, Class callInterface, Method callMethod, Object[] args, Object primKey) throws OpenEJBException {
        BeanContext beanContext = this.getBeanContext(deployID);

        if (beanContext == null) throw new OpenEJBException("Deployment does not exist in this container. Deployment(id='"+deployID+"'), Container(id='"+containerID+"')");

        // Use the backup way to determine call type if null was supplied.
        if (type == null) type = beanContext.getInterfaceType(callInterface);

        Method runMethod = beanContext.getMatchingBeanMethod(callMethod);

        ThreadContext callContext = new ThreadContext(beanContext, primKey);
        ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            boolean authorized = type == InterfaceType.TIMEOUT || getSecurityService().isCallerAuthorized(callMethod, type);
            if (!authorized)
                throw new org.apache.openejb.ApplicationException(new EJBAccessException("Unauthorized Access by Principal Denied"));

            Class declaringClass = callMethod.getDeclaringClass();
            if (EJBHome.class.isAssignableFrom(declaringClass) || EJBLocalHome.class.isAssignableFrom(declaringClass)) {
                if (callMethod.getName().startsWith("create")) {
                    return createEJBObject(beanContext, callMethod);
                } else
                    return null;// EJBHome.remove( ) and other EJBHome methods are not process by the container
            } else if (EJBObject.class == declaringClass || EJBLocalObject.class == declaringClass) {
                return null;// EJBObject.remove( ) and other EJBObject methods are not process by the container
            }

            Instance instance = instanceManager.getInstance(callContext);

            callContext.setCurrentOperation(type == InterfaceType.TIMEOUT ? Operation.TIMEOUT : Operation.BUSINESS);
            callContext.setCurrentAllowedStates(null);
            callContext.set(Method.class, runMethod);
            callContext.setInvokedInterface(callInterface);

            Object retValue = _invoke(callMethod, runMethod, args, instance, callContext, type);

            return retValue;

        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    private SecurityService getSecurityService() {
        return securityService;
    }

    protected Object _invoke(Method callMethod, Method runMethod, Object[] args, Instance instance, ThreadContext callContext, InterfaceType callType) throws OpenEJBException {
        BeanContext beanContext = callContext.getBeanContext();
               
        Duration accessTimeout = getAccessTimeout(beanContext, runMethod);
        boolean read = beanContext.getConcurrencyAttribute(runMethod) == javax.ejb.LockType.READ;
        
        final Lock lock = aquireLock(read, accessTimeout, instance);

        Object returnValue;
        try {
            TransactionPolicy txPolicy = createTransactionPolicy(beanContext.getTransactionType(callMethod, callType), callContext);

            returnValue = null;
            try {
                if (callType == InterfaceType.SERVICE_ENDPOINT) {
                    callContext.setCurrentOperation(Operation.BUSINESS_WS);
                    returnValue = invokeWebService(args, beanContext, runMethod, instance);
                } else {
                    List<InterceptorData> interceptors = beanContext.getMethodInterceptors(runMethod);
                    InterceptorStack interceptorStack = new InterceptorStack(instance.bean, runMethod, callType == InterfaceType.TIMEOUT ? Operation.TIMEOUT : Operation.BUSINESS, interceptors,
                            instance.interceptors);
                    returnValue = interceptorStack.invoke(args);
                }
            } catch (Throwable e) {// handle reflection exception
                ExceptionType type = beanContext.getExceptionType(e);
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

    private Duration getAccessTimeout(BeanContext beanContext, Method callMethod) {
        Duration accessTimeout = beanContext.getAccessTimeout(callMethod);
        if (accessTimeout == null) {
            accessTimeout = beanContext.getAccessTimeout();
            if (accessTimeout == null) {
                accessTimeout = this.accessTimeout;
            }
        }
        return accessTimeout;
    }
    
    private Lock aquireLock(boolean read, Duration accessTimeout, Instance instance) {
        final Lock lock;
        if (read) {
            lock = instance.lock.readLock();
        } else {
            lock = instance.lock.writeLock();
        }
        
        boolean lockAcquired;
        if (accessTimeout == null || accessTimeout.getTime() < 0) {
            // wait indefinitely for a lock
            lock.lock();
            lockAcquired = true;
        } else if (accessTimeout.getTime() == 0) {
            // concurrent calls are not allowed, lock only once
            lockAcquired = lock.tryLock();
        } else {
            // try to get a lock within the specified period. 
            try {
                lockAcquired = lock.tryLock(accessTimeout.getTime(), accessTimeout.getUnit());
            } catch (InterruptedException e) {
                throw (ConcurrentAccessTimeoutException) new ConcurrentAccessTimeoutException("Unable to get lock within specified time on: " + instance).initCause(e);
            }
        }

        // Did we acquire the lock to the current execution?
        if (!lockAcquired) {
            throw new ConcurrentAccessTimeoutException("Unable to get lock on: " + instance);
        }

        return lock;
    }

    private Object invokeWebService(Object[] args, BeanContext beanContext, Method runMethod, Instance instance) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException("WebService calls must follow format {messageContext, interceptor, [arg...]}.");
        }

        Object messageContext = args[0];

        if (messageContext == null) throw new IllegalArgumentException("MessageContext is null.");

        // This object will be used as an interceptor in the stack and will be responsible
        // for unmarshalling the soap message parts into an argument list that will be
        // used for the actual method invocation.
        //
        // We just need to make it an interceptor in the OpenEJB sense and tack it on the end
        // of our stack.
        Object interceptor = args[1];

        if (interceptor == null) throw new IllegalArgumentException("Interceptor instance is null.");

        //  Add the webservice interceptor to the list of interceptor instances
        Map<String, Object> interceptors = new HashMap<String, Object>(instance.interceptors);
        {
            interceptors.put(interceptor.getClass().getName(), interceptor);
        }

        //  Create an InterceptorData for the webservice interceptor to the list of interceptorDatas for this method
        List<InterceptorData> interceptorDatas = new ArrayList<InterceptorData>(beanContext.getMethodInterceptors(runMethod));
        {
            InterceptorData providerData = new InterceptorData(interceptor.getClass());
            ClassFinder finder = new ClassFinder(interceptor.getClass());
            providerData.getAroundInvoke().addAll(finder.findAnnotatedMethods(AroundInvoke.class));
            interceptorDatas.add(providerData);
        }

        InterceptorStack interceptorStack = new InterceptorStack(instance.bean, runMethod, Operation.BUSINESS_WS, interceptorDatas, interceptors);
        Object[] params = new Object[runMethod.getParameterTypes().length];
        if (messageContext instanceof javax.xml.rpc.handler.MessageContext) {
            ThreadContext.getThreadContext().set(javax.xml.rpc.handler.MessageContext.class, (javax.xml.rpc.handler.MessageContext) messageContext);
            return interceptorStack.invoke((javax.xml.rpc.handler.MessageContext) messageContext, params);
        } else if (messageContext instanceof javax.xml.ws.handler.MessageContext) {
            AddressingSupport wsaSupport = NoAddressingSupport.INSTANCE;
            for (int i = 2; i < args.length; i++) {
                if (args[i] instanceof AddressingSupport) {
                    wsaSupport = (AddressingSupport)args[i];
                }
            }
            ThreadContext.getThreadContext().set(AddressingSupport.class, wsaSupport);
            ThreadContext.getThreadContext().set(javax.xml.ws.handler.MessageContext.class, (javax.xml.ws.handler.MessageContext) messageContext);
            return interceptorStack.invoke((javax.xml.ws.handler.MessageContext) messageContext, params);
        }
        throw new IllegalArgumentException("Uknown MessageContext type: " + messageContext.getClass().getName());
    }

    protected ProxyInfo createEJBObject(BeanContext beanContext, Method callMethod) {
        return new ProxyInfo(beanContext, null);
    }
}
