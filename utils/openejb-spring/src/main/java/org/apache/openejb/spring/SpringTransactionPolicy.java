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
package org.apache.openejb.spring;

import javax.transaction.RollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.xa.XAResource;

import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.core.transaction.TransactionPolicy.TransactionSynchronization.Status;
import org.apache.openejb.ApplicationException;
import org.apache.openejb.SystemException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.HeuristicCompletionException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;

public class SpringTransactionPolicy implements TransactionPolicy {
    protected final PlatformTransactionManager transactionManager;
    protected final TransactionType type;
    private final DefaultTransactionStatus transactionStatus;

    public SpringTransactionPolicy(PlatformTransactionManager transactionManager, TransactionType type) {
        this.transactionManager = transactionManager;
        this.type = type;

        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        switch (type) {
            case BeanManaged:
                transactionDefinition.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
                break;
            case Mandatory:
                transactionDefinition.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_MANDATORY);
                break;
            case Never:
                transactionDefinition.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_NEVER);
                break;
            case NotSupported:
                transactionDefinition.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
                break;
            case Required:
                transactionDefinition.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRED);
                break;
            case RequiresNew:
                transactionDefinition.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                break;
            case Supports:
                transactionDefinition.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_SUPPORTS);
                break;
        }

        TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
        if (!(transactionManager instanceof DefaultTransactionStatus)) {
            throw new IllegalArgumentException("SpringTransactionPolicy only works with a PlatformTransactionManager that uses DefaultTransactionStatus");
        }
        this.transactionStatus = (DefaultTransactionStatus) transactionStatus;
    }

    public TransactionType getTransactionType() {
        return type;
    }

    public boolean isNewTransaction() {
        return getTransactionStatus().isNewTransaction();
    }

    public boolean isClientTransaction() {
        DefaultTransactionStatus status = getTransactionStatus();
        return status.hasTransaction() && !status.isNewSynchronization();
    }

    public boolean isTransactionActive() {
        return !getTransactionStatus().isCompleted() && getTransactionStatus().hasTransaction();
    }

    public boolean isRollbackOnly() {
        return getTransactionStatus().isRollbackOnly();
    }

    public void setRollbackOnly(Throwable t) {
        getTransactionStatus().setRollbackOnly();
    }
    
    public void setRollbackOnly() {
        getTransactionStatus().setRollbackOnly();
    }

    public void commit() throws ApplicationException, SystemException {
        try {
            transactionManager.commit(getTransactionStatus());
        } catch (TransactionException e) {
            // check if exception is simply wrapping a supported JTA exception
            Throwable cause = e.getCause();
            if (cause instanceof RollbackException) {
                throw new ApplicationException(cause);
            } else if (cause instanceof HeuristicMixedException) {
                throw new ApplicationException(cause);
            } else if (cause instanceof HeuristicRollbackException) {
                throw new ApplicationException(cause);
            } else if (cause instanceof IllegalStateException) {
                throw new SystemException(cause);
            } else if (cause instanceof SecurityException) {
                throw new SystemException(cause);
            } else if (cause instanceof javax.transaction.SystemException) {
                throw new SystemException(cause);
            }

            // wrap with application or system exception based on type
            if (e instanceof HeuristicCompletionException) {
                throw new ApplicationException(e);
            } else if (e instanceof UnexpectedRollbackException) {
                throw new ApplicationException(e);
            }
            throw new SystemException(e);
        }
    }

    public Object getResource(Object key) {
        Object resource = TransactionSynchronizationManager.getResource(key);
        return resource;
    }

    public void putResource(Object key, Object value) {
        TransactionSynchronizationManager.bindResource(key, value);
    }

    public Object removeResource(Object key) {
        Object resource = TransactionSynchronizationManager.unbindResource(key);
        return resource;
    }

    public void registerSynchronization(final TransactionSynchronization synchronization) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            public void beforeCompletion() {
                synchronization.beforeCompletion();
            }

            public void afterCompletion(int status) {
                Status s = null;
                if (status == STATUS_COMMITTED) {
                    s = Status.COMMITTED;
                } else if (status == STATUS_ROLLED_BACK) {
                    s = Status.ROLLEDBACK;
                } else if (status == STATUS_UNKNOWN) {
                    s = Status.UNKNOWN;
                }
                synchronization.afterCompletion(s);
            }
        });
    }

    public void enlistResource(XAResource xaResource) throws SystemException {
        throw new SystemException(new UnsupportedOperationException("SpringTransactionPolicy does not support XAResource enlistment"));
    }

    protected DefaultTransactionStatus getTransactionStatus() {
        return transactionStatus;
    }
}
