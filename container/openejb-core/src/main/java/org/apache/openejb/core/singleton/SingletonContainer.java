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
package org.apache.openejb.core.singleton;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.TimeUnit;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.ConcurrentAccessTimeoutException;
import javax.interceptor.AroundInvoke;
import javax.transaction.TransactionManager;

import org.apache.openejb.ContainerType;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.util.Duration;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ExceptionType;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.interceptor.InterceptorStack;
import org.apache.openejb.core.timer.EjbTimerService;
import org.apache.openejb.core.transaction.TransactionContainer;
import org.apache.openejb.core.transaction.TransactionContext;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.spi.SecurityService;
import org.apache.xbean.finder.ClassFinder;

/**
 * @org.apache.xbean.XBean element="statelessContainer"
 */
public class SingletonContainer implements org.apache.openejb.RpcContainer, TransactionContainer {

    private SingletonInstanceManager instanceManager;

    private HashMap<String,DeploymentInfo> deploymentRegistry = new HashMap<String,DeploymentInfo>();

    private Object containerID = null;
    private TransactionManager transactionManager;
    private SecurityService securityService;
    private long wait = 30;
    private TimeUnit unit = TimeUnit.SECONDS;


    public SingletonContainer(Object id, TransactionManager transactionManager, SecurityService securityService) throws OpenEJBException {
        this.containerID = id;
        this.transactionManager = transactionManager;
        this.securityService = securityService;

        instanceManager = new SingletonInstanceManager(transactionManager, securityService);

        for (DeploymentInfo deploymentInfo : deploymentRegistry.values()) {
            org.apache.openejb.core.CoreDeploymentInfo di = (org.apache.openejb.core.CoreDeploymentInfo) deploymentInfo;
            di.setContainer(this);
        }
    }

    public void setAccessTimeout(Duration duration){
        this.unit = duration.getUnit();
        this.wait = duration.getTime();
    }

    public synchronized DeploymentInfo [] deployments() {
        return deploymentRegistry.values().toArray(new DeploymentInfo[deploymentRegistry.size()]);
    }

    public synchronized DeploymentInfo getDeploymentInfo(Object deploymentID) {
        String id = (String) deploymentID;
        return deploymentRegistry.get(id);
    }

    public ContainerType getContainerType() {
        return ContainerType.STATELESS;
    }

    public Object getContainerID() {
        return containerID;
    }

    public void deploy(DeploymentInfo info) throws OpenEJBException {
        CoreDeploymentInfo deploymentInfo = (CoreDeploymentInfo) info;
        instanceManager.deploy(deploymentInfo);
        String id = (String) deploymentInfo.getDeploymentID();
        synchronized (this) {
            deploymentRegistry.put(id, deploymentInfo);
            deploymentInfo.setContainer(this);
        }

        EjbTimerService timerService = deploymentInfo.getEjbTimerService();
        if (timerService != null) {
            timerService.start();
        }
    }

    public void undeploy(DeploymentInfo info) {
        undeploy((CoreDeploymentInfo)info);
    }

    private void undeploy(CoreDeploymentInfo deploymentInfo) {
        instanceManager.undeploy(deploymentInfo);
        EjbTimerService timerService = deploymentInfo.getEjbTimerService();
        if (timerService != null) {
            timerService.stop();
        }

        synchronized (this) {
            String id = (String) deploymentInfo.getDeploymentID();
            deploymentInfo.setContainer(null);
            deploymentInfo.setContainerData(null);
            deploymentRegistry.remove(id);
        }
    }

    /**
     * @deprecated use invoke signature without 'securityIdentity' argument.
     */
    public Object invoke(Object deployID, Method callMethod, Object[] args, Object primKey, Object securityIdentity) throws OpenEJBException {
        return invoke(deployID, callMethod.getDeclaringClass(), callMethod, args, primKey);
    }

    public Object invoke(Object deployID, Class callInterface, Method callMethod, Object [] args, Object primKey) throws OpenEJBException {
        CoreDeploymentInfo deployInfo = (CoreDeploymentInfo) this.getDeploymentInfo(deployID);
        if (deployInfo == null) throw new OpenEJBException("Deployment does not exist in this container. Deployment(id='"+deployID+"'), Container(id='"+containerID+"')");

        ThreadContext callContext = new ThreadContext(deployInfo, primKey);
        ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            boolean authorized = getSecurityService().isCallerAuthorized(callMethod, deployInfo.getInterfaceType(callInterface));
            if (!authorized)
                throw new org.apache.openejb.ApplicationException(new EJBAccessException("Unauthorized Access by Principal Denied"));

            Class declaringClass = callMethod.getDeclaringClass();
            if (EJBHome.class.isAssignableFrom(declaringClass) || EJBLocalHome.class.isAssignableFrom(declaringClass)) {
                if (callMethod.getName().startsWith("create")) {
                    return createEJBObject(deployInfo, callMethod);
                } else
                    return null;// EJBHome.remove( ) and other EJBHome methods are not process by the container
            } else if (EJBObject.class == declaringClass || EJBLocalObject.class == declaringClass) {
                return null;// EJBObject.remove( ) and other EJBObject methods are not process by the container
            }

            Instance instance = instanceManager.getInstance(callContext);

            callContext.setCurrentOperation(Operation.BUSINESS);
            callContext.setCurrentAllowedStates(SingletonContext.getStates());

            Method runMethod = deployInfo.getMatchingBeanMethod(callMethod);

            callContext.set(Method.class, runMethod);
            callContext.setInvokedInterface(callInterface);
            Object retValue = _invoke(callInterface, callMethod, runMethod, args, instance, callContext);

            return retValue;

        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    private SecurityService getSecurityService() {
        return securityService;
    }

    public SingletonInstanceManager getInstanceManager() {
        return instanceManager;
    }

    /**
     * @deprecated use type-safe {@link #_invoke(Class, java.lang.reflect.Method, java.lang.reflect.Method, Object[], Instance, org.apache.openejb.core.ThreadContext)}
     */
    protected Object _invoke(Class callInterface, Method callMethod, Method runMethod, Object[] args, Object object, ThreadContext callContext)
            throws OpenEJBException {
        return _invoke(callInterface, callMethod, runMethod, args, (Instance) object, callContext);
    }

    protected Object _invoke(Class callInterface, Method callMethod, Method runMethod, Object[] args, Instance instance, ThreadContext callContext)
            throws OpenEJBException {

        CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
        TransactionPolicy txPolicy = deploymentInfo.getTransactionPolicy(callMethod);
        TransactionContext txContext = new TransactionContext(callContext, getTransactionManager());
        txContext.callContext = callContext;


        boolean read = deploymentInfo.getConcurrencyAttribute(runMethod) == DeploymentInfo.READ_LOCK;
        
        final Lock lock = aquireLock(read, instance);

        Object returnValue;
        try {
            txPolicy.beforeInvoke(instance, txContext);

            returnValue = null;
            try {
                InterfaceType type = deploymentInfo.getInterfaceType(callInterface);
                if (type == InterfaceType.SERVICE_ENDPOINT){
                    callContext.setCurrentOperation(Operation.BUSINESS_WS);
                    returnValue = invokeWebService(args, deploymentInfo, runMethod, instance);
                } else {
                    List<InterceptorData> interceptors = deploymentInfo.getMethodInterceptors(runMethod);
                    InterceptorStack interceptorStack = new InterceptorStack(instance.bean, runMethod, Operation.BUSINESS, interceptors, instance.interceptors);
                    returnValue = interceptorStack.invoke(args);
                }
            } catch (Throwable re) {// handle reflection exception
                ExceptionType type = deploymentInfo.getExceptionType(re);
                if (type == ExceptionType.SYSTEM) {
                    /* System Exception ****************************/

                    /**
                     * The bean instance is not put into the pool via instanceManager.poolInstance
                     * and therefore the instance will be garbage collected and destroyed.
                     * For this reason the discardInstance method of the StatelessInstanceManager
                     * does nothing.
                     */

                    txPolicy.handleSystemException(re, instance, txContext);
                } else {
                    /* Application Exception ***********************/

                    txPolicy.handleApplicationException(re, type == ExceptionType.APPLICATION_ROLLBACK, txContext);
                }
            } finally {
                txPolicy.afterInvoke(instance, txContext);
            }
        } finally {
            lock.unlock();
        }

        return returnValue;
    }

    private Lock aquireLock(boolean read, Instance instance) {
        final Lock lock;
        if (read) {
            lock = instance.lock.readLock();
        } else {
            lock = instance.lock.writeLock();
        }

        try {
            if (!lock.tryLock(wait, unit)){
                throw new ConcurrentAccessTimeoutException();
            }
        } catch (InterruptedException e) {
            throw (ConcurrentAccessTimeoutException) new ConcurrentAccessTimeoutException().initCause(e);
        }
        return lock;
    }

    private Object invokeWebService(Object[] args, CoreDeploymentInfo deploymentInfo, Method runMethod, Instance instance) throws Exception {
        if (args.length != 2){
            throw new IllegalArgumentException("WebService calls must follow format {messageContext, interceptor}.");
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
        List<InterceptorData> interceptorDatas = new ArrayList<InterceptorData>(deploymentInfo.getMethodInterceptors(runMethod));
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
            ThreadContext.getThreadContext().set(javax.xml.ws.handler.MessageContext.class, (javax.xml.ws.handler.MessageContext) messageContext);
            return interceptorStack.invoke((javax.xml.ws.handler.MessageContext) messageContext, params);
        }
        throw new IllegalArgumentException("Uknown MessageContext type: " + messageContext.getClass().getName());
    }

    private TransactionManager getTransactionManager() {
        return transactionManager;
    }

    protected ProxyInfo createEJBObject(org.apache.openejb.core.CoreDeploymentInfo deploymentInfo, Method callMethod) {
        return new ProxyInfo(deploymentInfo, null);
    }

    public void discardInstance(Object instance, ThreadContext context) {
//        instanceManager.discardInstance(context, instance);
    }
}
