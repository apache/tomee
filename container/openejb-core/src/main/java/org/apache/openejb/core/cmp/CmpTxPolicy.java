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
package org.apache.openejb.core.cmp;

import java.rmi.RemoteException;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.core.transaction.TransactionContext;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.transaction.TransactionContainer;
import org.apache.openejb.core.RpcContainerWrapper;
import org.apache.openejb.core.CoreDeploymentInfo;

import javax.persistence.EntityTransaction;

public class CmpTxPolicy extends org.apache.openejb.core.transaction.TransactionPolicy {

    protected TransactionPolicy policy;

    protected final CmpContainer cmpContainer;

    public CmpTxPolicy(TransactionPolicy policy) {
        this.policy = policy;
        this.container = policy.getContainer();
        this.policyType = policy.policyType;

        cmpContainer = getCmpContainer(container);
    }

    private CmpContainer getCmpContainer(TransactionContainer container) {
        if (container instanceof RpcContainerWrapper) {
            RpcContainerWrapper wrapper = (RpcContainerWrapper) container;
            return getCmpContainer((TransactionContainer) wrapper.getContainer());
        } else {
            return (CmpContainer) container;
        }
    }

    public void beforeInvoke(Object instance, TransactionContext context) throws org.apache.openejb.SystemException, org.apache.openejb.ApplicationException {
        policy.beforeInvoke(instance, context);

//        DeploymentInfo deploymentInfo = context.callContext.getDeploymentInfo();
//        ClassLoader classLoader = deploymentInfo.getBeanClass().getClassLoader();

        try {
            if (context.currentTx == null) {
                CoreDeploymentInfo deploymentInfo = context.callContext.getDeploymentInfo();
                CmpEngine cmpEngine = cmpContainer.getCmpEngine(deploymentInfo.getDeploymentID());
                EntityTransaction entityTransaction = cmpEngine.getTransaction();
                entityTransaction.begin();
                context.callContext.setUnspecified(entityTransaction);
            } else {
                /*
                * If there is a transaction, that means that context is transaction-managed so
                * we make the unspecified field of the current ThreadContext null, which will 
                * be used by the getDatabase() method of this class to determine that a 
                * transaction-managed database object is needed.
                */
                context.callContext.setUnspecified(null);
            }
        } catch (Throwable e) {
            RemoteException re = new RemoteException("Encountered and unkown error when attempting to begin local transaciton", e);
            handleSystemException(re, instance, context);
        }
    }

    public void afterInvoke(Object instance, TransactionContext context) throws org.apache.openejb.ApplicationException, org.apache.openejb.SystemException {
        try {
            if (context.currentTx == null) {
                EntityTransaction entityTransaction = (EntityTransaction) context.callContext.getUnspecified();
                if (entityTransaction != null && entityTransaction.isActive()) {
                    entityTransaction.commit();
                }
            }
        } catch (javax.persistence.RollbackException e) {
            RemoteException ex = new RemoteException("Local transaction was rolled back", e);
            policy.handleApplicationException(ex, context);
        } catch (Throwable e) {
            RemoteException ex = new RemoteException("Encountered and unknown exception while attempting to commit the local transaction", e);
            policy.handleSystemException(ex, instance, context);
        } finally {
            policy.afterInvoke(instance, context);
        }
    }

    public void handleApplicationException(Throwable appException, TransactionContext context) throws ApplicationException {
        try {
            if (context.currentTx == null) {
                EntityTransaction entityTransaction = (EntityTransaction) context.callContext.getUnspecified();
                if (entityTransaction != null && entityTransaction.isActive()) {
                    entityTransaction.rollback();
                }
            }
        } catch (IllegalStateException ignored) {
        } catch (javax.persistence.PersistenceException e) {
            logger.debug("Exception while rolling back local transaction", e);
        } finally {
            policy.handleApplicationException(appException, context);
        }
    }

    public void handleSystemException(Throwable sysException, Object instance, TransactionContext context) throws org.apache.openejb.ApplicationException, org.apache.openejb.SystemException {
        try {
            if (context.currentTx == null) {
                EntityTransaction entityTransaction = (EntityTransaction) context.callContext.getUnspecified();
                if (entityTransaction != null && entityTransaction.isActive()) {
                    entityTransaction.rollback();
                }
            }
        } catch (IllegalStateException ignored) {
        } catch (javax.persistence.PersistenceException e) {
            logger.debug("Exception while rolling back local transaction", e);
        } finally {
            policy.handleSystemException(sysException, instance, context);
        }
    }

}

