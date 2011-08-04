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
import javax.ejb.EJBContext;
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
import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.ContainerType;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.loader.SystemInstance;
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
    protected final Map<Object, BeanContext> deploymentsById = new HashMap<Object, BeanContext>();

    /**
     * When events are fired from the CMP engine only an entity bean instance is returned.  The type of the bean is used
     * to find the deployment info.  This means that when the same type is used multiple ejb deployments a random deployment
     * will be selected to handle the ejb callback.
     */
    protected final Map<Class, BeanContext> beansByClass = new HashMap<Class, BeanContext>();

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

    public synchronized BeanContext[] getBeanContexts() {
        return deploymentsById.values().toArray(new BeanContext[deploymentsById.size()]);
    }

    public synchronized BeanContext getBeanContext(Object deploymentID) {
        return deploymentsById.get(deploymentID);
    }

    private BeanContext getBeanContextByClass(Class type) {
        BeanContext beanContext = null;
        while (type != null && beanContext == null) {
            beanContext = beansByClass.get(type);
            type = type.getSuperclass();
        }

        return beanContext;
    }

    public void deploy(BeanContext beanContext) throws OpenEJBException {
        synchronized (this) {
            Object deploymentId = beanContext.getDeploymentID();

            cmpEngine.deploy(beanContext);
            beanContext.setContainerData(cmpEngine);
            beanContext.set(EJBContext.class, new EntityContext(securityService));
            // try to set deploymentInfo static field on bean implementation class
            try {
                Field field = beanContext.getCmpImplClass().getField("deploymentInfo");
                field.set(null, beanContext);
            } catch (Exception e) {
                // ignore
            }

            // add to indexes
            deploymentsById.put(deploymentId, beanContext);
            beansByClass.put(beanContext.getCmpImplClass(), beanContext);
            beanContext.setContainer(this);
        }

        EjbTimerService timerService = beanContext.getEjbTimerService();
        if (timerService != null) {
            timerService.start();
        }
    }

    public void start(BeanContext beanContext) throws OpenEJBException {
    }
    
    public void stop(BeanContext beanContext) throws OpenEJBException {
    }
    
    public void undeploy(BeanContext beanContext) throws OpenEJBException {
        EjbTimerService timerService = beanContext.getEjbTimerService();
        if (timerService != null) {
            timerService.stop();
        }
        synchronized (this) {
            deploymentsById.remove(beanContext.getDeploymentID());
            beansByClass.remove(beanContext.getCmpImplClass());

            try {
                Field field = beanContext.getCmpImplClass().getField("deploymentInfo");
                field.set(null, null);
            } catch (Exception e) {
                // ignore
            }

            beanContext.setContainer(null);
            beanContext.setContainerData(null);
        }
    }

    public Object getEjbInstance(BeanContext beanContext, Object primaryKey) {
        ThreadContext callContext = new ThreadContext(beanContext, primaryKey);

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
        BeanContext beanContext = this.getBeanContext(deployID);

        if (beanContext == null) throw new OpenEJBException("Deployment does not exist in this container. Deployment(id='"+deployID+"'), Container(id='"+containerID+"')");

        // Use the backup way to determine call type if null was supplied.
        if (type == null) type = beanContext.getInterfaceType(callInterface);

        ThreadContext callContext = new ThreadContext(beanContext, primKey);

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
                        return createEJBObject(callMethod, args, callContext, type);
                    } else if (methodName.equals("findByPrimaryKey")) {
                        return findByPrimaryKey(callMethod, args, callContext, type);
                    } else if (methodName.startsWith("find")) {
                        return findEJBObject(callMethod, args, callContext, type);
                    } else {
                        return homeMethod(callMethod, args, callContext, type);
                    }
                } else if (methodName.equals("remove")) {
                    removeEJBObject(callMethod, callContext, type);
                    return null;
                }
            } else if ((EJBObject.class == declaringClass || EJBLocalObject.class == declaringClass) && methodName.equals("remove")) {
                removeEJBObject(callMethod, callContext, type);
                return null;
            }

            // business method
            callContext.setCurrentOperation(Operation.BUSINESS);
            Method runMethod = beanContext.getMatchingBeanMethod(callMethod);

            callContext.set(Method.class, runMethod);

            Object retValue = businessMethod(callMethod, runMethod, args, callContext, type);

            return retValue;
        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    private EntityBean createNewInstance(ThreadContext callContext) {
        BeanContext beanContext = callContext.getBeanContext();
        try {
            EntityBean bean = (EntityBean) beanContext.getCmpImplClass().newInstance();
            return bean;
        } catch (Exception e) {
            throw new EJBException("Unable to create new entity bean instance " + beanContext.getCmpImplClass(), e);
        }
    }

    private ThreadContext createThreadContext(EntityBean entityBean) {
        if (entityBean == null) throw new NullPointerException("entityBean is null");

        BeanContext beanContext = getBeanContextByClass(entityBean.getClass());
        KeyGenerator keyGenerator = beanContext.getKeyGenerator();
        Object primaryKey = keyGenerator.getPrimaryKey(entityBean);

        ThreadContext callContext = new ThreadContext(beanContext, primaryKey);
        return callContext;
    }

    private void setEntityContext(EntityBean entityBean) {
        if (entityBean == null) throw new NullPointerException("entityBean is null");

        // activating entity doen't have a primary key
        BeanContext beanContext = getBeanContextByClass(entityBean.getClass());

        ThreadContext callContext = new ThreadContext(beanContext, null);
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

    private Object businessMethod(Method callMethod, Method runMethod, Object[] args, ThreadContext callContext, InterfaceType interfaceType) throws OpenEJBException {
        BeanContext beanContext = callContext.getBeanContext();

        TransactionPolicy txPolicy = createTransactionPolicy(beanContext.getTransactionType(callMethod, interfaceType), callContext);

        EntityBean bean;
        Object returnValue = null;

        entrancyTracker.enter(beanContext, callContext.getPrimaryKey());
        try {
            bean = (EntityBean) cmpEngine.loadBean(callContext, callContext.getPrimaryKey());
            if (bean == null) {
                throw new NoSuchObjectException(beanContext.getDeploymentID() + " : " + callContext.getPrimaryKey());
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

            ExceptionType type = callContext.getBeanContext().getExceptionType(e);
            if (type == ExceptionType.SYSTEM) {
                /* System Exception ****************************/
                handleSystemException(txPolicy, e, callContext);
            } else {
                /* Application Exception ***********************/
                handleApplicationException(txPolicy, e, type == ExceptionType.APPLICATION_ROLLBACK);
            }
        } finally {
            entrancyTracker.exit(beanContext, callContext.getPrimaryKey());
            afterInvoke(txPolicy, callContext);
        }

        return returnValue;
    }

    private Object homeMethod(Method callMethod, Object[] args, ThreadContext callContext, InterfaceType interfaceType) throws OpenEJBException {
        BeanContext beanContext = callContext.getBeanContext();

        TransactionPolicy txPolicy = createTransactionPolicy(beanContext.getTransactionType(callMethod, interfaceType), callContext);

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

                Method runMethod = beanContext.getMatchingBeanMethod(callMethod);

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

            ExceptionType type = callContext.getBeanContext().getExceptionType(e);
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

    private ProxyInfo createEJBObject(Method callMethod, Object[] args, ThreadContext callContext, InterfaceType interfaceType) throws OpenEJBException {
        BeanContext beanContext = callContext.getBeanContext();

        TransactionPolicy txPolicy = createTransactionPolicy(beanContext.getTransactionType(callMethod, interfaceType), callContext);

        EntityBean bean;
        Object primaryKey = null;

        try {
            // Obtain a bean instance from the method ready pool
            bean = createNewInstance(callContext);

            // set the entity context
            setEntityContext(bean);

            // Obtain the proper ejbCreate() method
            Method ejbCreateMethod = beanContext.getMatchingBeanMethod(callMethod);

            // Set current operation for allowed operations
            callContext.setCurrentOperation(Operation.CREATE);

            // Invoke the proper ejbCreate() method on the instance
            ejbCreateMethod.invoke(bean, args);

            // create the new bean
            primaryKey = cmpEngine.createBean(bean, callContext);

            // determine post create callback method
            Method ejbPostCreateMethod = beanContext.getMatchingPostCreateMethod(ejbCreateMethod);

            // create a new context containing the pk for the post create call
            ThreadContext postCreateContext = new ThreadContext(beanContext, primaryKey);
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

            ExceptionType type = callContext.getBeanContext().getExceptionType(e);
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

        return new ProxyInfo(beanContext, primaryKey);
    }

    private Object findByPrimaryKey(Method callMethod, Object[] args, ThreadContext callContext, InterfaceType interfaceType) throws OpenEJBException {
        BeanContext beanContext = callContext.getBeanContext();

        TransactionPolicy txPolicy = createTransactionPolicy(beanContext.getTransactionType(callMethod, interfaceType), callContext);

        try {
            EntityBean bean = (EntityBean) cmpEngine.loadBean(callContext, args[0]);
            if (bean == null) {
                throw new ObjectNotFoundException(beanContext.getDeploymentID() + " : " + args[0]);
            }

            // rebuild the primary key
            KeyGenerator kg = beanContext.getKeyGenerator();
            Object primaryKey = kg.getPrimaryKey(bean);

            // create a new ProxyInfo based on the deployment info and primary key
            return new ProxyInfo(beanContext, primaryKey);
        } catch (javax.ejb.FinderException fe) {
            handleApplicationException(txPolicy, fe, false);
        } catch (Throwable e) {// handle reflection exception
            handleSystemException(txPolicy, e, callContext);
        } finally {
            afterInvoke(txPolicy, callContext);
        }
        throw new AssertionError("Should not get here");
    }

    private Object findEJBObject(Method callMethod, Object[] args, ThreadContext callContext, InterfaceType interfaceType) throws OpenEJBException {
        BeanContext beanContext = callContext.getBeanContext();

        TransactionPolicy txPolicy = createTransactionPolicy(beanContext.getTransactionType(callMethod, interfaceType), callContext);

        try {
            List<Object> results = cmpEngine.queryBeans(callContext, callMethod, args);

            KeyGenerator kg = beanContext.getKeyGenerator();

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
                        proxies.add(new ProxyInfo(beanContext, primaryKey));
                    }
                }
                if (callMethod.getReturnType() == Enumeration.class) {
                    return new Enumerator(proxies);
                } else {
                    return proxies;
                }
            } else {
                if (results.size() != 1) {
                    throw new ObjectNotFoundException("A Enteprise bean with deployment_id = " + beanContext.getDeploymentID() + " and primarykey = " + args[0] + " Does not exist");
                }

                // create a new ProxyInfo based on the deployment info and primary key
                EntityBean bean = (EntityBean) results.get(0);
                if (bean == null) {
                    return null;
                } else {
                    Object primaryKey = kg.getPrimaryKey(bean);
                    return new ProxyInfo(beanContext, primaryKey);
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

    public Object select(BeanContext beanContext, String methodSignature, String returnType, Object... args) throws FinderException {
        String signature = beanContext.getAbstractSchemaName() + "." + methodSignature;

        try {
            // execute the select query
            Collection<Object> results = cmpEngine.queryBeans(beanContext, signature, args);

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
                        BeanContext result = getBeanContextByClass(entityBean.getClass());
                        if (result != null) {
                            proxyFactory = new ProxyFactory(result);
                        }
                    }

                    if (proxyFactory != null) {
                        if (beanContext.isRemoteQueryResults(methodSignature)) {
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

    public int update(BeanContext beanContext, String methodSignature, Object... args) throws FinderException {
        String signature = beanContext.getAbstractSchemaName() + "." + methodSignature;

        // exectue the update query
        int result = cmpEngine.executeUpdateQuery(beanContext, signature, args);
        return result;
    }

    private void removeEJBObject(Method callMethod, ThreadContext callContext, InterfaceType interfaceType) throws OpenEJBException {
        BeanContext beanContext = callContext.getBeanContext();

        TransactionPolicy txPolicy = createTransactionPolicy(beanContext.getTransactionType(callMethod, interfaceType), callContext);

        try {
            EntityBean entityBean = (EntityBean) cmpEngine.loadBean(callContext, callContext.getPrimaryKey());
            if (entityBean == null) {
                throw new NoSuchObjectException(callContext.getBeanContext().getDeploymentID() + " " + callContext.getPrimaryKey());
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
        BeanContext beanContext = threadContext.getBeanContext();
        Object primaryKey = threadContext.getPrimaryKey();

        // stop timers
        if (primaryKey != null && beanContext.getEjbTimerService() != null) {
            EjbTimerService timerService = beanContext.getEjbTimerService();
            if (timerService != null && timerService instanceof EjbTimerServiceImpl) {
                for (Timer timer : beanContext.getEjbTimerService().getTimers(primaryKey)) {
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
