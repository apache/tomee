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

import org.apache.openejb.ApplicationException;
import org.apache.openejb.InvalidateReferenceException;
import org.apache.openejb.ContainerType;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.transaction.TransactionContext;
import org.apache.openejb.core.transaction.TransactionPolicy;

import javax.ejb.SessionSynchronization;

public class SessionSynchronizationTxPolicy extends TransactionPolicy {

    protected TransactionPolicy policy;

    public SessionSynchronizationTxPolicy(TransactionPolicy policy) {
        super(policy.getPolicyType(), policy.getContainer());
        this.policy = policy;
        if (container.getContainerType() != ContainerType.STATEFUL ||
                getPolicyType() == TransactionPolicy.Type.Never ||
                getPolicyType() == TransactionPolicy.Type.NotSupported) {
            throw new IllegalArgumentException();
        }

    }

    public void beforeInvoke(Object instance, TransactionContext context) throws SystemException, ApplicationException {
        policy.beforeInvoke(instance, context);

        if (context.currentTx == null) return;

        try {
            StatefulInstanceManager.Instance instance2 = (StatefulInstanceManager.Instance) instance;
            SessionSynchronization session = (SessionSynchronization) instance2.bean ;
            SessionSynchronizationCoordinator.registerSessionSynchronization(session, context);
        } catch (javax.transaction.RollbackException e) {
            logger.error("Cannot register the SessionSynchronization bean with the transaction, the transaction has been rolled back");
            handleSystemException(e, instance, context);
        } catch (javax.transaction.SystemException e) {
            logger.error("Cannot register the SessionSynchronization bean with the transaction, received an unknown system exception from the transaction manager: " + e.getMessage());
            handleSystemException(e, instance, context);
        } catch (Throwable e) {
            logger.error("Cannot register the SessionSynchronization bean with the transaction, received an unknown exception: " + e.getClass().getName() + " " + e.getMessage());
            handleSystemException(e, instance, context);
        }
    }

    public void afterInvoke(Object instance, TransactionContext context) throws ApplicationException, SystemException {
        policy.afterInvoke(instance, context);
    }

    public void handleApplicationException(Throwable appException, boolean rollback, TransactionContext context) throws ApplicationException, SystemException {
        policy.handleApplicationException(appException, rollback, context);
    }

    public void handleSystemException(Throwable sysException, Object instance, TransactionContext context) throws ApplicationException, SystemException {
        try {
            policy.handleSystemException(sysException, instance, context);
        } catch (ApplicationException e) {
            throw new InvalidateReferenceException(e.getRootCause());
        }
    }

}

