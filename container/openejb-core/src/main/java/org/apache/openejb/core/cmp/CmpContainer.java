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

import org.apache.openejb.ApplicationException;
import org.apache.openejb.BeanContext;
import org.apache.openejb.ContainerType;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.core.ExceptionType;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.entity.EntityContext;
import org.apache.openejb.core.entity.EntrancyTracker;
import org.apache.openejb.core.timer.EjbTimerService;
import org.apache.openejb.core.timer.EjbTimerServiceImpl;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Enumerator;

import jakarta.ejb.EJBAccessException;
import jakarta.ejb.EJBContext;
import jakarta.ejb.EJBException;
import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBLocalHome;
import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.EJBObject;
import jakarta.ejb.EntityBean;
import jakarta.ejb.FinderException;
import jakarta.ejb.ObjectNotFoundException;
import jakarta.ejb.RemoveException;
import jakarta.ejb.Timer;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.openejb.core.transaction.EjbTransactionUtil.afterInvoke;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.createTransactionPolicy;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.handleApplicationException;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.handleSystemException;

/**
 * @org.apache.xbean.XBean element="cmpContainer"
 */
public class CmpContainer implements RpcContainer {

    protected final Object containerID;
    protected final SecurityService securityService;

    /**
     * Index used for getDeployments() and getDeploymentInfo(deploymentId).
     */
    protected final Map<Object, BeanContext> deploymentsById = new HashMap<>();

    /**
     * When events are fired from the CMP engine only an entity bean instance is returned.  The type of the bean is used
     * to find the deployment info.  This means that when the same type is used multiple ejb deployments a random deployment
     * will be selected to handle the ejb callback.
     */
    protected final Map<Class, BeanContext> beansByClass = new HashMap<>();

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

    public CmpContainer(final Object id,
                        final TransactionManager transactionManager,
                        final SecurityService securityService,
                        final String cmpEngineFactory) throws OpenEJBException {
        this.containerID = id;
        this.securityService = securityService;
        synchronizationRegistry = SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class);
        entrancyTracker = new EntrancyTracker(synchronizationRegistry);

        // create the cmp engine instance
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }

        final CmpEngineFactory factory;
        try {
            final Class<?> cmpEngineFactoryClass = classLoader.loadClass(cmpEngineFactory);
            factory = (CmpEngineFactory) cmpEngineFactoryClass.newInstance();
        } catch (final Exception e) {
            throw new OpenEJBException("Unable to create cmp engine factory " + cmpEngineFactory, e);
        }
        factory.setTransactionManager(transactionManager);
        factory.setTransactionSynchronizationRegistry(synchronizationRegistry);
        factory.setCmpCallback(new ContainerCmpCallback());
        cmpEngine = factory.create();
    }

    @Override
    public Object getContainerID() {
        return containerID;
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.CMP_ENTITY;
    }

    @Override
    public synchronized BeanContext[] getBeanContexts() {
        return deploymentsById.values().toArray(new BeanContext[deploymentsById.size()]);
    }

    @Override
    public synchronized BeanContext getBeanContext(final Object deploymentID) {
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

    @Override
    public void deploy(final BeanContext beanContext) throws OpenEJBException {
        synchronized (this) {
            final Object deploymentId = beanContext.getDeploymentID();

            cmpEngine.deploy(beanContext);
            beanContext.setContainerData(cmpEngine);
            beanContext.set(EJBContext.class, new EntityContext(securityService));
            // try to set deploymentInfo static field on bean implementation class
            try {
                final Field field = beanContext.getCmpImplClass().getField("deploymentInfo");
                field.set(null, beanContext);
            } catch (final Exception e) {
                // ignore
            }

            // add to indexes
            deploymentsById.put(deploymentId, beanContext);
            beansByClass.put(beanContext.getCmpImplClass(), beanContext);
            beanContext.setContainer(this);
        }
    }

    @Override
    public void start(final BeanContext beanContext) throws OpenEJBException {
        final EjbTimerService timerService = beanContext.getEjbTimerService();
        if (timerService != null) {
            timerService.start();
        }
    }

    @Override
    public void stop(final BeanContext beanContext) throws OpenEJBException {
        beanContext.stop();
    }

    @Override
    public void undeploy(final BeanContext beanContext) throws OpenEJBException {
        synchronized (this) {
            deploymentsById.remove(beanContext.getDeploymentID());
            beansByClass.remove(beanContext.getCmpImplClass());

            try {
                final Field field = beanContext.getCmpImplClass().getField("deploymentInfo");
                field.set(null, null);
            } catch (final Exception e) {
                // ignore
            }

            beanContext.setContainer(null);
            beanContext.setContainerData(null);
        }
    }

    public Object getEjbInstance(final BeanContext beanContext, final Object primaryKey) {
        final ThreadContext callContext = new ThreadContext(beanContext, primaryKey);

        final ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            return cmpEngine.loadBean(callContext, primaryKey);
        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    @Override
    public Object invoke(final Object deployID,
                         InterfaceType type,
                         final Class callInterface,
                         final Method callMethod,
                         final Object[] args,
                         final Object primKey) throws OpenEJBException {
        final BeanContext beanContext = this.getBeanContext(deployID);

        if (beanContext == null) {
            throw new OpenEJBException("Deployment does not exist in this container. Deployment(id='" + deployID + "'), Container(id='" + containerID + "')");
        }

        // Use the backup way to determine call type if null was supplied.
        if (type == null) {
            type = beanContext.getInterfaceType(callInterface);
        }

        final ThreadContext callContext = new ThreadContext(beanContext, primKey);

        final ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {

            final boolean authorized = securityService.isCallerAuthorized(callMethod, type);

            if (!authorized) {
                throw new ApplicationException(new EJBAccessException("Unauthorized Access by Principal Denied"));
            }

            final Class declaringClass = callMethod.getDeclaringClass();
            final String methodName = callMethod.getName();

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
            final Method runMethod = beanContext.getMatchingBeanMethod(callMethod);

            callContext.set(Method.class, runMethod);

            return businessMethod(callMethod, runMethod, args, callContext, type);
        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    private EntityBean createNewInstance(final ThreadContext callContext) {
        final BeanContext beanContext = callContext.getBeanContext();
        try {
            return (EntityBean) beanContext.getCmpImplClass().newInstance();
        } catch (final Exception e) {
            throw new EJBException("Unable to create new entity bean instance " + beanContext.getCmpImplClass(), e);
        }
    }

    private ThreadContext createThreadContext(final EntityBean entityBean) {
        if (entityBean == null) {
            throw new NullPointerException("entityBean is null");
        }

        final BeanContext beanContext = getBeanContextByClass(entityBean.getClass());
        final KeyGenerator keyGenerator = beanContext.getKeyGenerator();
        final Object primaryKey = keyGenerator.getPrimaryKey(entityBean);

        return new ThreadContext(beanContext, primaryKey);
    }

    private void setEntityContext(final EntityBean entityBean) {
        if (entityBean == null) {
            throw new NullPointerException("entityBean is null");
        }

        // activating entity doen't have a primary key
        final BeanContext beanContext = getBeanContextByClass(entityBean.getClass());

        final ThreadContext callContext = new ThreadContext(beanContext, null);
        callContext.setCurrentOperation(Operation.SET_CONTEXT);

        final ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            entityBean.setEntityContext(new EntityContext(securityService));
        } catch (final RemoteException e) {
            throw new EJBException(e);
        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    private void unsetEntityContext(final EntityBean entityBean) {
        if (entityBean == null) {
            throw new NullPointerException("entityBean is null");
        }

        final ThreadContext callContext = createThreadContext(entityBean);
        callContext.setCurrentOperation(Operation.UNSET_CONTEXT);

        final ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            entityBean.unsetEntityContext();
        } catch (final RemoteException e) {
            throw new EJBException(e);
        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    private void ejbLoad(final EntityBean entityBean) {
        if (entityBean == null) {
            throw new NullPointerException("entityBean is null");
        }

        final ThreadContext callContext = createThreadContext(entityBean);
        callContext.setCurrentOperation(Operation.LOAD);

        final ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            entityBean.ejbLoad();
        } catch (final RemoteException e) {
            throw new EJBException(e);
        } finally {
            ThreadContext.exit(oldCallContext);
        }

        // if we call load we must call store
        try {
            //noinspection unchecked
            Set<EntityBean> registeredEntities = (LinkedHashSet<EntityBean>) synchronizationRegistry.getResource(ENTITIES_TO_STORE);
            if (registeredEntities == null) {
                registeredEntities = new LinkedHashSet<>();
                synchronizationRegistry.putResource(ENTITIES_TO_STORE, registeredEntities);
                synchronizationRegistry.registerInterposedSynchronization(new Synchronization() {
                    @Override
                    public void beforeCompletion() {
                        //noinspection unchecked
                        final Set<EntityBean> registeredEntities = (LinkedHashSet<EntityBean>) synchronizationRegistry.getResource(ENTITIES_TO_STORE);
                        if (registeredEntities == null) {
                            return;
                        }
                        for (final EntityBean entityBean : registeredEntities) {
                            ejbStore(entityBean);
                        }
                    }

                    @Override
                    public void afterCompletion(final int i) {
                    }
                });
            }
            registeredEntities.add(entityBean);
        } catch (final Exception e) {
            // no-op
        }
    }

    private void ejbStore(final EntityBean entityBean) {
        if (entityBean == null) {
            throw new NullPointerException("entityBean is null");
        }

        final ThreadContext callContext = createThreadContext(entityBean);
        callContext.setCurrentOperation(Operation.STORE);

        final ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            entityBean.ejbStore();
        } catch (final RemoteException e) {
            throw new EJBException(e);
        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    private void ejbRemove(final EntityBean entityBean) throws RemoveException {
        if (entityBean == null) {
            throw new NullPointerException("entityBean is null");
        }
        if (isDeleted(entityBean)) {
            return;
        }

        final ThreadContext callContext = createThreadContext(entityBean);
        callContext.setCurrentOperation(Operation.REMOVE);

        final ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            entityBean.ejbRemove();
        } catch (final RemoteException e) {
            throw new EJBException(e);
        } finally {
            // clear relationships
            // todo replace with interface call when CmpEntityBean interface is added
            try {
                entityBean.getClass().getMethod("OpenEJB_deleted").invoke(entityBean);
            } catch (final Exception ignored) {
                // no-op
            }
            cancelTimers(callContext);
            ThreadContext.exit(oldCallContext);
        }
    }

    private boolean isDeleted(final EntityBean entityBean) {
        try {
            return (Boolean) entityBean.getClass().getMethod("OpenEJB_isDeleted").invoke(entityBean);
        } catch (final NoSuchMethodException e) {
            return false;
        } catch (final Exception e) {
            throw new EJBException(e);
        }
    }

    private void ejbActivate(final EntityBean entityBean) {
        if (entityBean == null) {
            throw new NullPointerException("entityBean is null");
        }

        final ThreadContext callContext = createThreadContext(entityBean);
        callContext.setCurrentOperation(Operation.ACTIVATE);

        final ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            entityBean.ejbActivate();
        } catch (final RemoteException e) {
            throw new EJBException(e);
        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    private void ejbPassivate(final EntityBean entityBean) {
        if (entityBean == null) {
            throw new NullPointerException("entityBean is null");
        }

        final ThreadContext callContext = createThreadContext(entityBean);
        callContext.setCurrentOperation(Operation.PASSIVATE);

        final ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            entityBean.ejbPassivate();
        } catch (final RemoteException e) {
            throw new EJBException(e);
        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    private Object businessMethod(final Method callMethod,
                                  final Method runMethod,
                                  final Object[] args,
                                  final ThreadContext callContext,
                                  final InterfaceType interfaceType) throws OpenEJBException {
        final BeanContext beanContext = callContext.getBeanContext();

        final TransactionPolicy txPolicy = createTransactionPolicy(beanContext.getTransactionType(callMethod, interfaceType), callContext);

        final EntityBean bean;
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
        } catch (final NoSuchObjectException e) {
            handleApplicationException(txPolicy, e, false);
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getTargetException();
            }

            final ExceptionType type = callContext.getBeanContext().getExceptionType(e);
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

    private Object homeMethod(final Method callMethod, final Object[] args, final ThreadContext callContext, final InterfaceType interfaceType) throws OpenEJBException {
        final BeanContext beanContext = callContext.getBeanContext();

        final TransactionPolicy txPolicy = createTransactionPolicy(beanContext.getTransactionType(callMethod, interfaceType), callContext);

        final EntityBean bean;
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

                final Method runMethod = beanContext.getMatchingBeanMethod(callMethod);

                try {
                    returnValue = runMethod.invoke(bean, args);
                } catch (final IllegalArgumentException e) {
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

            final ExceptionType type = callContext.getBeanContext().getExceptionType(e);
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

    private ProxyInfo createEJBObject(final Method callMethod, final Object[] args, final ThreadContext callContext, final InterfaceType interfaceType) throws OpenEJBException {
        final BeanContext beanContext = callContext.getBeanContext();

        final TransactionPolicy txPolicy = createTransactionPolicy(beanContext.getTransactionType(callMethod, interfaceType), callContext);

        final EntityBean bean;
        Object primaryKey = null;

        try {
            // Obtain a bean instance from the method ready pool
            bean = createNewInstance(callContext);

            // set the entity context
            setEntityContext(bean);

            // Obtain the proper ejbCreate() method
            final Method ejbCreateMethod = beanContext.getMatchingBeanMethod(callMethod);

            // Set current operation for allowed operations
            callContext.setCurrentOperation(Operation.CREATE);

            // Invoke the proper ejbCreate() method on the instance
            ejbCreateMethod.invoke(bean, args);

            // create the new bean
            primaryKey = cmpEngine.createBean(bean, callContext);

            // determine post create callback method
            final Method ejbPostCreateMethod = beanContext.getMatchingPostCreateMethod(ejbCreateMethod);

            // create a new context containing the pk for the post create call
            final ThreadContext postCreateContext = new ThreadContext(beanContext, primaryKey);
            postCreateContext.setCurrentOperation(Operation.POST_CREATE);

            final ThreadContext oldContext = ThreadContext.enter(postCreateContext);
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

            final ExceptionType type = callContext.getBeanContext().getExceptionType(e);
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

    private Object findByPrimaryKey(final Method callMethod, final Object[] args, final ThreadContext callContext, final InterfaceType interfaceType) throws OpenEJBException {
        final BeanContext beanContext = callContext.getBeanContext();

        final TransactionPolicy txPolicy = createTransactionPolicy(beanContext.getTransactionType(callMethod, interfaceType), callContext);

        try {
            final EntityBean bean = (EntityBean) cmpEngine.loadBean(callContext, args[0]);
            if (bean == null) {
                throw new ObjectNotFoundException(beanContext.getDeploymentID() + " : " + args[0]);
            }

            // rebuild the primary key
            final KeyGenerator kg = beanContext.getKeyGenerator();
            final Object primaryKey = kg.getPrimaryKey(bean);

            // create a new ProxyInfo based on the deployment info and primary key
            return new ProxyInfo(beanContext, primaryKey);
        } catch (final FinderException fe) {
            handleApplicationException(txPolicy, fe, false);
        } catch (final Throwable e) {// handle reflection exception
            handleSystemException(txPolicy, e, callContext);
        } finally {
            afterInvoke(txPolicy, callContext);
        }
        throw new AssertionError("Should not get here");
    }

    private Object findEJBObject(final Method callMethod, final Object[] args, final ThreadContext callContext, final InterfaceType interfaceType) throws OpenEJBException {
        final BeanContext beanContext = callContext.getBeanContext();

        final TransactionPolicy txPolicy = createTransactionPolicy(beanContext.getTransactionType(callMethod, interfaceType), callContext);

        try {
            final List<Object> results = cmpEngine.queryBeans(callContext, callMethod, args);

            final KeyGenerator kg = beanContext.getKeyGenerator();

            // The following block of code is responsible for returning ProxyInfo object(s) for each
            // matching entity bean found by the query.  If its a multi-value find operation a Vector
            // of ProxyInfo objects will be returned. If its a single-value find operation then a
            // single ProxyInfo object is returned.
            if (callMethod.getReturnType() == Collection.class || callMethod.getReturnType() == Enumeration.class) {
                final List<ProxyInfo> proxies = new ArrayList<>();
                for (final Object value : results) {
                    final EntityBean bean = (EntityBean) value;

                    if (value == null) {
                        proxies.add(null);
                    } else {
                        // get the primary key
                        final Object primaryKey = kg.getPrimaryKey(bean);

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
                    throw new ObjectNotFoundException("A Enteprise bean with deployment_id = " +
                        beanContext.getDeploymentID() +
                        (args != null && args.length >= 1 ? " and primarykey = " + args[0] : "") +
                        " Does not exist");
                }

                // create a new ProxyInfo based on the deployment info and primary key
                final EntityBean bean = (EntityBean) results.get(0);
                if (bean == null) {
                    return null;
                } else {
                    final Object primaryKey = kg.getPrimaryKey(bean);
                    return new ProxyInfo(beanContext, primaryKey);
                }
            }
        } catch (final FinderException fe) {
            handleApplicationException(txPolicy, fe, false);
        } catch (final Throwable e) {// handle reflection exception
            handleSystemException(txPolicy, e, callContext);
        } finally {
            afterInvoke(txPolicy, callContext);
        }
        throw new AssertionError("Should not get here");
    }

    public Object select(final BeanContext beanContext, final String methodSignature, final String returnType, final Object... args) throws FinderException {
        final String signature = beanContext.getAbstractSchemaName() + "." + methodSignature;

        try {
            // execute the select query
            final Collection<Object> results = cmpEngine.queryBeans(beanContext, signature, args);

            //
            // process the results
            //

            // If we need to return a set...
            final Collection<Object> proxies;
            if (returnType.equals("java.util.Set")) {
                // we collect values into a LinkedHashSet to preserve ordering
                proxies = new LinkedHashSet<>();
            } else {
                // otherwise use a simple array list
                proxies = new ArrayList<>();
            }

            final boolean isSingleValued = !returnType.equals("java.util.Collection") && !returnType.equals("java.util.Set");
            ProxyFactory proxyFactory = null;
            for (Object value : results) {
                // if this is a single valued query and we already have results, throw FinderException
                if (isSingleValued && !proxies.isEmpty()) {
                    throw new FinderException("The single valued query " + methodSignature + "returned more than one item");
                }

                // if we have an EntityBean, we need to proxy it
                if (value instanceof EntityBean) {
                    final EntityBean entityBean = (EntityBean) value;
                    if (proxyFactory == null) {
                        final BeanContext result = getBeanContextByClass(entityBean.getClass());
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
            return proxies.iterator().next();
        } catch (final RuntimeException e) {
            throw new EJBException(e);
        }
    }

    public int update(final BeanContext beanContext, final String methodSignature, final Object... args) throws FinderException {
        final String signature = beanContext.getAbstractSchemaName() + "." + methodSignature;

        // exectue the update query
        return cmpEngine.executeUpdateQuery(beanContext, signature, args);
    }

    private void removeEJBObject(final Method callMethod, final ThreadContext callContext, final InterfaceType interfaceType) throws OpenEJBException {
        final BeanContext beanContext = callContext.getBeanContext();

        final TransactionPolicy txPolicy = createTransactionPolicy(beanContext.getTransactionType(callMethod, interfaceType), callContext);

        try {
            final EntityBean entityBean = (EntityBean) cmpEngine.loadBean(callContext, callContext.getPrimaryKey());
            if (entityBean == null) {
                throw new NoSuchObjectException(callContext.getBeanContext().getDeploymentID() + " " + callContext.getPrimaryKey());
            }
            ejbRemove(entityBean);
            cmpEngine.removeBean(callContext);
        } catch (final NoSuchObjectException e) {
            handleApplicationException(txPolicy, e, false);
        } catch (final Throwable e) {// handle reflection exception
            handleSystemException(txPolicy, e, callContext);
        } finally {
            afterInvoke(txPolicy, callContext);
        }
    }

    private void cancelTimers(final ThreadContext threadContext) {
        final BeanContext beanContext = threadContext.getBeanContext();
        final Object primaryKey = threadContext.getPrimaryKey();

        // stop timers
        if (primaryKey != null && beanContext.getEjbTimerService() != null) {
            final EjbTimerService timerService = beanContext.getEjbTimerService();
            if (timerService != null && timerService instanceof EjbTimerServiceImpl) {
                for (final Timer timer : beanContext.getEjbTimerService().getTimers(primaryKey)) {
                    timer.cancel();
                }
            }
        }
    }

    private class ContainerCmpCallback implements CmpCallback {

        @Override
        public void setEntityContext(final EntityBean entity) {
            CmpContainer.this.setEntityContext(entity);
        }

        @Override
        public void unsetEntityContext(final EntityBean entity) {
            CmpContainer.this.unsetEntityContext(entity);
        }

        @Override
        public void ejbActivate(final EntityBean entity) {
            CmpContainer.this.ejbActivate(entity);
        }

        @Override
        public void ejbPassivate(final EntityBean entity) {
            CmpContainer.this.ejbPassivate(entity);
        }

        @Override
        public void ejbLoad(final EntityBean entity) {
            CmpContainer.this.ejbLoad(entity);
        }

        @Override
        public void ejbStore(final EntityBean entity) {
            CmpContainer.this.ejbStore(entity);
        }

        @Override
        public void ejbRemove(final EntityBean entity) throws RemoveException {
            CmpContainer.this.ejbRemove(entity);
        }
    }
}
