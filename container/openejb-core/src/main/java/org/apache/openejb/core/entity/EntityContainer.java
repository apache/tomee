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
package org.apache.openejb.core.entity;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.rmi.NoSuchObjectException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import javax.ejb.EJBAccessException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.EntityBean;
import javax.ejb.NoSuchEntityException;
import javax.ejb.Timer;
import javax.transaction.TransactionSynchronizationRegistry;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.ContainerType;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.SystemException;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.core.BaseContext;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ExceptionType;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.timer.EjbTimerService;
import org.apache.openejb.core.timer.EjbTimerServiceImpl;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.handleApplicationException;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.handleSystemException;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.afterInvoke;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.createTransactionPolicy;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

/**
 * @org.apache.xbean.XBean element="bmpContainer"
 */
public class EntityContainer implements RpcContainer {

    private EntityInstanceManager instanceManager;

    private Map<String,CoreDeploymentInfo> deploymentRegistry  = new HashMap<String,CoreDeploymentInfo>();

    private Object containerID = null;

    public static Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");
    private SecurityService securityService;

    /**
     * Tracks entity instances that have been "entered" so we can throw reentrancy exceptions.
     */
    protected EntrancyTracker entrancyTracker;

    public EntityContainer(Object id, SecurityService securityService, int poolSize) throws OpenEJBException {
        this.containerID = id;
        this.securityService = securityService;
        entrancyTracker = new EntrancyTracker(SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class));

        instanceManager = new EntityInstanceManager(this, securityService, poolSize);
    }

    public synchronized DeploymentInfo [] deployments() {
        return deploymentRegistry.values().toArray(new DeploymentInfo[deploymentRegistry.size()]);
    }

    public synchronized DeploymentInfo getDeploymentInfo(Object deploymentID) {
        String id = (String) deploymentID;
        return deploymentRegistry.get(id);
    }

    public ContainerType getContainerType() {
        return ContainerType.BMP_ENTITY;
    }

    public Object getContainerID() {
        return containerID;
    }

    public void deploy(DeploymentInfo info) throws OpenEJBException {
        synchronized (this) {
            CoreDeploymentInfo deploymentInfo = (CoreDeploymentInfo) info;
            deploymentRegistry.put((String)deploymentInfo.getDeploymentID(), deploymentInfo);
            deploymentInfo.setContainer(this);
        }
        instanceManager.deploy(info);

        EjbTimerService timerService = info.getEjbTimerService();
        if (timerService != null) {
            timerService.start();
        }
    }

    public void undeploy(DeploymentInfo info) throws OpenEJBException {
        EjbTimerService timerService = info.getEjbTimerService();
        if (timerService != null) {
            timerService.stop();
        }

        instanceManager.undeploy(info);

        synchronized (this) {
            String id = (String) info.getDeploymentID();
            deploymentRegistry.remove(id);
            info.setContainer(null);
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
            boolean authorized = type == InterfaceType.TIMEOUT || getSecurityService().isCallerAuthorized(callMethod, type);
            if (!authorized)
                throw new org.apache.openejb.ApplicationException(new EJBAccessException("Unauthorized Access by Principal Denied"));

            Class declaringClass = callMethod.getDeclaringClass();
            String methodName = callMethod.getName();

            if (EJBHome.class.isAssignableFrom(declaringClass) || EJBLocalHome.class.isAssignableFrom(declaringClass)) {
                if (declaringClass != EJBHome.class && declaringClass != EJBLocalHome.class) {

                    if (methodName.startsWith("create")) {

                        return createEJBObject(callMethod, args, callContext);
                    } else if (methodName.startsWith("find")) {

                        return findMethod(callMethod, args, callContext);
                    } else {

                        return homeMethod(callMethod, args, callContext);
                    }
                } else if (methodName.equals("remove")) {
                    removeEJBObject(callMethod, args, callContext);
                    return null;
                }
            } else if ((EJBObject.class == declaringClass || EJBLocalObject.class == declaringClass) && methodName.equals("remove")) {
                removeEJBObject(callMethod, args, callContext);
                return null;
            }

            callContext.setCurrentOperation(type == InterfaceType.TIMEOUT ? Operation.TIMEOUT : Operation.BUSINESS);
            Method runMethod = deployInfo.getMatchingBeanMethod(callMethod);

            callContext.set(Method.class, runMethod);

            Object retValue = invoke(callMethod, runMethod, args, callContext);

            return retValue;

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

    protected Object invoke(Method callMethod, Method runMethod, Object [] args, ThreadContext callContext) throws OpenEJBException {
        CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
        TransactionPolicy txPolicy = createTransactionPolicy(deploymentInfo.getTransactionType(callMethod), callContext);

        EntityBean bean = null;

        Object returnValue = null;
        entrancyTracker.enter(callContext.getDeploymentInfo(), callContext.getPrimaryKey());
        try {
            bean = instanceManager.obtainInstance(callContext);

            ejbLoad_If_No_Transaction(callContext, bean);
            returnValue = runMethod.invoke(bean, args);
            ejbStore_If_No_Transaction(callContext, bean);
            instanceManager.poolInstance(callContext, bean, callContext.getPrimaryKey());
        } catch (Throwable e) {
            handleException(txPolicy, e, callContext, bean);
        } finally {
            entrancyTracker.exit(callContext.getDeploymentInfo(), callContext.getPrimaryKey());
            afterInvoke(txPolicy, callContext);
        }

        return returnValue;
    }

    public void ejbLoad_If_No_Transaction(ThreadContext callContext, EntityBean bean) throws Exception {
        Operation orginalOperation = callContext.getCurrentOperation();
        if (orginalOperation == Operation.BUSINESS || orginalOperation == Operation.REMOVE) {

            TransactionPolicy callerTxPolicy = callContext.getTransactionPolicy();
            if (callerTxPolicy != null && callerTxPolicy.isTransactionActive()) {
                return;
            }

            CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
            TransactionPolicy txPolicy = deploymentInfo.getTransactionPolicyFactory().createTransactionPolicy(TransactionType.Supports);
            try {
                // double check we don't have an active transaction
                if (!txPolicy.isTransactionActive()) {
                    callContext.setCurrentOperation(Operation.LOAD);
                    bean.ejbLoad();
                }
            } catch (NoSuchEntityException e) {
                instanceManager.discardInstance(callContext, bean);
                throw new ApplicationException(new NoSuchObjectException("Entity not found: " + callContext.getPrimaryKey())/*.initCause(e)*/);
            } catch (Exception e) {
                instanceManager.discardInstance(callContext, bean);
                throw e;
            } finally {
                callContext.setCurrentOperation(orginalOperation);
                txPolicy.commit();
            }

        }
    }

    public void ejbStore_If_No_Transaction(ThreadContext callContext, EntityBean bean) throws Exception {
        Operation currentOp = callContext.getCurrentOperation();
        if (currentOp == Operation.BUSINESS) {

            TransactionPolicy callerTxPolicy = callContext.getTransactionPolicy();
            if (callerTxPolicy != null && callerTxPolicy.isTransactionActive()) {
                return;
            }

            CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
            TransactionPolicy txPolicy = deploymentInfo.getTransactionPolicyFactory().createTransactionPolicy(TransactionType.Supports);
            try {
                // double check we don't have an active transaction
                if (!txPolicy.isTransactionActive()) {
                    callContext.setCurrentOperation(Operation.STORE);
                    bean.ejbStore();
                }
            } catch (Exception e) {
                instanceManager.discardInstance(callContext, bean);
                throw e;
            } finally {
                callContext.setCurrentOperation(currentOp);
                txPolicy.commit();
            }
        }
    }

    protected void didCreateBean(ThreadContext callContext, EntityBean bean) throws OpenEJBException {
    }

    protected ProxyInfo createEJBObject(Method callMethod, Object [] args, ThreadContext callContext) throws OpenEJBException {
        CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();

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

        TransactionPolicy txPolicy = createTransactionPolicy(deploymentInfo.getTransactionType(callMethod), callContext);

        EntityBean bean = null;
        Object primaryKey = null;
        try {
            // Get new ready instance
            bean = instanceManager.obtainInstance(callContext);

            // Obtain the proper ejbCreate() method
            Method ejbCreateMethod = deploymentInfo.getMatchingBeanMethod(callMethod);

            // invoke the ejbCreate which returns the primary key
            primaryKey = ejbCreateMethod.invoke(bean, args);

            didCreateBean(callContext, bean);

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

            // update pool
            instanceManager.poolInstance(callContext, bean, primaryKey);
        } catch (Throwable e) {
            handleException(txPolicy, e, callContext, bean);
        } finally {
            afterInvoke(txPolicy, callContext);
        }

        return new ProxyInfo(deploymentInfo, primaryKey);

    }

    protected Object findMethod(Method callMethod, Object [] args, ThreadContext callContext) throws OpenEJBException {
        CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
        callContext.setCurrentOperation(Operation.FIND);
        Method runMethod = deploymentInfo.getMatchingBeanMethod(callMethod);
        Object returnValue = invoke(callMethod, runMethod, args, callContext);

        /*
        * Find operations return either a single primary key or a collection of primary keys.
        * The primary keys are converted to ProxyInfo objects.
        */
        if (returnValue instanceof java.util.Collection) {
            Iterator keys = ((Collection) returnValue).iterator();
            Vector<ProxyInfo> proxies = new Vector<ProxyInfo>();
            while (keys.hasNext()) {
                Object primaryKey = keys.next();
                proxies.addElement(new ProxyInfo(deploymentInfo, primaryKey));
            }
            returnValue = proxies;
        } else if (returnValue instanceof java.util.Enumeration) {
            Enumeration keys = (Enumeration) returnValue;
            Vector<ProxyInfo> proxies = new Vector<ProxyInfo>();
            while (keys.hasMoreElements()) {
                Object primaryKey = keys.nextElement();
                proxies.addElement(new ProxyInfo(deploymentInfo, primaryKey));
            }
            returnValue = new org.apache.openejb.util.ArrayEnumeration(proxies);
        } else
            returnValue = new ProxyInfo(deploymentInfo, returnValue);

        return returnValue;
    }

    protected Object homeMethod(Method callMethod, Object [] args, ThreadContext callContext) throws OpenEJBException {
        org.apache.openejb.core.CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
        callContext.setCurrentOperation(Operation.HOME);
        Method runMethod = deploymentInfo.getMatchingBeanMethod(callMethod);
        return invoke(callMethod, runMethod, args, callContext);
    }

    protected void didRemove(EntityBean bean, ThreadContext threadContext) throws OpenEJBException {
        cancelTimers(threadContext);
    }

    private void cancelTimers(ThreadContext threadContext) {
        CoreDeploymentInfo deploymentInfo = threadContext.getDeploymentInfo();
        Object primaryKey = threadContext.getPrimaryKey();

        // if we have a real timerservice, stop all timers. Otherwise, ignore...
        if (primaryKey != null) {
            EjbTimerService timerService = deploymentInfo.getEjbTimerService();
            if (timerService != null && timerService instanceof EjbTimerServiceImpl) {
                for (Timer timer : deploymentInfo.getEjbTimerService().getTimers(primaryKey)) {
                    timer.cancel();
                }
            }
        }
    }

    protected void removeEJBObject(Method callMethod, Object [] args, ThreadContext callContext) throws OpenEJBException {
        callContext.setCurrentOperation(Operation.REMOVE);

        CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
        TransactionPolicy txPolicy = createTransactionPolicy(deploymentInfo.getTransactionType(callMethod), callContext);

        EntityBean bean = null;
        try {

            bean = instanceManager.obtainInstance(callContext);

            ejbLoad_If_No_Transaction(callContext, bean);
            bean.ejbRemove();
            didRemove(bean, callContext);
            instanceManager.poolInstance(callContext, bean, callContext.getPrimaryKey());
        } catch (Throwable e) {
            handleException(txPolicy, e, callContext, bean);
        } finally {
            afterInvoke(txPolicy, callContext);
        }
    }

    private void handleException(TransactionPolicy txPolicy, Throwable e, ThreadContext callContext, EntityBean bean) throws OpenEJBException {
        ExceptionType type;
        if (e instanceof InvocationTargetException) {
            e = ((InvocationTargetException) e).getTargetException();
            type = callContext.getDeploymentInfo().getExceptionType(e);
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
                } catch (SystemException e1) {
                    logger.error("The instance manager encountered an unkown system exception while trying to discard the entity instance with primary key " + callContext.getPrimaryKey());
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
