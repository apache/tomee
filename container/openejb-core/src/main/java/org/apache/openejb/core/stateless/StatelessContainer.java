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
package org.apache.openejb.core.stateless;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.interceptor.AroundInvoke;

import org.apache.openejb.*;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ExceptionType;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.interceptor.InterceptorStack;
import org.apache.openejb.core.timer.EjbTimerService;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.webservices.AddressingSupport;
import org.apache.openejb.core.webservices.NoAddressingSupport;

import static org.apache.openejb.core.transaction.EjbTransactionUtil.handleApplicationException;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.handleSystemException;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.afterInvoke;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.createTransactionPolicy;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.Pool;
import org.apache.xbean.finder.ClassFinder;

/**
 * @org.apache.xbean.XBean element="statelessContainer"
 */
public class StatelessContainer implements org.apache.openejb.RpcContainer {

    private StatelessInstanceManager instanceManager;

    private HashMap<String,DeploymentInfo> deploymentRegistry = new HashMap<String,DeploymentInfo>();

    private Object containerID = null;
    private SecurityService securityService;

    public StatelessContainer(Object id, SecurityService securityService, Duration accessTimeout, Duration closeTimeout, Pool.Builder poolBuilder, int callbackThreads) {
        this.containerID = id;
        this.securityService = securityService;

        instanceManager = new StatelessInstanceManager(securityService, accessTimeout, closeTimeout, poolBuilder, callbackThreads);

        for (DeploymentInfo deploymentInfo : deploymentRegistry.values()) {
            org.apache.openejb.core.CoreDeploymentInfo di = (org.apache.openejb.core.CoreDeploymentInfo) deploymentInfo;
            di.setContainer(this);
        }
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
        return invoke(deployID, null, callMethod.getDeclaringClass(), callMethod, args, primKey);
    }

    public Object invoke(Object deployID, Class callInterface, Method callMethod, Object[] args, Object primKey) throws OpenEJBException {
        return invoke(deployID, null, callInterface, callMethod, args, primKey);
    }

    public Object invoke(Object deployID, InterfaceType type, Class callInterface, Method callMethod, Object[] args, Object primKey) throws OpenEJBException {
        CoreDeploymentInfo deployInfo = (CoreDeploymentInfo) this.getDeploymentInfo(deployID);

        if (deployInfo == null) throw new OpenEJBException("Deployment does not exist in this container. Deployment(id='"+deployID+"'), Container(id='"+containerID+"')");

        // Use the backup way to determine call type if null was supplied.
        if (type == null) type = deployInfo.getInterfaceType(callInterface);

        ThreadContext callContext = new ThreadContext(deployInfo, primKey);
        ThreadContext oldCallContext = ThreadContext.enter(callContext);
        Object bean = null;
        try {
            boolean authorized = getSecurityService().isCallerAuthorized(callMethod, type);
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

            bean = instanceManager.getInstance(callContext);

            callContext.setCurrentOperation(Operation.BUSINESS);
            callContext.setCurrentAllowedStates(StatelessContext.getStates());

            Method runMethod = deployInfo.getMatchingBeanMethod(callMethod);

            callContext.set(Method.class, runMethod);
            callContext.setInvokedInterface(callInterface);
            Object retValue = _invoke(callMethod, runMethod, args, (Instance) bean, callContext, type);

            return retValue;

        } finally {
            if (bean != null) {
                if (callContext.isDiscardInstance()) {
                    instanceManager.discardInstance(callContext, bean);
                } else {
                    instanceManager.poolInstance(callContext, bean);
                }
            }
            ThreadContext.exit(oldCallContext);
        }
    }

    private SecurityService getSecurityService() {
        return securityService;
    }

    public StatelessInstanceManager getInstanceManager() {
        return instanceManager;
    }

    /**
     * @deprecated
     */
    protected Object _invoke(Class callInterface, Method callMethod, Method runMethod, Object[] args, Object object, ThreadContext callContext)
            throws OpenEJBException {
        return _invoke(callMethod, runMethod, args, (Instance) object, callContext, null);
    }

    protected Object _invoke(Method callMethod, Method runMethod, Object[] args, Instance instance, ThreadContext callContext, InterfaceType type)
            throws OpenEJBException {

        CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();

        TransactionPolicy txPolicy = createTransactionPolicy(deploymentInfo.getTransactionType(callMethod), callContext);

        Object returnValue = null;
        try {
            if (type == InterfaceType.SERVICE_ENDPOINT){
                callContext.setCurrentOperation(Operation.BUSINESS_WS);
                returnValue = invokeWebService(args, deploymentInfo, runMethod, instance, returnValue);
            } else {
                List<InterceptorData> interceptors = deploymentInfo.getMethodInterceptors(runMethod);
                InterceptorStack interceptorStack = new InterceptorStack(instance.bean, runMethod, Operation.BUSINESS, interceptors, instance.interceptors);
                returnValue = interceptorStack.invoke(args);
            }
        } catch (Throwable re) {// handle reflection exception
            ExceptionType exceptionType = deploymentInfo.getExceptionType(re);
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
            } catch (SystemException e) {
                callContext.setDiscardInstance(true);
                throw e;
            } catch (ApplicationException e) {
                throw e;
            } catch (RuntimeException e) {
                callContext.setDiscardInstance(true);
                throw e;
            }
        }

        return returnValue;
    }

    private Object invokeWebService(Object[] args, CoreDeploymentInfo deploymentInfo, Method runMethod, Instance instance, Object returnValue) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException("WebService calls must follow format {messageContext, interceptor, [arg...]}.");
        }

        Object messageContext = args[0];

        // This object will be used as an interceptor in the stack and will be responsible
        // for unmarshalling the soap message parts into an argument list that will be
        // used for the actual method invocation.
        //
        // We just need to make it an interceptor in the OpenEJB sense and tack it on the end
        // of our stack.
        Object interceptor = args[1];


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
//            interceptorDatas.add(providerData);
            interceptorDatas.add(0, providerData);
        }

        InterceptorStack interceptorStack = new InterceptorStack(instance.bean, runMethod, Operation.BUSINESS_WS, interceptorDatas, interceptors);
        Object[] params = new Object[runMethod.getParameterTypes().length];
        if (messageContext instanceof javax.xml.rpc.handler.MessageContext) {
            ThreadContext.getThreadContext().set(javax.xml.rpc.handler.MessageContext.class, (javax.xml.rpc.handler.MessageContext) messageContext);
            returnValue = interceptorStack.invoke((javax.xml.rpc.handler.MessageContext) messageContext, params);
        } else if (messageContext instanceof javax.xml.ws.handler.MessageContext) {
            AddressingSupport wsaSupport = NoAddressingSupport.INSTANCE;
            for (int i = 2; i < args.length; i++) {
                if (args[i] instanceof AddressingSupport) {
                    wsaSupport = (AddressingSupport)args[i];
                }
            }
            ThreadContext.getThreadContext().set(AddressingSupport.class, wsaSupport);
            ThreadContext.getThreadContext().set(javax.xml.ws.handler.MessageContext.class, (javax.xml.ws.handler.MessageContext) messageContext);
            returnValue = interceptorStack.invoke((javax.xml.ws.handler.MessageContext) messageContext, params);
        }
        return returnValue;
    }

    protected ProxyInfo createEJBObject(org.apache.openejb.core.CoreDeploymentInfo deploymentInfo, Method callMethod) {
        return new ProxyInfo(deploymentInfo, null);
    }
}
