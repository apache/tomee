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

package org.apache.openejb.core.entity;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.BeanContext;
import org.apache.openejb.ContainerType;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.ExceptionType;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.timer.EjbTimerService;
import org.apache.openejb.core.timer.EjbTimerServiceImpl;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.ArrayEnumeration;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.ejb.EJBAccessException;
import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBLocalHome;
import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.EJBObject;
import jakarta.ejb.EntityBean;
import jakarta.ejb.NoSuchEntityException;
import jakarta.ejb.Timer;
import jakarta.transaction.TransactionSynchronizationRegistry;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import static org.apache.openejb.core.transaction.EjbTransactionUtil.afterInvoke;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.createTransactionPolicy;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.handleApplicationException;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.handleSystemException;

/**
 * @org.apache.xbean.XBean element="bmpContainer"
 */
public class EntityContainer implements RpcContainer {

    private final EntityInstanceManager instanceManager;

    private final Map<String, BeanContext> deploymentRegistry = new HashMap<>();

    private final Object containerID;

    public static Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");
    private final SecurityService securityService;

    /**
     * Tracks entity instances that have been "entered" so we can throw reentrancy exceptions.
     */
    protected EntrancyTracker entrancyTracker;

    public EntityContainer(final Object id, final SecurityService securityService, final int poolSize) throws OpenEJBException {
        this.containerID = id;
        this.securityService = securityService;
        entrancyTracker = new EntrancyTracker(SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class));

        instanceManager = new EntityInstanceManager(this, securityService, poolSize);
    }

    @Override
    public synchronized BeanContext[] getBeanContexts() {
        return deploymentRegistry.values().toArray(new BeanContext[deploymentRegistry.size()]);
    }

    @Override
    public synchronized BeanContext getBeanContext(final Object deploymentID) {
        final String id = (String) deploymentID;
        return deploymentRegistry.get(id);
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.BMP_ENTITY;
    }

    @Override
    public Object getContainerID() {
        return containerID;
    }

    @Override
    public void deploy(final BeanContext beanContext) throws OpenEJBException {
        synchronized (this) {
            deploymentRegistry.put((String) beanContext.getDeploymentID(), beanContext);
            beanContext.setContainer(this);
        }
        instanceManager.deploy(beanContext);
    }

    @Override
    public void start(final BeanContext info) throws OpenEJBException {
        final EjbTimerService timerService = info.getEjbTimerService();
        if (timerService != null) {
            timerService.start();
        }
    }

    @Override
    public void stop(final BeanContext info) throws OpenEJBException {
        info.stop();
    }

    @Override
    public void undeploy(final BeanContext info) throws OpenEJBException {
        instanceManager.undeploy(info);

        synchronized (this) {
            final String id = (String) info.getDeploymentID();
            deploymentRegistry.remove(id);
            info.setContainer(null);
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

            final boolean authorized = type == InterfaceType.TIMEOUT || getSecurityService().isCallerAuthorized(callMethod, type);

            if (!authorized) {
                throw new ApplicationException(new EJBAccessException("Unauthorized Access by Principal Denied"));
            }

            final Class declaringClass = callMethod.getDeclaringClass();
            final String methodName = callMethod.getName();

            if (EJBHome.class.isAssignableFrom(declaringClass) || EJBLocalHome.class.isAssignableFrom(declaringClass)) {
                if (declaringClass != EJBHome.class && declaringClass != EJBLocalHome.class) {

                    if (methodName.startsWith("create")) {

                        return createEJBObject(callMethod, args, callContext, type);
                    } else if (methodName.startsWith("find")) {

                        return findMethod(callMethod, args, callContext, type);
                    } else {

                        return homeMethod(callMethod, args, callContext, type);
                    }
                } else if (methodName.equals("remove")) {
                    removeEJBObject(callMethod, args, callContext, type);
                    return null;
                }
            } else if ((EJBObject.class == declaringClass || EJBLocalObject.class == declaringClass) && methodName.equals("remove")) {
                removeEJBObject(callMethod, args, callContext, type);
                return null;
            }

            callContext.setCurrentOperation(type == InterfaceType.TIMEOUT ? Operation.TIMEOUT : Operation.BUSINESS);
            final Method runMethod = beanContext.getMatchingBeanMethod(callMethod);

            callContext.set(Method.class, runMethod);

            return invoke(type, callMethod, runMethod, args, callContext);

        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    private SecurityService getSecurityService() {
        return securityService;
    }

    public EntityInstanceManager getInstanceManager() {
        return instanceManager;
    }

    protected Object invoke(final InterfaceType type,
                            final Method callMethod,
                            final Method runMethod,
                            final Object[] args,
                            final ThreadContext callContext) throws OpenEJBException {
        final BeanContext beanContext = callContext.getBeanContext();
        final TransactionPolicy txPolicy = createTransactionPolicy(beanContext.getTransactionType(callMethod, type), callContext);

        EntityBean bean = null;

        Object returnValue = null;
        entrancyTracker.enter(callContext.getBeanContext(), callContext.getPrimaryKey());
        try {
            bean = instanceManager.obtainInstance(callContext);

            ejbLoad_If_No_Transaction(callContext, bean);
            returnValue = runMethod.invoke(bean, args);
            ejbStore_If_No_Transaction(callContext, bean);
            instanceManager.poolInstance(callContext, bean, callContext.getPrimaryKey());
        } catch (final Throwable e) {
            handleException(txPolicy, e, callContext, bean);
        } finally {
            entrancyTracker.exit(callContext.getBeanContext(), callContext.getPrimaryKey());
            afterInvoke(txPolicy, callContext);
        }

        return returnValue;
    }

    public void ejbLoad_If_No_Transaction(final ThreadContext callContext, final EntityBean bean) throws Exception {
        final Operation orginalOperation = callContext.getCurrentOperation();
        if (orginalOperation == Operation.BUSINESS || orginalOperation == Operation.REMOVE) {

            final TransactionPolicy callerTxPolicy = callContext.getTransactionPolicy();
            if (callerTxPolicy != null && callerTxPolicy.isTransactionActive()) {
                return;
            }

            final BeanContext beanContext = callContext.getBeanContext();
            final TransactionPolicy txPolicy = beanContext.getTransactionPolicyFactory().createTransactionPolicy(TransactionType.Supports);
            try {
                // double check we don't have an active transaction
                if (!txPolicy.isTransactionActive()) {
                    callContext.setCurrentOperation(Operation.LOAD);
                    bean.ejbLoad();
                }
            } catch (final NoSuchEntityException e) {
                instanceManager.discardInstance(callContext, bean);
                throw new ApplicationException(new NoSuchObjectException("Entity not found: " + callContext.getPrimaryKey())/*.initCause(e)*/);
            } catch (final Exception e) {
                instanceManager.discardInstance(callContext, bean);
                throw e;
            } finally {
                callContext.setCurrentOperation(orginalOperation);
                txPolicy.commit();
            }

        }
    }

    public void ejbStore_If_No_Transaction(final ThreadContext callContext, final EntityBean bean) throws Exception {
        final Operation currentOp = callContext.getCurrentOperation();
        if (currentOp == Operation.BUSINESS) {

            final TransactionPolicy callerTxPolicy = callContext.getTransactionPolicy();
            if (callerTxPolicy != null && callerTxPolicy.isTransactionActive()) {
                return;
            }

            final BeanContext beanContext = callContext.getBeanContext();
            final TransactionPolicy txPolicy = beanContext.getTransactionPolicyFactory().createTransactionPolicy(TransactionType.Supports);
            try {
                // double check we don't have an active transaction
                if (!txPolicy.isTransactionActive()) {
                    callContext.setCurrentOperation(Operation.STORE);
                    bean.ejbStore();
                }
            } catch (final Exception e) {
                instanceManager.discardInstance(callContext, bean);
                throw e;
            } finally {
                callContext.setCurrentOperation(currentOp);
                txPolicy.commit();
            }
        }
    }

    protected void didCreateBean(final ThreadContext callContext, final EntityBean bean) throws OpenEJBException {
    }

    protected ProxyInfo createEJBObject(final Method callMethod, final Object[] args, final ThreadContext callContext, final InterfaceType type) throws OpenEJBException {
        final BeanContext beanContext = callContext.getBeanContext();

        callContext.setCurrentOperation(Operation.CREATE);

        /*
        * According to section 9.1.5.1 of the EJB 1.1 specification, the "ejbPostCreate(...)
        * method executes in the same transaction context as the previous ejbCreate(...) method."
        *
        * For this reason the TransactionScopeHandler methods usally preformed by the invoke( )
        * operation must be handled here along with the call explicitly.
        * This ensures that the afterInvoke() is not processed between the ejbCreate and ejbPostCreate methods to
        * ensure that the ejbPostCreate executes in the same transaction context of the ejbCreate.
        * This would otherwise not be possible if container-managed transactions were used because
        * the TransactionScopeManager would attempt to commit the transaction immediately after the ejbCreate
        * and before the ejbPostCreate had a chance to execute.  Once the ejbPostCreate method execute the
        * super classes afterInvoke( ) method will be executed committing the transaction if its a CMT.
        */

        final TransactionPolicy txPolicy = createTransactionPolicy(beanContext.getTransactionType(callMethod, type), callContext);

        EntityBean bean = null;
        Object primaryKey = null;
        try {
            // Get new ready instance
            bean = instanceManager.obtainInstance(callContext);

            // Obtain the proper ejbCreate() method
            final Method ejbCreateMethod = beanContext.getMatchingBeanMethod(callMethod);

            // invoke the ejbCreate which returns the primary key
            primaryKey = ejbCreateMethod.invoke(bean, args);

            didCreateBean(callContext, bean);

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

            // update pool
            instanceManager.poolInstance(callContext, bean, primaryKey);
        } catch (final Throwable e) {
            handleException(txPolicy, e, callContext, bean);
        } finally {
            afterInvoke(txPolicy, callContext);
        }

        return new ProxyInfo(beanContext, primaryKey);

    }

    protected Object findMethod(final Method callMethod, final Object[] args, final ThreadContext callContext, final InterfaceType type) throws OpenEJBException {
        final BeanContext beanContext = callContext.getBeanContext();
        callContext.setCurrentOperation(Operation.FIND);
        final Method runMethod = beanContext.getMatchingBeanMethod(callMethod);
        Object returnValue = invoke(type, callMethod, runMethod, args, callContext);

        /*
        * Find operations return either a single primary key or a collection of primary keys.
        * The primary keys are converted to ProxyInfo objects.
        */
        if (returnValue instanceof Collection) {
            final Iterator keys = ((Collection) returnValue).iterator();
            final Vector<ProxyInfo> proxies = new Vector<>();
            while (keys.hasNext()) {
                final Object primaryKey = keys.next();
                proxies.addElement(new ProxyInfo(beanContext, primaryKey));
            }
            returnValue = proxies;
        } else if (returnValue instanceof Enumeration) {
            final Enumeration keys = (Enumeration) returnValue;
            final Vector<ProxyInfo> proxies = new Vector<>();
            while (keys.hasMoreElements()) {
                final Object primaryKey = keys.nextElement();
                proxies.addElement(new ProxyInfo(beanContext, primaryKey));
            }
            returnValue = new ArrayEnumeration<>(proxies);
        } else {
            returnValue = new ProxyInfo(beanContext, returnValue);
        }

        return returnValue;
    }

    protected Object homeMethod(final Method callMethod, final Object[] args, final ThreadContext callContext, final InterfaceType type) throws OpenEJBException {
        final BeanContext beanContext = callContext.getBeanContext();
        callContext.setCurrentOperation(Operation.HOME);
        final Method runMethod = beanContext.getMatchingBeanMethod(callMethod);
        return invoke(type, callMethod, runMethod, args, callContext);
    }

    protected void didRemove(final EntityBean bean, final ThreadContext threadContext) throws OpenEJBException {
        cancelTimers(threadContext);
    }

    private void cancelTimers(final ThreadContext threadContext) {
        final BeanContext beanContext = threadContext.getBeanContext();
        final Object primaryKey = threadContext.getPrimaryKey();

        // if we have a real timerservice, stop all timers. Otherwise, ignore...
        if (primaryKey != null) {
            final EjbTimerService timerService = beanContext.getEjbTimerService();
            if (timerService != null && timerService instanceof EjbTimerServiceImpl) {
                for (final Timer timer : beanContext.getEjbTimerService().getTimers(primaryKey)) {
                    timer.cancel();
                }
            }
        }
    }

    protected void removeEJBObject(final Method callMethod, final Object[] args, final ThreadContext callContext, final InterfaceType type) throws OpenEJBException {
        callContext.setCurrentOperation(Operation.REMOVE);

        final BeanContext beanContext = callContext.getBeanContext();
        final TransactionPolicy txPolicy = createTransactionPolicy(beanContext.getTransactionType(callMethod, type), callContext);

        EntityBean bean = null;
        try {

            bean = instanceManager.obtainInstance(callContext);

            ejbLoad_If_No_Transaction(callContext, bean);
            bean.ejbRemove();
            didRemove(bean, callContext);
            instanceManager.poolInstance(callContext, bean, callContext.getPrimaryKey());
        } catch (final Throwable e) {
            handleException(txPolicy, e, callContext, bean);
        } finally {
            afterInvoke(txPolicy, callContext);
        }
    }

    private void handleException(final TransactionPolicy txPolicy, Throwable e, final ThreadContext callContext, final EntityBean bean) throws OpenEJBException {
        final ExceptionType type;
        if (e instanceof InvocationTargetException) {
            e = ((InvocationTargetException) e).getTargetException();
            type = callContext.getBeanContext().getExceptionType(e);
        } else if (e instanceof ApplicationException) {
            e = ((ApplicationException) e).getRootCause();
            type = ExceptionType.APPLICATION;
        } else if (e instanceof SystemException) {
            e = ((SystemException) e).getRootCause();
            type = ExceptionType.SYSTEM;
        } else {
            type = ExceptionType.SYSTEM;
        }

        if (type == ExceptionType.SYSTEM) {
            // System Exception
            if (bean != null) {
                try {
                    instanceManager.discardInstance(callContext, bean);
                } catch (final SystemException e1) {
                    logger.error("The instance manager encountered an unkown system exception while trying to discard the entity instance with primary key " +
                        callContext.getPrimaryKey());
                }
            }
            handleSystemException(txPolicy, e, callContext);
        } else {
            // Application Exception
            instanceManager.poolInstance(callContext, bean, callContext.getPrimaryKey());
            handleApplicationException(txPolicy, e, type == ExceptionType.APPLICATION_ROLLBACK);
        }
    }
}
