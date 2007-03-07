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

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.ContainerType;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.timer.EjbTimerService;
import org.apache.openejb.core.transaction.TransactionContainer;
import org.apache.openejb.core.transaction.TransactionContext;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.spi.SecurityService;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.transaction.TransactionManager;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.HashMap;

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

    public Object invoke(Object deployID, Method callMethod, Object [] args, Object primKey, Object securityIdentity) throws OpenEJBException {
        CoreDeploymentInfo deployInfo = (CoreDeploymentInfo) this.getDeploymentInfo(deployID);
        if (deployInfo == null) throw new OpenEJBException("Deployment does not exist in this container. Deployment(id='"+deployID+"'), Container(id='"+containerID+"')");
        
        ThreadContext callContext = new ThreadContext(deployInfo, primKey, securityIdentity);
        ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            boolean authorized = getSecurityService().isCallerAuthorized(securityIdentity, deployInfo.getAuthorizedRoles(callMethod));
            if (!authorized)
                throw new org.apache.openejb.ApplicationException(new RemoteException("Unauthorized Access by Principal Denied"));

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

    protected Object _invoke(Method callMethod, Method runMethod, Object [] args, Object bean, ThreadContext callContext)
            throws org.apache.openejb.OpenEJBException {

        TransactionPolicy txPolicy = callContext.getDeploymentInfo().getTransactionPolicy(callMethod);
        TransactionContext txContext = new TransactionContext(callContext, getTransactionManager());
        txContext.callContext = callContext;

        txPolicy.beforeInvoke(bean, txContext);

        Object returnValue = null;
        try {

            returnValue = runMethod.invoke(bean, args);
        } catch (java.lang.reflect.InvocationTargetException ite) {// handle exceptions thrown by enterprise bean
            if (ite.getTargetException() instanceof RuntimeException) {
                /* System Exception ****************************/

                txPolicy.handleSystemException(ite.getTargetException(), bean, txContext);
            } else {
                /* Application Exception ***********************/
                instanceManager.poolInstance(callContext, bean);

                txPolicy.handleApplicationException(ite.getTargetException(), txContext);
            }
        } catch (Throwable re) {// handle reflection exception
            /*
              Any exception thrown by reflection; not by the enterprise bean. Possible
              Exceptions are:
                IllegalAccessException - if the underlying method is inaccessible.
                IllegalArgumentException - if the number of actual and formal parameters differ, or if an unwrapping conversion fails.
                NullPointerException - if the specified object is null and the method is an instance method.
                ExceptionInInitializerError - if the initialization provoked by this method fails.
            */
            txPolicy.handleSystemException(re, bean, txContext);
        } finally {

            txPolicy.afterInvoke(bean, txContext);
        }

        return returnValue;
    }

    private TransactionManager getTransactionManager() {
        return transactionManager;
    }

    protected ProxyInfo createEJBObject(org.apache.openejb.core.CoreDeploymentInfo deploymentInfo, Method callMethod) {
        Class callingClass = callMethod.getDeclaringClass();
        Class objectInterface = deploymentInfo.getObjectInterface(callingClass);
        return new ProxyInfo(deploymentInfo, null, objectInterface, this);
    }

    public void discardInstance(Object instance, ThreadContext context) {
        instanceManager.discardInstance(context, instance);
    }
}
