/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.cmp;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.EntityBean;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.RemoveException;
import javax.ejb.Timer;
import javax.ejb.FinderException;
import javax.ejb.EJBAccessException;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.Synchronization;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.ContainerType;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ExceptionType;
import org.apache.openejb.core.timer.EjbTimerService;
import org.apache.openejb.core.timer.EjbTimerServiceImpl;
import org.apache.openejb.core.entity.EntityContext;
import org.apache.openejb.core.entity.EntrancyTracker;
import org.apache.openejb.core.transaction.TransactionPolicy;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.handleApplicationException;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.handleSystemException;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.afterInvoke;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.createTransactionPolicy;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Enumerator;

/**
 * @org.apache.xbean.XBean element="cmpContainer"
 */
public class CmpContainer implements RpcContainer {
    protected final Object containerID;
    protected final SecurityService securityService;

    /**
     * Index used for getDeployments() and getDeploymentInfo(deploymentId).
     */
    protected final Map<Object, DeploymentInfo> deploymentsById = new HashMap<Object, DeploymentInfo>();

    /**
     * When events are fired from the CMP engine only an entity bean instance is returned.  The type of the bean is used
     * to find the deployment info.  This means that when the same type is used multiple ejb deployments a random deployment
     * will be selected to handle the ejb callback.
     */
    protected final Map<Class, DeploymentInfo> deploymentsByClass = new HashMap<Class, DeploymentInfo>();

    /**
     * The CmpEngine which performs the actual persistence operations
     */
    protected final CmpEngine cmpEngine;

    /**
     * Tracks entity instances that have been "entered" so we can throw reentrancy exceptions.
     */
    protected EntrancyTracker entrancyTracker;
    protected TransactionSynchronizationRegistry synchronizationRegistry;
    private static final Object ENTITIES_TO_STORE = new Object() {
        public String toString() {
            return "EntitiesToStore";
        }
    };

    public CmpContainer(Object id, TransactionManager transactionManager, SecurityService securityService, String cmpEngineFactory) throws OpenEJBException {
        this.containerID = id;
        this.securityService = securityService;
        synchronizationRegistry = SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class);
        entrancyTracker = new EntrancyTracker(synchronizationRegistry);

        // create the cmp engine instance
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) classLoader = getClass().getClassLoader();

        CmpEngineFactory factory;
        try {
            Class<?> cmpEngineFactoryClass = classLoader.loadClass(cmpEngineFactory);
            factory = (CmpEngineFactory) cmpEngineFactoryClass.newInstance();
        } catch (Exception e) {
            throw new OpenEJBException("Unable to create cmp engine factory " + cmpEngineFactory, e);
        }
        factory.setTransactionManager(transactionManager);
        factory.setTransactionSynchronizationRegistry(synchronizationRegistry);
        factory.setCmpCallback(new ContainerCmpCallback());
        cmpEngine = factory.create();
    }

    public Object getContainerID() {
        return containerID;
    }

    public ContainerType getContainerType() {
        return ContainerType.CMP_ENTITY;
    }

    public synchronized DeploymentInfo[] deployments() {
        return deploymentsById.values().toArray(new DeploymentInfo[deploymentsById.size()]);
    }

    public synchronized DeploymentInfo getDeploymentInfo(Object deploymentID) {
        return deploymentsById.get(deploymentID);
    }

    private DeploymentInfo getDeploymentInfoByClass(Class type) {
        DeploymentInfo deploymentInfo = null;
        while (type != null && deploymentInfo == null) {
            deploymentInfo = deploymentsByClass.get(type);
            type = type.getSuperclass();
        }

        return deploymentInfo;
    }

    public void deploy(DeploymentInfo deploymentInfo) throws OpenEJBException {
        deploy((CoreDeploymentInfo) deploymentInfo);
    }

    public void deploy(CoreDeploymentInfo deploymentInfo) throws OpenEJBException {
        synchronized (this) {
            Object deploymentId = deploymentInfo.getDeploymentID();

            cmpEngine.deploy(deploymentInfo);
            deploymentInfo.setContainerData(cmpEngine);

            // try to set deploymentInfo static field on bean implementation class
            try {
                Field field = deploymentInfo.getCmpImplClass().getField("deploymentInfo");
                field.set(null, deploymentInfo);
            } catch (Exception e) {
                // ignore
            }

            // add to indexes
            deploymentsById.put(deploymentId, deploymentInfo);
            deploymentsByClass.put(deploymentInfo.getCmpImplClass(), deploymentInfo);
            deploymentInfo.setContainer(this);
        }

        EjbTimerService timerService = deploymentInfo.getEjbTimerService();
        if (timerService != null) {
            timerService.start();
        }
    }

    public void undeploy(DeploymentInfo deploymentInfo) throws OpenEJBException {
        EjbTimerService timerService = deploymentInfo.getEjbTimerService();
        if (timerService != null) {
            timerService.stop();
        }
        undeploy((CoreDeploymentInfo)deploymentInfo);
    }

    public void undeploy(CoreDeploymentInfo deploymentInfo) throws OpenEJBException {
        synchronized (this) {
            deploymentsById.remove(deploymentInfo.getDeploymentID());
            deploymentsByClass.remove(deploymentInfo.getCmpImplClass());

            try {
                Field field = deploymentInfo.getCmpImplClass().getField("deploymentInfo");
                field.set(null, null);
            } catch (Exception e) {
                // ignore
            }

            deploymentInfo.setContainer(null);
            deploymentInfo.setContainerData(null);
        }
    }

    public Object getEjbInstance(DeploymentInfo deployInfo, Object primaryKey) {
        return getEjbInstance((CoreDeploymentInfo)deployInfo, primaryKey);
    }
    
    public Object getEjbInstance(CoreDeploymentInfo deployInfo, Object primaryKey) {
        ThreadContext callContext = new ThreadContext(deployInfo, primaryKey);

        ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            Object bean = cmpEngine.loadBean(callContext, primaryKey);
            return bean;
        } finally {
            ThreadContext.exit(oldCallContext);
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
        try {

            boolean authorized = securityService.isCallerAuthorized(callMethod, type);
            if (!authorized) {
                throw new ApplicationException(new EJBAccessException("Unauthorized Access by Principal Denied"));
            }

            Class declaringClass = callMethod.getDeclaringClass();
            String methodName = callMethod.getName();

            if (EJBHome.class.isAssignableFrom(declaringClass) || EJBLocalHome.class.isAssignableFrom(declaringClass)) {
                if (declaringClass != EJBHome.class && declaringClass != EJBLocalHome.class) {
                    if (methodName.startsWith("create")) {
                        return createEJBObject(callMethod, args, callContext);
                    } else if (methodName.equals("findByPrimaryKey")) {
                        return findByPrimaryKey(callMethod, args, callContext);
                    } else if (methodName.startsWith("find")) {
                        return findEJBObject(callMethod, args, callContext);
                    } else {
                        return homeMethod(callMethod, args, callContext);
                    }
                } else if (methodName.equals("remove")) {
                    removeEJBObject(callMethod, callContext);
                    return null;
                }
            } else if ((EJBObject.class == declaringClass || EJBLocalObject.class == declaringClass) && methodName.equals("remove")) {
                removeEJBObject(callMethod, callContext);
                return null;
            }

            // business method
            callContext.setCurrentOperation(Operation.BUSINESS);
            Method runMethod = deployInfo.getMatchingBeanMethod(callMethod);

            callContext.set(Method.class, runMethod);

            Object retValue = businessMethod(callMethod, runMethod, args, callContext);

            return retValue;
        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    private EntityBean createNewInstance(ThreadContext callContext) {
        CoreDeploymentInfo deploymentInfo = (CoreDeploymentInfo) callContext.getDeploymentInfo();
        try {
            EntityBean bean = (EntityBean) deploymentInfo.getCmpImplClass().newInstance();
            return bean;
        } catch (Exception e) {
            throw new EJBException("Unable to create new entity bean instance " + deploymentInfo.getCmpImplClass(), e);
        }
    }

    private ThreadContext createThreadContext(EntityBean entityBean) {
        if (entityBean == null) throw new NullPointerException("entityBean is null");

        CoreDeploymentInfo deployInfo = (CoreDeploymentInfo) getDeploymentInfoByClass(entityBean.getClass());
        KeyGenerator keyGenerator = deployInfo.getKeyGenerator();
        Object primaryKey = keyGenerator.getPrimaryKey(entityBean);

        ThreadContext callContext = new ThreadContext(deployInfo, primaryKey);
        return callContext;
    }

    private void setEntityContext(EntityBean entityBean) {
        if (entityBean == null) throw new NullPointerException("entityBean is null");

        // activating entity doen't have a primary key
        CoreDeploymentInfo deployInfo = (CoreDeploymentInfo) getDeploymentInfoByClass(entityBean.getClass());

        ThreadContext callContext = new ThreadContext(deployInfo, null);
        callContext.setCurrentOperation(Operation.SET_CONTEXT);

        ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            entityBean.setEntityContext(new EntityContext(securityService));
        } catch (RemoteException e) {
            throw new EJBException(e);
        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    private void unsetEntityContext(EntityBean entityBean) {
        if (entityBean == null) throw new NullPointerException("entityBean is null");

        ThreadContext callContext = createThreadContext(entityBean);
        callContext.setCurrentOperation(Operation.UNSET_CONTEXT);

        ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            entityBean.unsetEntityContext();
        } catch (RemoteException e) {
            throw new EJBException(e);
        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    private void ejbLoad(EntityBean entityBean) {
        if (entityBean == null) throw new NullPointerException("entityBean is null");

        ThreadContext callContext = createThreadContext(entityBean);
        callContext.setCurrentOperation(Operation.LOAD);

        ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            entityBean.ejbLoad();
        } catch (RemoteException e) {
            throw new EJBException(e);
        } finally {
            ThreadContext.exit(oldCallContext);
        }

        // if we call load we must call store
        try {
            //noinspection unchecked
            Set<EntityBean> registeredEntities = (LinkedHashSet<EntityBean>) synchronizationRegistry.getResource(ENTITIES_TO_STORE);
            if (registeredEntities == null) {
                registeredEntities = new LinkedHashSet<EntityBean>();
                synchronizationRegistry.putResource(ENTITIES_TO_STORE, registeredEntities);
                synchronizationRegistry.registerInterposedSynchronization(new Synchronization() {
                    public void beforeCompletion() {
                        //noinspection unchecked
                        Set<EntityBean> registeredEntities = (LinkedHashSet<EntityBean>) synchronizationRegistry.getResource(ENTITIES_TO_STORE);
                        if (registeredEntities == null) {
                            return;
                        }
                        for (EntityBean entityBean : registeredEntities) {
                            ejbStore(entityBean);
                        }
                    }
                    public void afterCompletion(int i) {
                    }
                });
            }
            registeredEntities.add(entityBean);
        } catch (Exception e) {
        }
    }

    private void ejbStore(EntityBean entityBean) {
        if (entityBean == null) throw new NullPointerException("entityBean is null");

        ThreadContext callContext = createThreadContext(entityBean);
        callContext.setCurrentOperation(Operation.STORE);

        ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            entityBean.ejbStore();
        } catch (RemoteException e) {
            throw new EJBException(e);
        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    private void ejbRemove(EntityBean entityBean) throws RemoveException {
        if (entityBean == null) throw new NullPointerException("entityBean is null");
        if (isDeleted(entityBean)) return;

        ThreadContext callContext = createThreadContext(entityBean);
        callContext.setCurrentOperation(Operation.REMOVE);

        ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            entityBean.ejbRemove();
        } catch (RemoteException e) {
            throw new EJBException(e);
        } finally {
            // clear relationships
            // todo replace with interface call when CmpEntityBean interface is added
            try {
                entityBean.getClass().getMethod("OpenEJB_deleted").invoke(entityBean);
            } catch (Exception ignored) {
            }
            cancelTimers(callContext);
            ThreadContext.exit(oldCallContext);
        }
    }

    private boolean isDeleted(EntityBean entityBean) {
        try {
            return (Boolean)entityBean.getClass().getMethod("OpenEJB_isDeleted").invoke(entityBean);
        } catch (NoSuchMethodException e) {
            return false;
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    private void ejbActivate(EntityBean entityBean) {
        if (entityBean == null) throw new NullPointerException("entityBean is null");

        ThreadContext callContext = createThreadContext(entityBean);
        callContext.setCurrentOperation(Operation.ACTIVATE);

        ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            entityBean.ejbActivate();
        } catch (RemoteException e) {
            throw new EJBException(e);
        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    private void ejbPassivate(EntityBean entityBean) {
        if (entityBean == null) throw new NullPointerException("entityBean is null");

        ThreadContext callContext = createThreadContext(entityBean);
        callContext.setCurrentOperation(Operation.PASSIVATE);

        ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            entityBean.ejbPassivate();
        } catch (RemoteException e) {
            throw new EJBException(e);
        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    private Object businessMethod(Method callMethod, Method runMethod, Object[] args, ThreadContext callContext) throws OpenEJBException {
        DeploymentInfo deploymentInfo = callContext.getDeploymentInfo();

        TransactionPolicy txPolicy = createTransactionPolicy(deploymentInfo.getTransactionType(callMethod), callContext);

        EntityBean bean;
        Object returnValue = null;

        entrancyTracker.enter(deploymentInfo, callContext.getPrimaryKey());
        try {
            bean = (EntityBean) cmpEngine.loadBean(callContext, callContext.getPrimaryKey());
            if (bean == null) {
                throw new NoSuchObjectException(deploymentInfo.getDeploymentID() + " : " + callContext.getPrimaryKey());
            }

            returnValue = runMethod.invoke(bean, args);

            // when there is not transaction, merge the data from the bean back into the cmp engine
            cmpEngine.storeBeanIfNoTx(callContext, bean);
        } catch (NoSuchObjectException e) {
            handleApplicationException(txPolicy, e, false);
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getTargetException();
            }

            ExceptionType type = callContext.getDeploymentInfo().getExceptionType(e);
            if (type == ExceptionType.SYSTEM) {
                /* System Exception ****************************/
                handleSystemException(txPolicy, e, callContext);
            } else {
                /* Application Exception ***********************/
                handleApplicationException(txPolicy, e, type == ExceptionType.APPLICATION_ROLLBACK);
            }
        } finally {
            entrancyTracker.exit(deploymentInfo, callContext.getPrimaryKey());
            afterInvoke(txPolicy, callContext);
        }

        return returnValue;
    }

    private Object homeMethod(Method callMethod, Object[] args, ThreadContext callContext) throws OpenEJBException {
        DeploymentInfo deploymentInfo = callContext.getDeploymentInfo();

        TransactionPolicy txPolicy = createTransactionPolicy(deploymentInfo.getTransactionType(callMethod), callContext);

        EntityBean bean;
        Object returnValue = null;
        try {
            /*
              Obtain a bean instance from the method ready pool
            */
            bean = createNewInstance(callContext);

            // set the entity context
            setEntityContext(bean);

            try {
                callContext.setCurrentOperation(Operation.HOME);

                Method runMethod = ((CoreDeploymentInfo)deploymentInfo).getMatchingBeanMethod(callMethod);

                try {
                    returnValue = runMethod.invoke(bean, args);
                } catch (IllegalArgumentException e) {
                    System.out.println("********************************************************");
                    System.out.println("callMethod = " + callMethod);
                    System.out.println("runMethod = " + runMethod);
                    System.out.println("bean = " + bean.getClass().getName());

                    throw e;
                }
            } finally {
                unsetEntityContext(bean);
            }
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getTargetException();
            }

            ExceptionType type = callContext.getDeploymentInfo().getExceptionType(e);
            if (type == ExceptionType.SYSTEM) {
                /* System Exception ****************************/
                handleSystemException(txPolicy, e, callContext);

            } else {
                /* Application Exception ***********************/
                handleApplicationException(txPolicy, e, type == ExceptionType.APPLICATION_ROLLBACK);
            }
        } finally {
            afterInvoke(txPolicy, callContext);
        }

        return returnValue;
    }

    private ProxyInfo createEJBObject(Method callMethod, Object[] args, ThreadContext callContext) throws OpenEJBException {
        CoreDeploymentInfo deploymentInfo = (CoreDeploymentInfo) callContext.getDeploymentInfo();

        TransactionPolicy txPolicy = createTransactionPolicy(deploymentInfo.getTransactionType(callMethod), callContext);

        EntityBean bean;
        Object primaryKey = null;

        try {
            // Obtain a bean instance from the method ready pool
            bean = createNewInstance(callContext);

            // set the entity context
            setEntityContext(bean);

            // Obtain the proper ejbCreate() method
            Method ejbCreateMethod = deploymentInfo.getMatchingBeanMethod(callMethod);

            // Set current operation for allowed operations
            callContext.setCurrentOperation(Operation.CREATE);

            // Invoke the proper ejbCreate() method on the instance
            ejbCreateMethod.invoke(bean, args);

            // create the new bean
            primaryKey = cmpEngine.createBean(bean, callContext);

            // determine post create callback method
            Method ejbPostCreateMethod = deploymentInfo.getMatchingPostCreateMethod(ejbCreateMethod);

            // create a new context containing the pk for the post create call
            ThreadContext postCreateContext = new ThreadContext(deploymentInfo, primaryKey);
            postCreateContext.setCurrentOperation(Operation.POST_CREATE);

            ThreadContext oldContext = ThreadContext.enter(postCreateContext);
            try {
                // Invoke the ejbPostCreate method on the bean instance
                ejbPostCreateMethod.invoke(bean, args);

                // According to section 9.1.5.1 of the EJB 1.1 specification, the "ejbPostCreate(...)
                // method executes in the same transaction context as the previous ejbCreate(...) method."
                //
                // The bean is first insterted using db.create( ) and then after ejbPostCreate( ) its
                // updated using db.update(). This protocol allows for visablity of the bean after ejbCreate
                // within the current trasnaction.
            } finally {
                ThreadContext.exit(oldContext);
            }

            // when there is not transaction, merge the data from the bean back into the cmp engine
            cmpEngine.storeBeanIfNoTx(callContext, bean);
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getTargetException();
            }

            ExceptionType type = callContext.getDeploymentInfo().getExceptionType(e);
            if (type == ExceptionType.SYSTEM) {
                /* System Exception ****************************/
                handleSystemException(txPolicy, e, callContext);
            } else {
                /* Application Exception ***********************/
                handleApplicationException(txPolicy, e, type == ExceptionType.APPLICATION_ROLLBACK);
            }
        } finally {
            afterInvoke(txPolicy, callContext);
        }

        return new ProxyInfo(deploymentInfo, primaryKey);
    }

    private Object findByPrimaryKey(Method callMethod, Object[] args, ThreadContext callContext) throws OpenEJBException {
        DeploymentInfo deploymentInfo = callContext.getDeploymentInfo();

        TransactionPolicy txPolicy = createTransactionPolicy(deploymentInfo.getTransactionType(callMethod), callContext);

        try {
            EntityBean bean = (EntityBean) cmpEngine.loadBean(callContext, args[0]);
            if (bean == null) {
                throw new ObjectNotFoundException(deploymentInfo.getDeploymentID() + " : " + args[0]);
            }

            // rebuild the primary key
            KeyGenerator kg = ((CoreDeploymentInfo)deploymentInfo).getKeyGenerator();
            Object primaryKey = kg.getPrimaryKey(bean);

            // create a new ProxyInfo based on the deployment info and primary key
            return new ProxyInfo(deploymentInfo, primaryKey);
        } catch (javax.ejb.FinderException fe) {
            handleApplicationException(txPolicy, fe, false);
        } catch (Throwable e) {// handle reflection exception
            handleSystemException(txPolicy, e, callContext);
        } finally {
            afterInvoke(txPolicy, callContext);
        }
        throw new AssertionError("Should not get here");
    }

    private Object findEJBObject(Method callMethod, Object[] args, ThreadContext callContext) throws OpenEJBException {
        DeploymentInfo deploymentInfo = callContext.getDeploymentInfo();

        TransactionPolicy txPolicy = createTransactionPolicy(deploymentInfo.getTransactionType(callMethod), callContext);

        try {
            List<Object> results = cmpEngine.queryBeans(callContext, callMethod, args);

            KeyGenerator kg = ((CoreDeploymentInfo)deploymentInfo).getKeyGenerator();

            // The following block of code is responsible for returning ProxyInfo object(s) for each
            // matching entity bean found by the query.  If its a multi-value find operation a Vector
            // of ProxyInfo objects will be returned. If its a single-value find operation then a
            // single ProxyInfo object is returned.
            if (callMethod.getReturnType() == Collection.class || callMethod.getReturnType() == Enumeration.class) {
                List<ProxyInfo> proxies = new ArrayList<ProxyInfo>();
                for (Object value : results) {
                    EntityBean bean = (EntityBean) value;

                    if (value == null) {
                        proxies.add(null);
                    } else {
                        // get the primary key
                        Object primaryKey = kg.getPrimaryKey(bean);

                        // create a new ProxyInfo based on the deployment info and primary key and add it to the vector
                        proxies.add(new ProxyInfo(deploymentInfo, primaryKey));
                    }
                }
                if (callMethod.getReturnType() == Enumeration.class) {
                    return new Enumerator(proxies);
                } else {
                    return proxies;
                }
            } else {
                if (results.size() != 1) {
                    throw new ObjectNotFoundException("A Enteprise bean with deployment_id = " + deploymentInfo.getDeploymentID() + " and primarykey = " + args[0] + " Does not exist");
                }

                // create a new ProxyInfo based on the deployment info and primary key
                EntityBean bean = (EntityBean) results.get(0);
                if (bean == null) {
                    return null;
                } else {
                    Object primaryKey = kg.getPrimaryKey(bean);
                    return new ProxyInfo(deploymentInfo, primaryKey);
                }
            }
        } catch (javax.ejb.FinderException fe) {
            handleApplicationException(txPolicy, fe, false);
        } catch (Throwable e) {// handle reflection exception
            handleSystemException(txPolicy, e, callContext);
        } finally {
            afterInvoke(txPolicy, callContext);
        }
        throw new AssertionError("Should not get here");
    }

    public Object select(DeploymentInfo di, String methodSignature, String returnType, Object... args) throws FinderException {
        CoreDeploymentInfo deploymentInfo = (CoreDeploymentInfo) di;
        String signature = deploymentInfo.getAbstractSchemaName() + "." + methodSignature;

        try {
            // execute the select query
            Collection<Object> results = cmpEngine.queryBeans(deploymentInfo, signature, args);

            //
            // process the results
            //

            // If we need to return a set...
            Collection<Object> proxies;
            if (returnType.equals("java.util.Set")) {
                // we collect values into a LinkedHashSet to preserve ordering
                proxies = new LinkedHashSet<Object>();
            } else {
                // otherwise use a simple array list
                proxies = new ArrayList<Object>();
            }

            boolean isSingleValued = !returnType.equals("java.util.Collection") && !returnType.equals("java.util.Set");
            ProxyFactory proxyFactory = null;
            for (Object value : results) {
                // if this is a single valued query and we already have results, throw FinderException
                if (isSingleValued && !proxies.isEmpty()) {
                    throw new FinderException("The single valued query " + methodSignature + "returned more than one item");
                }

                // if we have an EntityBean, we need to proxy it
                if (value instanceof EntityBean) {
                    EntityBean entityBean = (EntityBean) value;
                    if (proxyFactory == null) {
                        CoreDeploymentInfo resultInfo = (CoreDeploymentInfo) getDeploymentInfoByClass(entityBean.getClass());
                        if (resultInfo != null) {
                            proxyFactory = new ProxyFactory(resultInfo);
                        }
                    }

                    if (proxyFactory != null) {
                        if (deploymentInfo.isRemoteQueryResults(methodSignature)) {
                            value = proxyFactory.createRemoteProxy(entityBean, this);
                        } else {
                            value = proxyFactory.createLocalProxy(entityBean, this);
                        }
                    }
                }
                proxies.add(value);
            }

            // if not single valued, return the set
            if (!isSingleValued) {
                return proxies;
            }

            // single valued query that returned no rows, is an exception
            if (proxies.isEmpty()) {
                throw new ObjectNotFoundException();
            }

            // return the single item.... multiple return values was handled in for loop above
            Object returnValue = proxies.iterator().next();
            return returnValue;
        } catch (RuntimeException e) {
            throw new EJBException(e);
        }
    }

    public int update(DeploymentInfo di, String methodSignature, Object... args) throws FinderException {
        CoreDeploymentInfo deploymentInfo = (CoreDeploymentInfo) di;
        String signature = deploymentInfo.getAbstractSchemaName() + "." + methodSignature;

        // exectue the update query
        int result = cmpEngine.executeUpdateQuery(deploymentInfo, signature, args);
        return result;
    }

    private void removeEJBObject(Method callMethod, ThreadContext callContext) throws OpenEJBException {
        DeploymentInfo deploymentInfo = callContext.getDeploymentInfo();

        TransactionPolicy txPolicy = createTransactionPolicy(deploymentInfo.getTransactionType(callMethod), callContext);

        try {
            EntityBean entityBean = (EntityBean) cmpEngine.loadBean(callContext, callContext.getPrimaryKey());
            if (entityBean == null) {
                throw new NoSuchObjectException(callContext.getDeploymentInfo().getDeploymentID() + " " + callContext.getPrimaryKey());
            }
            ejbRemove(entityBean);
            cmpEngine.removeBean(callContext);
        } catch (NoSuchObjectException e) {
            handleApplicationException(txPolicy, e, false);
        } catch (Throwable e) {// handle reflection exception
            handleSystemException(txPolicy, e, callContext);
        } finally {
            afterInvoke(txPolicy, callContext);
        }
    }

    private void cancelTimers(ThreadContext threadContext) {
        DeploymentInfo deploymentInfo = threadContext.getDeploymentInfo();
        Object primaryKey = threadContext.getPrimaryKey();

        // stop timers
        if (primaryKey != null && deploymentInfo.getEjbTimerService() != null) {
            EjbTimerService timerService = deploymentInfo.getEjbTimerService();
            if (timerService != null && timerService instanceof EjbTimerServiceImpl) {
                for (Timer timer : deploymentInfo.getEjbTimerService().getTimers(primaryKey)) {
                    timer.cancel();
                }
            }
        }
    }

    private class ContainerCmpCallback implements CmpCallback {
        public void setEntityContext(EntityBean entity) {
            CmpContainer.this.setEntityContext(entity);
        }

        public void unsetEntityContext(EntityBean entity) {
            CmpContainer.this.unsetEntityContext(entity);
        }

        public void ejbActivate(EntityBean entity) {
            CmpContainer.this.ejbActivate(entity);
        }

        public void ejbPassivate(EntityBean entity) {
            CmpContainer.this.ejbPassivate(entity);
        }

        public void ejbLoad(EntityBean entity) {
            CmpContainer.this.ejbLoad(entity);
        }

        public void ejbStore(EntityBean entity) {
            CmpContainer.this.ejbStore(entity);
        }

        public void ejbRemove(EntityBean entity) throws RemoveException {
            CmpContainer.this.ejbRemove(entity);
        }
    }
}
