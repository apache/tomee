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
package org.apache.openejb.core.stateful;

import org.apache.openejb.Container;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.ApplicationException;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.transaction.TransactionContainer;
import org.apache.openejb.core.transaction.TransactionContext;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Index;

import javax.ejb.SessionBean;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionRequiredException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.dgc.VMID;
import java.util.HashMap;
import java.util.Map;

/**
 * @org.apache.xbean.XBean element="statefulContainer"
 */
public class StatefulContainer implements RpcContainer, TransactionContainer {
    private static final Logger logger = Logger.getInstance("OpenEJB", "org.apache.openejb.util.resources");

    private final Object containerID;
    private final TransactionManager transactionManager;
    private final SecurityService securityService;
    private final StatefulInstanceManager instanceManager;

    /**
     * Index used for getDeployments() and getDeploymentInfo(deploymentId).
     */
    protected final Map<Object, DeploymentInfo> deploymentsById = new HashMap<Object, DeploymentInfo>();


    public StatefulContainer(Object id, TransactionManager transactionManager, SecurityService securityService, Class passivator, int timeOut, int poolSize, int bulkPassivate) throws OpenEJBException {
        this.containerID = id;
        this.transactionManager = transactionManager;
        this.securityService = securityService;

        instanceManager = new StatefulInstanceManager(transactionManager, securityService, passivator, timeOut, poolSize, bulkPassivate);
    }

    private class Data {
        private final Index<Method,MethodType> methodIndex;

        private Data(Index<Method,MethodType> methodIndex) {
            this.methodIndex = methodIndex;
        }

        public Index<Method, MethodType> getMethodIndex() {
            return methodIndex;
        }
    }

    private Map<Method, MethodType> getLifecycelMethodsOfInterface(CoreDeploymentInfo deploymentInfo) {
        Map<Method, MethodType> methods = new HashMap<Method, MethodType>();

        Method preDestroy = deploymentInfo.getPreDestroy();
        if (preDestroy != null){
            methods.put(preDestroy, MethodType.REMOVE);

            Class businessLocal = deploymentInfo.getBusinessLocalInterface();
            if (businessLocal != null){
                try {
                    Method method = businessLocal.getMethod(preDestroy.getName());
                    methods.put(method, MethodType.REMOVE);
                } catch (NoSuchMethodException thatsFine) {}
            }

            Class businessRemote = deploymentInfo.getBusinessRemoteInterface();
            if (businessRemote != null){
                try {
                    Method method = businessRemote.getMethod(preDestroy.getName());
                    methods.put(method, MethodType.REMOVE);
                } catch (NoSuchMethodException thatsFine) {}
            }
        }

        Class legacyRemote = deploymentInfo.getRemoteInterface();
        if (legacyRemote != null){
            try {
                Method method = legacyRemote.getMethod("remove");
                methods.put(method, MethodType.REMOVE);
            } catch (NoSuchMethodException thatsFine) {}
        }

        Class legacyLocal = deploymentInfo.getLocalInterface();
        if (legacyLocal != null){
            try {
                Method method = legacyLocal.getMethod("remove");
                methods.put(method, MethodType.REMOVE);
            } catch (NoSuchMethodException thatsFine) {}
        }

        Class businessLocalHomeInterface = deploymentInfo.getBusinessLocalInterface();
        if (businessLocalHomeInterface != null){
            for (Method method : DeploymentInfo.BusinessLocalHome.class.getMethods()) {
                if (method.getName().startsWith("create")){
                    methods.put(method, MethodType.CREATE);
                } else if (method.getName().equals("remove")){
                    methods.put(method, MethodType.REMOVE);
                }
            }
        }

        Class businessRemoteHomeInterface = deploymentInfo.getBusinessRemoteInterface();
        if (businessRemoteHomeInterface != null){
            for (Method method : DeploymentInfo.BusinessRemoteHome.class.getMethods()) {
                if (method.getName().startsWith("create")){
                    methods.put(method, MethodType.CREATE);
                } else if (method.getName().equals("remove")){
                    methods.put(method, MethodType.REMOVE);
                }
            }
        }

        Class homeInterface = deploymentInfo.getHomeInterface();
        if (homeInterface != null){
            for (Method method : homeInterface.getMethods()) {
                if (method.getName().startsWith("create")){
                    methods.put(method, MethodType.CREATE);
                } else if (method.getName().equals("remove")){
                    methods.put(method, MethodType.REMOVE);
                }
            }
        }

        Class localHomeInterface = deploymentInfo.getLocalHomeInterface();
        if (localHomeInterface != null){
            for (Method method : localHomeInterface.getMethods()) {
                if (method.getName().startsWith("create")){
                    methods.put(method, MethodType.CREATE);
                } else if (method.getName().equals("remove")){
                    methods.put(method, MethodType.REMOVE);
                }
            }
        }
        return methods;
    }

    private static enum MethodType {
        CREATE, REMOVE, BUSINESS
    }

    public int getContainerType() {
        return Container.STATEFUL;
    }

    public Object getContainerID() {
        return containerID;
    }

    public StatefulInstanceManager getInstanceManager() {
        return instanceManager;
    }

    public synchronized DeploymentInfo[] deployments() {
        return deploymentsById.values().toArray(new DeploymentInfo[deploymentsById.size()]);
    }

    public synchronized DeploymentInfo getDeploymentInfo(Object deploymentID) {
        return deploymentsById.get(deploymentID);
    }

    public void deploy(Object deploymentId, DeploymentInfo deploymentInfo) throws OpenEJBException {
        deploy(deploymentId, (CoreDeploymentInfo)deploymentInfo);
    }

    private synchronized void deploy(Object deploymentId, CoreDeploymentInfo deploymentInfo) {
        Map<Method, MethodType> methods = getLifecycelMethodsOfInterface(deploymentInfo);
        deploymentInfo.setContainerData(new Data(new Index<Method,MethodType>(methods)));

        deploymentsById.put(deploymentId, deploymentInfo);
        deploymentInfo.setContainer(this);
    }

    public Object invoke(Object deployID, Method callMethod, Object [] args, Object primKey, Object securityIdentity) throws OpenEJBException {
        CoreDeploymentInfo deployInfo = (CoreDeploymentInfo) this.getDeploymentInfo(deployID);

        Data data = (Data) deployInfo.getContainerData();
        MethodType methodType = data.getMethodIndex().get(callMethod);
        methodType = (methodType != null) ? methodType : MethodType.BUSINESS;

        switch (methodType) {
            case CREATE:
                ProxyInfo proxyInfo = createEJBObject(deployInfo, callMethod, args, securityIdentity);
                return proxyInfo;
            case REMOVE:
                removeEJBObject(deployInfo, primKey, callMethod, args, securityIdentity);
                return null;
            default:
                Object value = businessMethod(deployInfo, primKey, callMethod, args, securityIdentity);
                return value;
        }
    }

    protected ProxyInfo createEJBObject(CoreDeploymentInfo deploymentInfo, Method callMethod, Object [] args, Object securityIdentity) throws OpenEJBException {
        // generate a new primary key
        Object primaryKey = newPrimaryKey();

        ThreadContext createContext = new ThreadContext(deploymentInfo, primaryKey, securityIdentity);
        createContext.setCurrentOperation(Operation.OP_CREATE);
        ThreadContext oldCallContext = ThreadContext.enter(createContext);
        try {
            checkAuthorization(deploymentInfo, callMethod, securityIdentity);

            // allocate a new instance
            Object bean = instanceManager.newInstance(primaryKey, deploymentInfo.getBeanClass());

            // Invoke postConstructs or create(...)
            if (bean instanceof SessionBean) {
                Method runMethod = deploymentInfo.getMatchingBeanMethod(callMethod);
                _invoke(callMethod, runMethod, args, bean, createContext);
            } else {
                Method postConstruct = deploymentInfo.getPostConstruct();
                if (postConstruct != null){
                    _invoke(callMethod, postConstruct, args, bean, createContext);
                }
            }


            instanceManager.poolInstance(primaryKey, bean);

            Class callingClass = callMethod.getDeclaringClass();
            Class objectInterface = deploymentInfo.getObjectInterface(callingClass);
            return new ProxyInfo(deploymentInfo, primaryKey, objectInterface, this);
        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    protected Object newPrimaryKey() {
        return new VMID();
    }

    protected void removeEJBObject(CoreDeploymentInfo deploymentInfo, Object primKey, Method callMethod, Object[] args, Object securityIdentity) throws OpenEJBException {
        ThreadContext callContext = new ThreadContext(deploymentInfo, primKey, securityIdentity);
        ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            checkAuthorization(deploymentInfo, callMethod, securityIdentity);
            try {
                Object bean = instanceManager.obtainInstance(primKey, callContext);
                if (bean != null) {
                    callContext.setCurrentOperation(Operation.OP_REMOVE);
                    Method preDestroy = callContext.getDeploymentInfo().getPreDestroy();
                    if (preDestroy != null) {
                        _invoke(callMethod, preDestroy, null, bean, callContext);
                    }
                }
            } finally {
                instanceManager.freeInstance(callContext.getPrimaryKey());
            }
        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    private Object businessMethod(CoreDeploymentInfo deploymentInfo, Object primKey, Method callMethod, Object[] args, Object securityIdentity) throws OpenEJBException {
        ThreadContext callContext = new ThreadContext(deploymentInfo, primKey, securityIdentity);
        ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            checkAuthorization(deploymentInfo, callMethod, securityIdentity);

            Object bean = instanceManager.obtainInstance(primKey, callContext);
            callContext.setCurrentOperation(Operation.OP_BUSINESS);
            Object returnValue = null;
            Method runMethod = deploymentInfo.getMatchingBeanMethod(callMethod);

            returnValue = _invoke(callMethod, runMethod, args, bean, callContext);

            instanceManager.poolInstance(primKey, bean);

            return returnValue;
        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    private void checkAuthorization(CoreDeploymentInfo deployInfo, Method callMethod, Object securityIdentity) throws ApplicationException {
        boolean authorized = securityService.isCallerAuthorized(securityIdentity, deployInfo.getAuthorizedRoles(callMethod));
        if (!authorized) {
            throw new ApplicationException(new RemoteException("Unauthorized Access by Principal Denied"));
        }
    }

    protected Object _invoke(Method callMethod, Method runMethod, Object [] args, Object bean, ThreadContext callContext) throws OpenEJBException {

        TransactionPolicy txPolicy = callContext.getDeploymentInfo().getTransactionPolicy(callMethod);
        TransactionContext txContext = new TransactionContext(callContext, transactionManager);
        try {
            txPolicy.beforeInvoke(bean, txContext);
        } catch (ApplicationException e) {
            if (e.getRootCause() instanceof TransactionRequiredException ||
                    e.getRootCause() instanceof RemoteException) {

                instanceManager.poolInstance(callContext.getPrimaryKey(), bean);
            }
            throw e;
        }

        Object returnValue = null;
        try {
            returnValue = runMethod.invoke(bean, args);
        } catch (InvocationTargetException ite) {// handle enterprise bean exception
            if (ite.getTargetException() instanceof RuntimeException) {
                /* System Exception ****************************/

                txPolicy.handleSystemException(ite.getTargetException(), bean, txContext);
            } else {
                /* Application Exception ***********************/
                instanceManager.poolInstance(callContext.getPrimaryKey(), bean);

                txPolicy.handleApplicationException(ite.getTargetException(), txContext);
            }
        } catch (Throwable re) {// handle reflection exception
            /*
              Any exception thrown by reflection; not by the enterprise bean. Possible
              Exceptions are:
                IllegalAccessException - if the underlying method is inaccessible.
                IllegalArgumentException - if the number of actual and formal parameters differ, or if an unwrapping conversion fails.
                NullPointerException - if the specified object is null and the method is an instance method.
                ExceptionInitializerError - if the initialization provoked by this method fails.
            */

            txPolicy.handleSystemException(re, bean, txContext);

        } finally {

            txPolicy.afterInvoke(bean, txContext);
        }

        return returnValue;
    }

    public void discardInstance(Object bean, ThreadContext threadContext) {
        try {
            Object primaryKey = threadContext.getPrimaryKey();
            instanceManager.freeInstance(primaryKey);
        } catch (Throwable t) {
            logger.error("", t);
        }
    }
}
