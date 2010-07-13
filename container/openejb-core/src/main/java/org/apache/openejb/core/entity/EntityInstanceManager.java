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

import org.apache.openejb.ApplicationException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.SystemException;
import org.apache.openejb.InvalidateReferenceException;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.NoSuchObjectException;
import org.apache.openejb.core.transaction.TransactionRolledbackException;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.transaction.TransactionPolicy.TransactionSynchronization;
import org.apache.openejb.util.LinkedListStack;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Stack;

import javax.ejb.EntityBean;
import javax.ejb.NoSuchEntityException;
import java.util.HashMap;
import java.util.Map;
import java.rmi.RemoteException;

public class EntityInstanceManager {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");

    /**
     *  The default size of the bean pools. Every bean class gets its own pool of this size
     */
    private int poolsize = 0;

    /**
     * contains a collection of LinkListStacks indexed by deployment id. Each indexed stack
     * represents the method ready pool of for that class.
     */
    private Map<Object,LinkedListStack> poolMap = null;


    private SecurityService securityService;

    public EntityInstanceManager(EntityContainer container, SecurityService securityService, int poolSize) {
        this.securityService = securityService;
        this.poolsize = poolSize;
        poolMap = new HashMap<Object,LinkedListStack>();// put size in later

        DeploymentInfo[] deploymentInfos = container.deployments();
        for (DeploymentInfo deploymentInfo : deploymentInfos) {
            deploy(deploymentInfo);
        }
    }

    public void deploy(DeploymentInfo deploymentInfo) {
        poolMap.put(deploymentInfo.getDeploymentID(), new LinkedListStack(poolsize / 2));
    }

    public void undeploy(DeploymentInfo deploymentInfo) {
        poolMap.remove(deploymentInfo.getDeploymentID());
    }

    public EntityBean obtainInstance(ThreadContext callContext) throws OpenEJBException {
        // primary key is null if its a servicing a home methods (create, find, ejbHome)
        Object primaryKey = callContext.getPrimaryKey();
        TransactionPolicy txPolicy = callContext.getTransactionPolicy();
        if (callContext.getPrimaryKey() != null && txPolicy != null && txPolicy.isTransactionActive()) {

            Key key = new Key(callContext.getDeploymentInfo().getDeploymentID(), primaryKey);
            SynchronizationWrapper wrapper = (SynchronizationWrapper) txPolicy.getResource(key);

            if (wrapper != null) {// if true, the requested bean instance is already enrolled in a transaction

                if (!wrapper.isAssociated()) {// is NOT associated
                    /*
                    * If the bean identity was removed (via ejbRemove()) within the same transaction,
                    * then it's SynchronizationWrapper will be in the txReady pool but marked as disassociated.
                    * This allows us to prevent a condition where the caller removes the bean and then attempts to
                    * call a business method on that bean within the same transaction.  After a bean is removed any
                    * subsequent invocations on that bean with the same transaction should throw a NoSuchEntityException.
                    * its likely that the application server would have already made the reference invalid, but this bit of
                    * code is an extra precaution.
                    */
                    throw new InvalidateReferenceException(new NoSuchObjectException("Entity not found: " + primaryKey));
                } else if (callContext.getCurrentOperation() == Operation.REMOVE) {
                    /*
                    *  To avoid calling ejbStore( ) on a bean that after its removed, we can not delegate
                    *  the wrapper is marked as disassociated from the transaction to avoid processing the
                    *  beforeCompletion( ) method on the SynchronizationWrapper object.
                    */
                    wrapper.disassociate();
                }

                if (wrapper.isAvailable() || wrapper.primaryKey.equals(primaryKey)) {
                    return wrapper.getEntityBean();
                } else {

                    // If the bean is declared as reentrant then the instance may be accessed
                    // by more then one thread at a time.  This is one of the reasons that reentrancy
                    // is bad. In this case beans must be programmed to be multi threaded. The other reason
                    // reentrancy is bad has to do with transaction isolation. Multiple instances writing to
                    // the same database records will inevitably cancel out previous writes within the same tx.
                    //
                    // In the future we may change this to return a new instance of the bean and to
                    // link it and its wrapper to the original wrapper, but for now we choose this strategy because
                    // its simpler to implement.
                    return wrapper.getEntityBean();
                }
            } else {
                /*
                * If no synchronized wrapper for the key exists
                * Then the bean entity is being access by this transaction for the first time,
                * so it needs to be enrolled in the transaction.
                */
                EntityBean bean = getPooledInstance(callContext);
                wrapper = new SynchronizationWrapper(callContext.getDeploymentInfo(), primaryKey, bean, false, key, txPolicy);

                if (callContext.getCurrentOperation() == Operation.REMOVE) {
                    /*
                    *  To avoid calling ejbStore( ) on a bean that after its removed, we can not delegate
                    *  the wrapper is marked as disassociated from the transaction to avoid processing the
                    *  beforeCompletion( ) method on the SynchronizationWrapper object.
                    *
                    *  We have to still use a wrapper so we can detect when a business method is called after
                    *  a ejbRemove() and act to prevent it from being processed.
                    */
                    wrapper.disassociate();
                }

                txPolicy.registerSynchronization(wrapper);

                loadingBean(bean, callContext);
                Operation orginalOperation = callContext.getCurrentOperation();
                callContext.setCurrentOperation(Operation.LOAD);
                try {
                    bean.ejbLoad();
                } catch (NoSuchEntityException e) {
                    wrapper.disassociate();
                    throw new InvalidateReferenceException(new NoSuchObjectException("Entity not found: " + primaryKey, e));
                } catch (Exception e) {
                    logger.error("Exception encountered during ejbLoad():", e);
                    //djencks not sure about this dissociate call
                    wrapper.disassociate();
                    throw new OpenEJBException(e);
                } finally {
                    callContext.setCurrentOperation(orginalOperation);
                }
                txPolicy.putResource(key, wrapper);

                return bean;
            }
        } else {
            // If no transaction is associated with the thread or if its a create, find or home method
            // (primaryKey == null), then no synchronized wrapper is needed. if bean instance is used
            // for a create method then a syncrhonziation wrapper may be assigned when the bean is
            // returned to the pool -- depending on if the tx is a client initiated or container initiated.
            return getPooledInstance(callContext);
        }
    }

    protected void loadingBean(EntityBean bean, ThreadContext callContext) throws OpenEJBException {
    }

    protected void reusingBean(EntityBean bean, ThreadContext callContext) throws OpenEJBException {
    }

    protected EntityBean getPooledInstance(ThreadContext callContext) throws OpenEJBException {
        CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
        Stack methodReadyPool = poolMap.get(deploymentInfo.getDeploymentID());
        if (methodReadyPool == null) throw new SystemException("Invalid deployment id " + deploymentInfo.getDeploymentID() + " for this container");

        EntityBean bean = (EntityBean) methodReadyPool.pop();
        if (bean == null) {
            try {
                bean = (EntityBean) deploymentInfo.getBeanClass().newInstance();
            } catch (Exception e) {
                logger.error("Bean instantiation failed for class " + deploymentInfo.getBeanClass(), e);
                throw new SystemException(e);
            }

            Operation currentOp = callContext.getCurrentOperation();
            callContext.setCurrentOperation(Operation.SET_CONTEXT);

            try {
                /*
                * setEntityContext executes in an unspecified transactional context. In this case we choose to
                * allow it to have what every transaction context is current. Better then suspending it
                * unnecessarily.
                *
                * We also chose not to invoke EntityContainer.invoke( ) method, which duplicate the exception handling
                * logic but also attempt to manage the begining and end of a transaction. It its a container managed transaciton
                * we don't want the TransactionScopeHandler commiting the transaction in afterInvoke() which is what it would attempt
                * to do.
                */
                bean.setEntityContext(createEntityContext());
            } catch (Exception e) {
                /*
                * The EJB 1.1 specification does not specify how exceptions thrown by setEntityContext impact the
                * transaction, if there is one.  In this case we choose the least disruptive operation, throwing an
                * application exception and NOT automatically marking the transaciton for rollback.
                */
                logger.error("Bean callback method failed ", e);
                throw new ApplicationException(e);
            } finally {
                callContext.setCurrentOperation(currentOp);
            }
        } else {
            reusingBean(bean, callContext);
        }

        if ((callContext.getCurrentOperation() == Operation.BUSINESS) || (callContext.getCurrentOperation() == Operation.REMOVE)) {
            /*
            * When a bean is retrieved from the bean pool to service a client's business method request it must be
            * notified that its about to enter service by invoking its ejbActivate( ) method. A bean instance
            * does not have its ejbActivate() invoked when:
            * 1. Its being retreived to service an ejbCreate()/ejbPostCreate().
            * 2. Its being retrieved to service an ejbFind method.
            * 3. Its being retrieved to service an ejbRemove() method.
            * See section 9.1.4 of the EJB 1.1 specification.
            */
            Operation currentOp = callContext.getCurrentOperation();

            callContext.setCurrentOperation(Operation.ACTIVATE);
            try {
                /*
                In the event of an exception, OpenEJB is required to log the exception, evict the instance,
                and mark the transaction for rollback.  If there is a transaction to rollback, then the a
                javax.transaction.TransactionRolledbackException must be throw to the client.
                See EJB 1.1 specification, section 12.3.2
                */
                bean.ejbActivate();
            } catch (Throwable e) {
                logger.error("Encountered exception during call to ejbActivate()", e);
                TransactionPolicy txPolicy = callContext.getTransactionPolicy();
                if (txPolicy != null && txPolicy.isTransactionActive()) {
                    txPolicy.setRollbackOnly();
                    throw new ApplicationException(new TransactionRolledbackException("Reflection exception thrown while attempting to call ejbActivate() on the instance", e));
                }
                throw new ApplicationException(new RemoteException("Exception thrown while attempting to call ejbActivate() on the instance. Exception message = " + e.getMessage(), e));
            } finally {
                callContext.setCurrentOperation(currentOp);
            }

        }
        return bean;
    }

    private EntityContext createEntityContext() {
        return new EntityContext(securityService);
    }

    public void poolInstance(ThreadContext callContext, EntityBean bean, Object primaryKey) throws OpenEJBException {
        if (bean == null) {
            return;
        }

        // primary key is null if its a servicing a home methods (create, find, ejbHome)
        TransactionPolicy txPolicy = callContext.getTransactionPolicy();
        if (primaryKey != null && txPolicy != null && txPolicy.isTransactionActive()) {

            Key key = new Key(callContext.getDeploymentInfo().getDeploymentID(), primaryKey);
            SynchronizationWrapper wrapper = (SynchronizationWrapper) txPolicy.getResource(key);

            if (wrapper != null) {
                if (callContext.getCurrentOperation() == Operation.REMOVE) {
                    /*
                    * The bean is being returned to the pool after it has been removed. Its
                    * important at this point to mark the bean as disassociated to prevent
                    * it's ejbStore method from bean called (see SynchronizationWrapper.beforeCompletion() method)
                    * and that subsequent methods can not be invoked on the bean identity (see obtainInstance() method).
                    */
                    wrapper.disassociate();
                    /*
                    * If the bean has been removed then the bean instance is no longer needed and can return to the methodReadyPool
                    * to service another identity.
                    */
                    Stack methodReadyPool = poolMap.get(callContext.getDeploymentInfo().getDeploymentID());
                    methodReadyPool.push(bean);
                } else {
                    if (callContext.getCurrentOperation() == Operation.CREATE) {
                        // Bean is being recreated (new-delete-new) so we need to reassociate it
                        wrapper.associate();
                    }
                    wrapper.setEntityBean(bean);
                }
            } else {
                /*
                A wrapper will not exist if the bean is being returned after a create operation.
                In this case the transaction scope is broader then the create method itself; its a client
                initiated transaction, so the bean must be registered with the tranaction and moved to the
                tx ready pool
                */

                wrapper = new SynchronizationWrapper(callContext.getDeploymentInfo(), primaryKey, bean, true, key, txPolicy);

                txPolicy.registerSynchronization(wrapper);

                txPolicy.putResource(key, wrapper);
            }
        } else {
            /*
            If there is no transaction associated with the thread OR if the operation was a find or home method (PrimaryKey == null)
            Then the bean instance is simply returned to the methodReady pool
            */

            if (primaryKey != null && callContext.getCurrentOperation() != Operation.REMOVE) {
                /*
                * If the bean has a primary key; And its not being returned following a remove operation;
                * then the bean is being returned to the method ready pool after successfully executing a business method or create
                * method. In this case we need to call the bean instance's ejbPassivate before returning it to the pool per EJB 1.1
                * Section 9.1.
                */
                Operation currentOp = callContext.getCurrentOperation();

                callContext.setCurrentOperation(Operation.PASSIVATE);

                try {
                    /*
                    In the event of an exception, OpenEJB is required to log the exception, evict the instance,
                    and mark the transaction for rollback.  If there is a transaction to rollback, then the a
                    javax.transaction.TransactionRolledbackException must be throw to the client.
                    See EJB 1.1 specification, section 12.3.2
                    */
                    bean.ejbPassivate();
                } catch (Throwable e) {
                    if (txPolicy.isTransactionActive()) {
                        txPolicy.setRollbackOnly();
                        throw new ApplicationException(new TransactionRolledbackException("Reflection exception thrown while attempting to call ejbPassivate() on the instance", e));
                    }
                    throw new ApplicationException(new RemoteException("Reflection exception thrown while attempting to call ejbPassivate() on the instance. Exception message = " + e.getMessage(), e));
                } finally {
                    callContext.setCurrentOperation(currentOp);
                }
            }

            /*
            * The bean is returned to the method ready pool if its returned after servicing a find, ejbHome, business or create
            * method and is not still part of a tx.  While in the method ready pool the bean instance is not associated with a
            * primary key and may be used to service a request for any bean of the same class.
            */
            Stack methodReadyPool = poolMap.get(callContext.getDeploymentInfo().getDeploymentID());
            methodReadyPool.push(bean);
        }

    }

    public void freeInstance(ThreadContext callContext, EntityBean bean) throws SystemException {

        discardInstance(callContext, bean);

        Operation currentOp = callContext.getCurrentOperation();
        callContext.setCurrentOperation(Operation.UNSET_CONTEXT);

        try {
            /*
            * unsetEntityContext executes in an unspecified transactional context. In this case we choose to
            * allow it to have what every transaction context is current. Better then suspending it
            * unnecessarily.
            *
            * We also chose not to invoke EntityContainer.invoke( ) method, which duplicate the exception handling
            * logic but also attempt to manage the begining and end of a transaction. It its a container managed transaciton
            * we don't want the TransactionScopeHandler commiting the transaction in afterInvoke() which is what it would attempt
            * to do.
            */
            bean.unsetEntityContext();
        } catch (Exception e) {
            /*
            * The EJB 1.1 specification does not specify how exceptions thrown by unsetEntityContext impact the
            * transaction, if there is one.  In this case we choose to do nothing since the instance is being disposed
            * of anyway.
            */

            logger.info(getClass().getName() + ".freeInstance: ignoring exception " + e + " on bean instance " + bean);
        } finally {
            callContext.setCurrentOperation(currentOp);
        }

    }

    public void discardInstance(ThreadContext callContext, EntityBean bean) throws SystemException {
        Object primaryKey = callContext.getPrimaryKey();
        TransactionPolicy txPolicy = callContext.getTransactionPolicy();
        if (primaryKey == null || txPolicy == null || !txPolicy.isTransactionActive()) {
            return;
        }

        // The wrapper is removed (if pooled) so that it can not be accessed again. This is
        // especially important in the obtainInstance( ) method where a disassociated wrapper
        // in the txReadyPool is indicative of an entity bean that has been removed via
        // ejbRemove() rather than freed because of an error condition as is the case here.
        Key key = new Key(callContext.getDeploymentInfo().getDeploymentID(), primaryKey);
        SynchronizationWrapper wrapper = (SynchronizationWrapper) txPolicy.getResource(key);
        if (wrapper != null) {
            /*
             It's not possible to deregister a wrapper with the transaction,
             but it can be removed from the tx pool and made inoperative by
             calling its disassociate method. The wrapper will be returned to the
             wrapper pool after the transaction completes
             (see SynchronizationWrapper.afterCompletion( ) method).  The wrapper must
             be returned after the transaction completes so that it is not in the service
             of another bean when the TransactionManager calls its Synchronization methods.

             In addition, the bean instance is dereferenced so it can be garbage
             collected.
            */
            wrapper.disassociate();
        }
    }

    /*
    * Instances of this class are used as keys for storing bean instances in the tx method
    * ready pool.  A compound key composed of the transaction, primary key, and deployment id
    * identifiers is required to uniquely identify a bean in the tx method ready pool.
    */
    private static class Key {
        private final Object deploymentId;
        private final Object primaryKey;

        public Key(Object deploymentId, Object primaryKey) {
            if (deploymentId == null) throw new NullPointerException("deploymentId is null");
            if (primaryKey == null) throw new NullPointerException("primaryKey is null");

            this.deploymentId = deploymentId;
            this.primaryKey = primaryKey;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            return deploymentId.equals(key.deploymentId) && primaryKey.equals(key.primaryKey);
        }

        public int hashCode() {
            int result;
            result = deploymentId.hashCode();
            result = 31 * result + primaryKey.hashCode();
            return result;
        }
    }

    /*
    * Instances of this class are used to wrap entity instances so that they can be registered
    * with a tx.  When the Synchronization.beforeCompletion is called, the bean's ejbStore method
    * is invoked.  When the Synchroniztion.afterCompletion is called, the bean instance is returned
    * to the method ready pool. Instances of this class are not recycled anymore, because modern VMs
    * (JDK1.3 and above) perform better for objects that are short lived.
    */
    protected class SynchronizationWrapper implements TransactionSynchronization {
        private EntityBean bean;
        /*
        * <tt>isAvailable<tt> determines if the wrapper is still associated with a bean.  If the bean identity is removed (ejbRemove)
        * or if the bean instance is discarded, the wrapper will not longer be associated with a bean instances
        * and therefore its beforeCompletion method will not process the ejbStore method.
        */
        private boolean available;
        private boolean associated;
        private final Key readyPoolKey;
        private final CoreDeploymentInfo deploymentInfo;
        private final Object primaryKey;
        private final TransactionPolicy txPolicy;

        public SynchronizationWrapper(CoreDeploymentInfo deploymentInfo, Object primaryKey, EntityBean bean, boolean available, Key readyPoolKey, TransactionPolicy txPolicy) {
            if (bean == null) throw new IllegalArgumentException("bean is null");
            if (readyPoolKey == null) throw new IllegalArgumentException("key is null");
            if (deploymentInfo == null) throw new IllegalArgumentException("deploymentInfo is null");
            if (primaryKey == null) throw new IllegalArgumentException("primaryKey is null");
            if (txPolicy == null) throw new IllegalArgumentException("txEnv is null");

            this.deploymentInfo = deploymentInfo;
            this.bean = bean;
            this.primaryKey = primaryKey;
            this.available = available;
            this.readyPoolKey = readyPoolKey;
            this.txPolicy = txPolicy;
            associated = true;
        }

        public void associate() {
            associated = true;
        }

        public void disassociate() {
            associated = false;
        }

        public boolean isAssociated() {
            return associated;
        }

        public synchronized boolean isAvailable() {
            return available;
        }

        public synchronized void setEntityBean(EntityBean ebean) {
            available = true;
            bean = ebean;
        }

        public synchronized EntityBean getEntityBean() {
            available = false;
            return bean;
        }

        public void beforeCompletion() {
            if (associated) {
                EntityBean bean;
                synchronized (this) {
                    bean = this.bean;
                }

                ThreadContext callContext = new ThreadContext(deploymentInfo, primaryKey);
                callContext.setCurrentOperation(Operation.STORE);

                ThreadContext oldCallContext = ThreadContext.enter(callContext);

                try {
                    bean.ejbStore();
                } catch (Exception re) {
                    logger.error("Exception occured during ejbStore()", re);
                    txPolicy.setRollbackOnly();
                } finally {
                    ThreadContext.exit(oldCallContext);
                }
            }
        }

        public void afterCompletion(Status status) {
            txPolicy.removeResource(readyPoolKey);
        }
    }
}

