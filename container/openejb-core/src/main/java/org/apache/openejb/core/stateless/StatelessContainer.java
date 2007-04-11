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
import javax.transaction.TransactionManager;

import org.apache.openejb.ContainerType;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
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
public class StatelessContainer implements org.apache.openejb.RpcContainer, TransactionContainer {

    private StatelessInstanceManager instanceManager;

    private HashMap<String,DeploymentInfo> deploymentRegistry = new HashMap<String,DeploymentInfo>();

    private Object containerID = null;
    private TransactionManager transactionManager;
    private SecurityService securityService;

    public StatelessContainer(Object id, TransactionManager transactionManager, SecurityService securityService, int timeOut, int poolSize, boolean strictPooling) throws OpenEJBException {
        this.containerID = id;
        this.transactionManager = transactionManager;
        this.securityService = securityService;

        instanceManager = new StatelessInstanceManager(transactionManager, securityService, timeOut, poolSize, strictPooling);

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
        return invoke(deployID, callMethod, args, primKey);
    }

    public Object invoke(Object deployID, Method callMethod, Object [] args, Object primKey) throws OpenEJBException {
        CoreDeploymentInfo deployInfo = (CoreDeploymentInfo) this.getDeploymentInfo(deployID);
        if (deployInfo == null) throw new OpenEJBException("Deployment does not exist in this container. Deployment(id='"+deployID+"'), Container(id='"+containerID+"')");

        ThreadContext callContext = new ThreadContext(deployInfo, primKey);
        ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            boolean authorized = getSecurityService().isCallerAuthorized(callMethod, null);
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

            Object bean = instanceManager.getInstance(callContext);

            callContext.setCurrentOperation(Operation.BUSINESS);
            callContext.setCurrentAllowedStates(StatelessContext.getStates());

            Method runMethod = deployInfo.getMatchingBeanMethod(callMethod);

            callContext.set(Method.class, runMethod);

            Object retValue = _invoke(callMethod, runMethod, args, bean, callContext);
            instanceManager.poolInstance(callContext, bean);

            return retValue;

        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    private SecurityService getSecurityService() {
        return securityService;
    }

    public StatelessInstanceManager getInstanceManager() {
        return instanceManager;
    }

    protected Object _invoke(Method callMethod, Method runMethod, Object [] args, Object object, ThreadContext callContext)
            throws org.apache.openejb.OpenEJBException {
        Instance instance = (Instance) object;

        CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
        TransactionPolicy txPolicy = deploymentInfo.getTransactionPolicy(callMethod);
        TransactionContext txContext = new TransactionContext(callContext, getTransactionManager());
        txContext.callContext = callContext;

        txPolicy.beforeInvoke(instance, txContext);

        Object returnValue = null;
        try {
            if (isWebServiceCall(deploymentInfo, callMethod, args)){
                returnValue = invokeWebService(args, deploymentInfo, runMethod, instance, returnValue);
            } else {
                List<InterceptorData> interceptors = deploymentInfo.getMethodInterceptors(runMethod);
                InterceptorStack interceptorStack = new InterceptorStack(instance.bean, runMethod, Operation.BUSINESS, interceptors, instance.interceptors);
                returnValue = interceptorStack.invoke(args);
            }
        } catch (java.lang.reflect.InvocationTargetException ite) {// handle exceptions thrown by enterprise bean
            if (!isApplicationException(deploymentInfo, ite.getTargetException())) {
                /* System Exception ****************************/

                txPolicy.handleSystemException(ite.getTargetException(), instance, txContext);
            } else {
                /* Application Exception ***********************/
                instanceManager.poolInstance(callContext, instance);

                txPolicy.handleApplicationException(ite.getTargetException(), txContext);
            }
        } catch (Throwable re) {// handle reflection exception
            if (!isApplicationException(deploymentInfo, re)) {
                /* System Exception ****************************/

                txPolicy.handleSystemException(re, instance, txContext);
            } else {
                /* Application Exception ***********************/
                instanceManager.poolInstance(callContext, instance);

                txPolicy.handleApplicationException(re, txContext);
            }
            /*
              Any exception thrown by reflection; not by the enterprise bean. Possible
              Exceptions are:
                IllegalAccessException - if the underlying method is inaccessible.
                IllegalArgumentException - if the number of actual and formal parameters differ, or if an unwrapping conversion fails.
                NullPointerException - if the specified object is null and the method is an instance method.
                ExceptionInInitializerError - if the initialization provoked by this method fails.
            */
//            txPolicy.handleSystemException(re, bean, txContext);
        } finally {

            txPolicy.afterInvoke(instance, txContext);
        }

        return returnValue;
    }

    private Object invokeWebService(Object[] args, CoreDeploymentInfo deploymentInfo, Method runMethod, Instance instance, Object returnValue) throws Exception {
        if (args.length != 2){
            throw new IllegalArgumentException("WebService calls must follow format {messageContext, interceptor}.");
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
        Map<String, Object> interceptors = new HashMap(instance.interceptors);
        {
            interceptors.put(interceptor.getClass().getName(), interceptor);
        }

        //  Create an InterceptorData for the webservice interceptor to the list of interceptorDatas for this method
        List<InterceptorData> interceptorDatas = new ArrayList(deploymentInfo.getMethodInterceptors(runMethod));
        {
            InterceptorData providerData = new InterceptorData(interceptor.getClass());
            ClassFinder finder = new ClassFinder(interceptor.getClass());
            providerData.getAroundInvoke().addAll(finder.findAnnotatedMethods(AroundInvoke.class));
            interceptorDatas.add(providerData);
        }

        InterceptorStack interceptorStack = new InterceptorStack(instance.bean, runMethod, Operation.BUSINESS_WS, interceptorDatas, interceptors);

        if (messageContext instanceof javax.xml.rpc.handler.MessageContext) {
            returnValue = interceptorStack.invoke((javax.xml.rpc.handler.MessageContext) messageContext);
        } else if (messageContext instanceof javax.xml.ws.handler.MessageContext) {
            returnValue = interceptorStack.invoke((javax.xml.ws.handler.MessageContext) messageContext);
        }
        return returnValue;
    }

    private boolean isWebServiceCall(DeploymentInfo deployment, Method callMethod, Object[] args) {
        Class serviceEndpointInterface = deployment.getServiceEndpointInterface();
        // DMB: This will be a problem if the calling method is in an interface and the
        // service-endpoint interface extends that interface.
        return (serviceEndpointInterface != null && serviceEndpointInterface.isAssignableFrom(callMethod.getDeclaringClass()));
    }

    private boolean isApplicationException(DeploymentInfo deploymentInfo, Throwable e) {
        return e instanceof Exception && !(e instanceof RuntimeException);
    }

    private TransactionManager getTransactionManager() {
        return transactionManager;
    }

    protected ProxyInfo createEJBObject(org.apache.openejb.core.CoreDeploymentInfo deploymentInfo, Method callMethod) {
        Class callingClass = callMethod.getDeclaringClass();
        List<Class> objectInterface = deploymentInfo.getObjectInterface(callingClass);
        return new ProxyInfo(deploymentInfo, null, objectInterface, this);
    }

    public void discardInstance(Object instance, ThreadContext context) {
        instanceManager.discardInstance(context, instance);
    }
}
