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

import org.apache.openejb.ApplicationException;
import org.apache.openejb.ContainerType;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ExceptionType;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.stateful.StatefulInstanceManager.Instance;
import static org.apache.openejb.core.ExceptionType.SYSTEM;
import static org.apache.openejb.core.ExceptionType.APPLICATION_ROLLBACK;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.interceptor.InterceptorStack;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.handleApplicationException;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.handleSystemException;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.createTransactionPolicy;
import org.apache.openejb.core.transaction.BeanTransactionPolicy.SuspendedTransaction;
import org.apache.openejb.core.transaction.BeanTransactionPolicy;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.transaction.EjbTransactionUtil;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.persistence.EntityManagerAlreadyRegisteredException;
import org.apache.openejb.persistence.JtaEntityManagerRegistry;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Index;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

/**
 * @org.apache.xbean.XBean element="statefulContainer"
 */
public class StatefulContainer implements RpcContainer {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");

    private final Object containerID;
    private final SecurityService securityService;
    protected final StatefulInstanceManager instanceManager;
    // todo this should be part of the constructor
    protected final JtaEntityManagerRegistry entityManagerRegistry = SystemInstance.get().getComponent(JtaEntityManagerRegistry.class);

    /**
     * Index used for getDeployments() and getDeploymentInfo(deploymentId).
     */
    protected final Map<Object, DeploymentInfo> deploymentsById = new HashMap<Object, DeploymentInfo>();


    public StatefulContainer(Object id,
            SecurityService securityService,
            Class passivator,
            int timeOut,
            int poolSize,
            int bulkPassivate) throws OpenEJBException {
        this.containerID = id;
        this.securityService = securityService;

        instanceManager = newStatefulInstanceManager(
                securityService,
            passivator,
            timeOut,
            poolSize,
            bulkPassivate);
    }

    protected StatefulInstanceManager newStatefulInstanceManager(
            SecurityService securityService,
            Class passivator,
            int timeOut,
            int poolSize,
            int bulkPassivate) throws OpenEJBException {
        return new StatefulInstanceManager(
                securityService,
            entityManagerRegistry,
            passivator,
            timeOut,
            poolSize,
            bulkPassivate);
    }

    private Map<Method, MethodType> getLifecycleMethodsOfInterface(CoreDeploymentInfo deploymentInfo) {
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

    public static enum MethodType {
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
        Map<Method, MethodType> methods = getLifecycleMethodsOfInterface(deploymentInfo);

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
                return createEJBObject(deployInfo, callInterface, callMethod, args);
            case REMOVE:
                return removeEJBObject(deployInfo, primKey, callInterface, callMethod, args);
            default:
                return businessMethod(deployInfo, primKey, callInterface, callMethod, args);
        }
    }

    protected ProxyInfo createEJBObject(CoreDeploymentInfo deploymentInfo, Class callInterface, Method callMethod, Object [] args) throws OpenEJBException {
        // generate a new primary key
        Object primaryKey = newPrimaryKey();


        ThreadContext createContext = new ThreadContext(deploymentInfo, primaryKey);
        ThreadContext oldCallContext = ThreadContext.enter(createContext);
        try {
            // Security check
            checkAuthorization(deploymentInfo, callMethod, callInterface);

            // Create the extended entity managers for this instance
            Index<EntityManagerFactory, EntityManager> entityManagers = createEntityManagers(deploymentInfo);

            // Register the newly created entity managers
            if (entityManagers != null) {
                try {
                    entityManagerRegistry.addEntityManagers((String) deploymentInfo.getDeploymentID(), primaryKey, entityManagers);
                } catch (EntityManagerAlreadyRegisteredException e) {
                    throw new EJBException(e);
                }
            }

            // Start transaction
            TransactionPolicy txPolicy = createTransactionPolicy(createContext.getDeploymentInfo().getTransactionType(callMethod), createContext);

            Instance instance = null;
            try {
                // Create new instance
                instance = (Instance) instanceManager.newInstance(primaryKey, deploymentInfo.getBeanClass());

                // Register the entity managers with the instance
                instanceManager.setEntityManagers(createContext, entityManagers);
                registerEntityManagers(createContext);

                // Register for synchronization callbacks
                SessionSynchronizationCoordinator.registerSessionSynchronization(instance, createContext);
              
                // Invoke create for legacy beans
                if (!callMethod.getDeclaringClass().equals(DeploymentInfo.BusinessLocalHome.class) &&
                        !callMethod.getDeclaringClass().equals(DeploymentInfo.BusinessRemoteHome.class)){

                    // Setup for business invocation
                    createContext.setCurrentOperation(Operation.CREATE);
                    createContext.setCurrentAllowedStates(StatefulContext.getStates());
                    Method createOrInit = deploymentInfo.getMatchingBeanMethod(callMethod);
                    createContext.set(Method.class, createOrInit);

                    // Initialize interceptor stack
                    InterceptorStack interceptorStack = new InterceptorStack(instance.bean, createOrInit, Operation.CREATE, new ArrayList<InterceptorData>(), new HashMap<String, Object>());

                    // Invoke
                    if (args == null){
                        interceptorStack.invoke();
                    } else {
                        interceptorStack.invoke(args);
                    }
                }
            } catch (Throwable e) {
                handleException(createContext, txPolicy, e);
            } finally {
                afterInvoke(createContext, txPolicy, instance);
            }

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
            // Security check
            checkAuthorization(deploymentInfo, callMethod, callInterface);

            // If a bean managed transaction is active, the bean can not be removed
            InterfaceType interfaceType = deploymentInfo.getInterfaceType(callInterface);
            if (interfaceType.isComponent() && instanceManager.getBeanTransaction(callContext) != null) {
                throw new ApplicationException(new RemoveException("A stateful EJB enrolled in a transaction can not be removed"));
            }

            // Start transaction
            TransactionPolicy txPolicy = createTransactionPolicy(callContext.getDeploymentInfo().getTransactionType(callMethod), callContext);

            Object returnValue = null;
            boolean retain = false;
            Instance instance = null;
            Method runMethod = null;
            try {
                // Obtain instance
                instance = (Instance) instanceManager.obtainInstance(primKey, callContext);
                if (instance == null) throw new ApplicationException(new javax.ejb.NoSuchEJBException());

                // Resume previous Bean transaction if there was one
                if (txPolicy instanceof BeanTransactionPolicy){
                    // Resume previous Bean transaction if there was one
                    SuspendedTransaction suspendedTransaction = instanceManager.getBeanTransaction(callContext);
                    if (suspendedTransaction != null) {
                        BeanTransactionPolicy beanTxEnv = (BeanTransactionPolicy) txPolicy;
                        beanTxEnv.resumeUserTransaction(suspendedTransaction);
                    }
                }

                // Register the entity managers
                registerEntityManagers(callContext);

                // Register for synchronization callbacks
                SessionSynchronizationCoordinator.registerSessionSynchronization(instance, callContext);

                // Setup for remove invocation
                callContext.setCurrentOperation(Operation.REMOVE);
                callContext.setCurrentAllowedStates(StatefulContext.getStates());
                callContext.setInvokedInterface(callInterface);
                runMethod = deploymentInfo.getMatchingBeanMethod(callMethod);
                callContext.set(Method.class, runMethod);

                // Do not pass arguments on home.remove(remote) calls
                Class<?> declaringClass = callMethod.getDeclaringClass();
                if (declaringClass.equals(EJBHome.class) || declaringClass.equals(EJBLocalHome.class)){
                    args = new Object[]{};
                }
                
                // Initialize interceptor stack
                List<InterceptorData> interceptors = deploymentInfo.getMethodInterceptors(runMethod);
                InterceptorStack interceptorStack = new InterceptorStack(instance.bean, runMethod, Operation.REMOVE, interceptors, instance.interceptors);

                // Invoke
                if (args == null){
                    returnValue = interceptorStack.invoke();
                } else {
                    returnValue = interceptorStack.invoke(args);
                }
            } catch (Throwable e) {
                if (interfaceType.isBusiness() && deploymentInfo.getExceptionType(e) == SYSTEM) {
                    retain = deploymentInfo.retainIfExeption(runMethod);
                }
                handleException(callContext, txPolicy, e);
            } finally {
                if (!retain) {
                    try {
                        callContext.setCurrentOperation(Operation.PRE_DESTROY);
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

                // Commit transaction
                afterInvoke(callContext, txPolicy, instance);
            }

            return returnValue;
        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    protected Object businessMethod(CoreDeploymentInfo deploymentInfo, Object primKey, Class callInterface, Method callMethod, Object[] args) throws OpenEJBException {
        ThreadContext callContext = new ThreadContext(deploymentInfo, primKey);
        ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            // Security check
            checkAuthorization(deploymentInfo, callMethod, callInterface);

            // Start transaction
            TransactionPolicy txPolicy = createTransactionPolicy(callContext.getDeploymentInfo().getTransactionType(callMethod), callContext);

            Object returnValue = null;
            Instance instance = null;
            try {
                // Obtain instance
                instance = (Instance) instanceManager.obtainInstance(primKey, callContext);

                // Resume previous Bean transaction if there was one
                if (txPolicy instanceof BeanTransactionPolicy){
                    SuspendedTransaction suspendedTransaction = instanceManager.getBeanTransaction(callContext);
                    if (suspendedTransaction != null) {
                        BeanTransactionPolicy beanTxEnv = (BeanTransactionPolicy) txPolicy;
                        beanTxEnv.resumeUserTransaction(suspendedTransaction);
                    }
                }

                // Register the entity managers
                registerEntityManagers(callContext);

                // Register for synchronization callbacks
                SessionSynchronizationCoordinator.registerSessionSynchronization(instance, callContext);

                // Setup for business invocation
                callContext.setCurrentOperation(Operation.BUSINESS);
                callContext.setCurrentAllowedStates(StatefulContext.getStates());
                callContext.setInvokedInterface(callInterface);
                Method runMethod = deploymentInfo.getMatchingBeanMethod(callMethod);
                callContext.set(Method.class, runMethod);

                // Initialize interceptor stack
                List<InterceptorData> interceptors = deploymentInfo.getMethodInterceptors(runMethod);
                InterceptorStack interceptorStack = new InterceptorStack(instance.bean, runMethod, Operation.BUSINESS, interceptors, instance.interceptors);

                // Invoke
                returnValue = interceptorStack.invoke(args);
            } catch (Throwable e) {
                handleException(callContext, txPolicy, e);
            } finally {
                // Commit transaction
                afterInvoke(callContext, txPolicy, instance);
            }
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

    private void handleException(ThreadContext callContext, TransactionPolicy txPolicy, Throwable e) throws ApplicationException {
        if (e instanceof ApplicationException) {
            throw (ApplicationException) e;
        }

        ExceptionType type = callContext.getDeploymentInfo().getExceptionType(e);
        if (type == SYSTEM) {
            instanceManager.freeInstance(callContext);
            handleSystemException(txPolicy, e, callContext);
        } else {
            handleApplicationException(txPolicy, e, type == APPLICATION_ROLLBACK);
        }
    }

    private void afterInvoke(ThreadContext callContext, TransactionPolicy txPolicy, Instance instance) throws OpenEJBException {
        try {
            unregisterEntityManagers(callContext);

            if (instance != null && txPolicy instanceof BeanTransactionPolicy) {
                // suspend the currently running transaction if any
                SuspendedTransaction suspendedTransaction = null;
                try {
                    BeanTransactionPolicy beanTxEnv = (BeanTransactionPolicy) txPolicy;
                    suspendedTransaction = beanTxEnv.suspendUserTransaction();
                } catch (SystemException e) {
                    handleSystemException(txPolicy, e, callContext);
                } finally {
                    instanceManager.setBeanTransaction(callContext, suspendedTransaction);
                }
            }
        } finally {
            instanceManager.checkInInstance(callContext);
            EjbTransactionUtil.afterInvoke(txPolicy, callContext);
        }
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
}
