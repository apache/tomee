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

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.dgc.VMID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.RemoveException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionRequiredException;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.ContainerType;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.InvalidateReferenceException;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ExceptionType;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.interceptor.InterceptorStack;
import org.apache.openejb.core.transaction.TransactionContainer;
import org.apache.openejb.core.transaction.TransactionContext;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.persistence.EntityManagerAlreadyRegisteredException;
import org.apache.openejb.persistence.JtaEntityManagerRegistry;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Index;
import org.apache.openejb.util.Logger;

/**
 * @org.apache.xbean.XBean element="statefulContainer"
 */
public class StatefulContainer implements RpcContainer, TransactionContainer {
    private static final Logger logger = Logger.getInstance("OpenEJB", "org.apache.openejb.util.resources");

    private final Object containerID;
    private final TransactionManager transactionManager;  
    private final SecurityService securityService;
    private final StatefulInstanceManager instanceManager;
    // todo this should be part of the constructor
    private final JtaEntityManagerRegistry entityManagerRegistry = SystemInstance.get().getComponent(JtaEntityManagerRegistry.class);

    /**
     * Index used for getDeployments() and getDeploymentInfo(deploymentId).
     */
    protected final Map<Object, DeploymentInfo> deploymentsById = new HashMap<Object, DeploymentInfo>();


    public StatefulContainer(Object id, TransactionManager transactionManager, SecurityService securityService, Class passivator, int timeOut, int poolSize, int bulkPassivate) throws OpenEJBException {
        this.containerID = id;
        this.transactionManager = transactionManager;
        this.securityService = securityService;

        instanceManager = new StatefulInstanceManager(transactionManager, securityService, entityManagerRegistry, passivator, timeOut, poolSize, bulkPassivate);
    }

    private Map<Method, MethodType> getLifecycelMethodsOfInterface(CoreDeploymentInfo deploymentInfo) {
        Map<Method, MethodType> methods = new HashMap<Method, MethodType>();

        List<Method> removeMethods = deploymentInfo.getRemoveMethods();
        for (Method removeMethod : removeMethods) {
            methods.put(removeMethod, MethodType.REMOVE);

            for (Class businessLocal : deploymentInfo.getBusinessLocalInterfaces()) {
                try {
                    Method method = businessLocal.getMethod(removeMethod.getName());
                    methods.put(method, MethodType.REMOVE);
                } catch (NoSuchMethodException thatsFine) {
                }
            }

            for (Class businessRemote : deploymentInfo.getBusinessRemoteInterfaces()) {
                try {
                    Method method = businessRemote.getMethod(removeMethod.getName());
                    methods.put(method, MethodType.REMOVE);
                } catch (NoSuchMethodException thatsFine) {
                }
            }
        }

        Class legacyRemote = deploymentInfo.getRemoteInterface();
        if (legacyRemote != null) {
            try {
                Method method = legacyRemote.getMethod("remove");
                methods.put(method, MethodType.REMOVE);
            } catch (NoSuchMethodException thatsFine) {
            }
        }

        Class legacyLocal = deploymentInfo.getLocalInterface();
        if (legacyLocal != null) {
            try {
                Method method = legacyLocal.getMethod("remove");
                methods.put(method, MethodType.REMOVE);
            } catch (NoSuchMethodException thatsFine) {
            }
        }

        Class businessLocalHomeInterface = deploymentInfo.getBusinessLocalInterface();
        if (businessLocalHomeInterface != null) {
            for (Method method : DeploymentInfo.BusinessLocalHome.class.getMethods()) {
                if (method.getName().startsWith("create")) {
                    methods.put(method, MethodType.CREATE);
                } else if (method.getName().equals("remove")) {
                    methods.put(method, MethodType.REMOVE);
                }
            }
        }

        Class businessRemoteHomeInterface = deploymentInfo.getBusinessRemoteInterface();
        if (businessRemoteHomeInterface != null) {
            for (Method method : DeploymentInfo.BusinessRemoteHome.class.getMethods()) {
                if (method.getName().startsWith("create")) {
                    methods.put(method, MethodType.CREATE);
                } else if (method.getName().equals("remove")) {
                    methods.put(method, MethodType.REMOVE);
                }
            }
        }

        Class homeInterface = deploymentInfo.getHomeInterface();
        if (homeInterface != null) {
            for (Method method : homeInterface.getMethods()) {
                if (method.getName().startsWith("create")) {
                    methods.put(method, MethodType.CREATE);
                } else if (method.getName().equals("remove")) {
                    methods.put(method, MethodType.REMOVE);
                }
            }
        }

        Class localHomeInterface = deploymentInfo.getLocalHomeInterface();
        if (localHomeInterface != null) {
            for (Method method : localHomeInterface.getMethods()) {
                if (method.getName().startsWith("create")) {
                    methods.put(method, MethodType.CREATE);
                } else if (method.getName().equals("remove")) {
                    methods.put(method, MethodType.REMOVE);
                }
            }
        }
        return methods;
    }

    static enum MethodType {
        CREATE, REMOVE, BUSINESS
    }

    public ContainerType getContainerType() {
        return ContainerType.STATEFUL;
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

    public void deploy(DeploymentInfo deploymentInfo) throws OpenEJBException {
        deploy((CoreDeploymentInfo) deploymentInfo);
    }

    public void undeploy(DeploymentInfo info) throws OpenEJBException {
        undeploy((CoreDeploymentInfo) info);
    }

    private synchronized void undeploy(CoreDeploymentInfo deploymentInfo) throws OpenEJBException {
        deploymentsById.remove(deploymentInfo.getDeploymentID());
        deploymentInfo.setContainer(null);
        deploymentInfo.setContainerData(null);
        instanceManager.undeploy(deploymentInfo);
    }

    private synchronized void deploy(CoreDeploymentInfo deploymentInfo) throws OpenEJBException {
        Map<Method, MethodType> methods = getLifecycelMethodsOfInterface(deploymentInfo);

        deploymentsById.put(deploymentInfo.getDeploymentID(), deploymentInfo);
        deploymentInfo.setContainer(this);
        instanceManager.deploy(deploymentInfo, new Index<Method, MethodType>(methods));
    }

    /**
     * @deprecated use invoke signature without 'securityIdentity' argument.
     */
    public Object invoke(Object deployID, Method callMethod, Object[] args, Object primKey, Object securityIdentity) throws OpenEJBException {
        return invoke(deployID, callMethod.getDeclaringClass(), callMethod, args, primKey);
    }

    public Object invoke(Object deployID, Class callInterface, Method callMethod, Object [] args, Object primKey) throws OpenEJBException {
        CoreDeploymentInfo deployInfo = (CoreDeploymentInfo) this.getDeploymentInfo(deployID);
        if (deployInfo == null)
            throw new OpenEJBException("Deployment does not exist in this container. Deployment(id='" + deployID + "'), Container(id='" + containerID + "')");

        MethodType methodType = instanceManager.getMethodIndex(deployInfo).get(callMethod);
        methodType = (methodType != null) ? methodType : MethodType.BUSINESS;

        switch (methodType) {
            case CREATE:
                ProxyInfo proxyInfo = createEJBObject(deployInfo, callInterface, callMethod, args);
                return proxyInfo;
            case REMOVE:
                Object o = removeEJBObject(deployInfo, primKey, callInterface, callMethod, args);
                return o;
            default:
                Object value = businessMethod(deployInfo, primKey, callInterface, callMethod, args);
                return value;
        }
    }

    protected ProxyInfo createEJBObject(CoreDeploymentInfo deploymentInfo, Class callInterface, Method callMethod, Object [] args) throws OpenEJBException {
        // generate a new primary key
        Object primaryKey = newPrimaryKey();


        ThreadContext createContext = new ThreadContext(deploymentInfo, primaryKey);
        createContext.setCurrentOperation(Operation.CREATE);
        createContext.setCurrentAllowedStates(StatefulContext.getStates());
        ThreadContext oldCallContext = ThreadContext.enter(createContext);

        try {
            checkAuthorization(deploymentInfo, callMethod, callInterface);

            // create the extended entity managers
            Index<EntityManagerFactory, EntityManager> entityManagers = createEntityManagers(deploymentInfo);
            // register them
            if (entityManagers != null) {
                try {
                    entityManagerRegistry.addEntityManagers((String) deploymentInfo.getDeploymentID(), primaryKey, entityManagers);
                } catch (EntityManagerAlreadyRegisteredException e) {
                    throw new EJBException(e);
                }
            }

            // allocate a new instance
            Object o = instanceManager.newInstance(primaryKey, deploymentInfo.getBeanClass());
            StatefulInstanceManager.Instance instance = (StatefulInstanceManager.Instance) o;

            instanceManager.setEntityManagers(createContext, entityManagers);

            if (!callMethod.getDeclaringClass().equals(DeploymentInfo.BusinessLocalHome.class) && !callMethod.getDeclaringClass().equals(DeploymentInfo.BusinessRemoteHome.class)){

                Method createOrInit = deploymentInfo.getMatchingBeanMethod(callMethod);

                InterceptorStack interceptorStack = new InterceptorStack(instance.bean, createOrInit, Operation.CREATE, new ArrayList<InterceptorData>(), new HashMap<String,Object>());

                _invoke(callMethod, interceptorStack, args, instance, createContext);
            }

            instanceManager.poolInstance(createContext, instance);

            return new ProxyInfo(deploymentInfo, primaryKey);
        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    protected Object newPrimaryKey() {
        return new VMID();
    }

    protected Object removeEJBObject(CoreDeploymentInfo deploymentInfo, Object primKey, Class callInterface, Method callMethod, Object[] args) throws OpenEJBException {
        ThreadContext callContext = new ThreadContext(deploymentInfo, primKey);
        ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            checkAuthorization(deploymentInfo, callMethod, callInterface);

            if (instanceManager.getBeanTransaction(callContext) != null) {
                throw new ApplicationException(new RemoveException("A stateful EJB enrolled in a transaction can not be removed"));
            }

            Method runMethod = deploymentInfo.getMatchingBeanMethod(callMethod);
            StatefulInstanceManager.Instance instance = (StatefulInstanceManager.Instance) instanceManager.obtainInstance(primKey, callContext);

            if (instance == null) throw new ApplicationException(new javax.ejb.NoSuchEJBException());

            boolean retain = false;
            try {
                callContext.setCurrentAllowedStates(StatefulContext.getStates());
                callContext.setCurrentOperation(Operation.REMOVE);
                callContext.setInvokedInterface(callInterface);

                Class<?> declaringClass = callMethod.getDeclaringClass();
                if (declaringClass.equals(EJBHome.class) || declaringClass.equals(EJBLocalHome.class)){
                    args = new Object[]{}; // no args to pass on home.remove(remote) calls
                }
                
                List<InterceptorData> interceptors = deploymentInfo.getMethodInterceptors(runMethod);
                InterceptorStack interceptorStack = new InterceptorStack(instance.bean, runMethod, Operation.REMOVE, interceptors, instance.interceptors);
                return _invoke(callMethod, interceptorStack, args, instance, callContext);

            } catch(InvalidateReferenceException e){
                throw e;
            } catch(ApplicationException e){
                InterfaceType type = deploymentInfo.getInterfaceType(callInterface);
                if (type.isBusiness()){
                    retain = deploymentInfo.retainIfExeption(runMethod);
                    throw e;
                } else {
                    return null;
                }
            } finally {
                if (retain){
                    instanceManager.poolInstance(callContext, instance);
                } else {
                    callContext.setCurrentOperation(Operation.PRE_DESTROY);

                    try {
                        List<InterceptorData> callbackInterceptors = deploymentInfo.getCallbackInterceptors();
                        InterceptorStack interceptorStack = new InterceptorStack(instance.bean, null, Operation.PRE_DESTROY, callbackInterceptors, instance.interceptors);
                        interceptorStack.invoke();
                    } catch (Throwable callbackException) {
                        String logMessage = "An unexpected exception occured while invoking the preDestroy method on the removed Stateful SessionBean instance; " + callbackException.getClass().getName() + " " + callbackException.getMessage();

                        /* [1] Log the exception or error */
                        logger.error(logMessage);

                    } finally {
                        callContext.setCurrentOperation(Operation.REMOVE);
                    }

                    // todo destroy extended persistence contexts
                    instanceManager.freeInstance(callContext);
                }
            }
        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    private Object businessMethod(CoreDeploymentInfo deploymentInfo, Object primKey, Class callInterface, Method callMethod, Object[] args) throws OpenEJBException {
        ThreadContext callContext = new ThreadContext(deploymentInfo, primKey);
        ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            checkAuthorization(deploymentInfo, callMethod, callInterface);

            Object bean = instanceManager.obtainInstance(primKey, callContext);
            callContext.setCurrentOperation(Operation.BUSINESS);
            callContext.setCurrentAllowedStates(StatefulContext.getStates());
            callContext.setInvokedInterface(callInterface);
            Method runMethod = deploymentInfo.getMatchingBeanMethod(callMethod);

            callContext.set(Method.class, runMethod);

            StatefulInstanceManager.Instance instance = (StatefulInstanceManager.Instance) bean;

            List<InterceptorData> interceptors = deploymentInfo.getMethodInterceptors(runMethod);
            InterceptorStack interceptorStack = new InterceptorStack(instance.bean, runMethod, Operation.BUSINESS, interceptors, instance.interceptors);
            Object returnValue = _invoke(callMethod, interceptorStack, args, bean, callContext);

            instanceManager.poolInstance(callContext, bean);

            return returnValue;
        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    private void checkAuthorization(CoreDeploymentInfo deployInfo, Method callMethod, Class callInterface) throws ApplicationException {
        boolean authorized = securityService.isCallerAuthorized(callMethod, deployInfo.getInterfaceType(callInterface));
        if (!authorized) {
            throw new ApplicationException(new EJBAccessException("Unauthorized Access by Principal Denied"));
        }
    }

    protected Object _invoke(Method callMethod, InterceptorStack interceptorStack, Object [] args, Object bean, ThreadContext callContext) throws OpenEJBException {

        TransactionPolicy txPolicy = callContext.getDeploymentInfo().getTransactionPolicy(callMethod);
        TransactionContext txContext = new TransactionContext(callContext, transactionManager);
        try {
            txPolicy.beforeInvoke(bean, txContext);
        } catch (ApplicationException e) {
            if (e.getRootCause() instanceof TransactionRequiredException ||
                    e.getRootCause() instanceof RemoteException) {

                instanceManager.poolInstance(callContext, bean);
            }
            throw e;
        }

        Object returnValue = null;
        try {
            registerEntityManagers(callContext);
            if (args == null){
                returnValue = interceptorStack.invoke();
            } else {
                returnValue = interceptorStack.invoke(args);
            }
        } catch (Throwable re) {// handle reflection exception
            ExceptionType type = callContext.getDeploymentInfo().getExceptionType(re);
            if (type == ExceptionType.SYSTEM) {
                /* System Exception ****************************/

                txPolicy.handleSystemException(re, bean, txContext);
            } else {
                /* Application Exception ***********************/
                instanceManager.poolInstance(callContext, bean);

                txPolicy.handleApplicationException(re, type == ExceptionType.APPLICATION_ROLLBACK, txContext);
            }
        } finally {
            unregisterEntityManagers(callContext);
            txPolicy.afterInvoke(bean, txContext);
        }

        return returnValue;
    }

    private Index<EntityManagerFactory, EntityManager> createEntityManagers(CoreDeploymentInfo deploymentInfo) {
        // create the extended entity managers
        Index<EntityManagerFactory, Map> factories = deploymentInfo.getExtendedEntityManagerFactories();
        Index<EntityManagerFactory, EntityManager> entityManagers = null;
        if (factories != null && factories.size() > 0) {
            entityManagers = new Index<EntityManagerFactory, EntityManager>(new ArrayList<EntityManagerFactory>(factories.keySet()));
            for (Map.Entry<EntityManagerFactory, Map> entry : factories.entrySet()) {
                EntityManagerFactory entityManagerFactory = entry.getKey();
                Map properties = entry.getValue();


                EntityManager entityManager = entityManagerRegistry.getInheritedEntityManager(entityManagerFactory);
                if (entityManager == null) {
                    if (properties != null) {
                        entityManager = entityManagerFactory.createEntityManager(properties);
                    } else {
                        entityManager = entityManagerFactory.createEntityManager();
                    }
                }
                entityManagers.put(entityManagerFactory, entityManager);
            }
        }
        return entityManagers;
    }

    private void registerEntityManagers(ThreadContext callContext) throws OpenEJBException {
        if (entityManagerRegistry == null) return;

        CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();

        // get the factories
        Index<EntityManagerFactory, Map> factories = deploymentInfo.getExtendedEntityManagerFactories();
        if (factories == null) return;

        // get the managers for the factories
        Object primaryKey = callContext.getPrimaryKey();
        Map<EntityManagerFactory, EntityManager> entityManagers = instanceManager.getEntityManagers(callContext, factories);
        if (entityManagers == null) return;

        // register them
        try {
            entityManagerRegistry.addEntityManagers((String) deploymentInfo.getDeploymentID(), primaryKey, entityManagers);
        } catch (EntityManagerAlreadyRegisteredException e) {
            throw new EJBException(e);
        }
    }

    private void unregisterEntityManagers(ThreadContext callContext) {
        if (entityManagerRegistry == null) return;

        CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();

        // get the managers for the factories
        Object primaryKey = callContext.getPrimaryKey();

        // register them
        entityManagerRegistry.removeEntityManagers((String) deploymentInfo.getDeploymentID(), primaryKey);
    }

    public void discardInstance(Object bean, ThreadContext threadContext) {
        try {
            instanceManager.freeInstance(threadContext);
        } catch (Throwable t) {
            logger.error("", t);
        }
    }
}
