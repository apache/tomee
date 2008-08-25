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

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.UserTransaction;
import static javax.transaction.Status.STATUS_NO_TRANSACTION;
import static javax.transaction.Status.STATUS_MARKED_ROLLBACK;
import static javax.transaction.Status.STATUS_ACTIVE;

import org.apache.openejb.SystemException;
import org.apache.openejb.core.transaction.BeanTransactionPolicy;
import org.apache.openejb.core.transaction.TransactionType;
import org.springframework.transaction.HeuristicCompletionException;
import org.springframework.transaction.NestedTransactionNotSupportedException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;

public class SpringBeanTransactionPolicy extends SpringTransactionPolicy implements BeanTransactionPolicy {
    private final UserTransaction userTransaction;
    private DefaultTransactionStatus beanTransaction;
    private int timeout;

    public SpringBeanTransactionPolicy(PlatformTransactionManager transactionManager) {
        super(transactionManager, TransactionType.BeanManaged);
        userTransaction = new SpringUserTransaction();
    }

    protected DefaultTransactionStatus getTransactionStatus() {
        return beanTransaction != null ? beanTransaction : super.getTransactionStatus();
    }

    public SuspendedTransaction suspendUserTransaction() throws SystemException {
        throw new SystemException(new UnsupportedOperationException("SpringTransactionPolicy does not support transaction suspension"));
    }

    public void resumeUserTransaction(SuspendedTransaction suspendedTransaction) throws SystemException {
        throw new SystemException(new UnsupportedOperationException("SpringTransactionPolicy does not support transaction resumption"));
    }

    public UserTransaction getUserTransaction() {
        if (getTransactionStatus().isCompleted()) {
            throw new IllegalStateException("SpringBeanTransactionPolicy transaction has been completed");
        }
        return userTransaction;
    }

    private class SpringUserTransaction implements UserTransaction {
        public int getStatus() {
            if (getTransactionStatus().isCompleted() || beanTransaction == null) {
                return STATUS_NO_TRANSACTION;
            } else if (isRollbackOnly()) {
                return STATUS_MARKED_ROLLBACK;
            } else {
                return STATUS_ACTIVE;
            }
        }

        public void begin() throws NotSupportedException, javax.transaction.SystemException {
            if (getTransactionStatus().isCompleted()) {
                throw new IllegalStateException("SpringBeanTransactionPolicy transaction has been completed");
            }

            if (beanTransaction != null) {
                // we could support nested transactions
                throw new NotSupportedException("Current thread is already associated with a transaction");
            }

            try {
                // create transaction definition
                DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
                definition.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
                if (timeout > 0) {
                    definition.setTimeout(timeout);
                }

                // start the transaction
                TransactionStatus transactionStatus = transactionManager.getTransaction(definition);

                // TransactionStatus must be a DefaultTransactionStatus so we can implement isTransactionActive()
                if (!(transactionManager instanceof DefaultTransactionStatus)) {
                    transactionManager.rollback(transactionStatus);
                    throw new IllegalArgumentException("SpringBeanTransactionPolicy only works with a PlatformTransactionManager that uses DefaultTransactionStatus");
                }
                beanTransaction = (DefaultTransactionStatus) transactionStatus;
            } catch (TransactionException e) {
                // check if exception is simply wrapping a JTA exception
                Throwable cause = e.getCause();
                if (cause instanceof NotSupportedException) {
                    throw (NotSupportedException) cause;
                } else if (cause instanceof javax.transaction.SystemException) {
                    throw (javax.transaction.SystemException) cause;
                }

                // convert to JTA exception
                if (e instanceof NestedTransactionNotSupportedException) {
                    throw createJtaException(NotSupportedException.class, e);
                }
                throw createJtaException(javax.transaction.SystemException.class, e);
            }
        }

        public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, javax.transaction.SystemException {
            if (getTransactionStatus().isCompleted()) {
                throw new IllegalStateException("SpringBeanTransactionPolicy transaction has been completed");
            }

            if (beanTransaction == null) {
                throw new IllegalStateException("Current thread is not associated with a transaction");
            }

            try {
                transactionManager.commit(beanTransaction);
            } catch (TransactionException e) {
                // check if exception is simply wrapping a JTA exception
                Throwable cause = e.getCause();
                if (cause instanceof HeuristicMixedException) {
                    throw (HeuristicMixedException) cause;
                } else if (cause instanceof HeuristicRollbackException) {
                    throw (HeuristicRollbackException) cause;
                } else if (cause instanceof IllegalStateException) {
                    throw (IllegalStateException) cause;
                } else if (cause instanceof RollbackException) {
                    throw (RollbackException) cause;
                } else if (cause instanceof SecurityException) {
                    throw (SecurityException) cause;
                } else if (cause instanceof javax.transaction.SystemException) {
                    throw (javax.transaction.SystemException) cause;
                }

                // convert to JTA exception
                if (e instanceof HeuristicCompletionException) {
                    HeuristicCompletionException heuristicCompletionException = (HeuristicCompletionException) e;
                    if (heuristicCompletionException.getOutcomeState() == HeuristicCompletionException.STATE_MIXED) {
                        throw createJtaException(HeuristicMixedException.class, e);
                    } else if (heuristicCompletionException.getOutcomeState() == HeuristicCompletionException.STATE_ROLLED_BACK) {
                        throw createJtaException(HeuristicRollbackException.class, e);
                    }
                } else if (e instanceof UnexpectedRollbackException) {
                    throw createJtaException(RollbackException.class, e);
                }
                throw createJtaException(javax.transaction.SystemException.class, e);
            }
        }

        public void rollback() throws IllegalStateException, javax.transaction.SystemException {
            if (getTransactionStatus().isCompleted()) {
                throw new IllegalStateException("SpringBeanTransactionPolicy transaction has been completed");
            }

            if (beanTransaction == null) {
                throw new IllegalStateException("Current thread is not associated with a transaction");
            }

            try {
                transactionManager.rollback(beanTransaction);
            } catch (TransactionException e) {
                // check if exception is simply wrapping a JTA exception
                Throwable cause = e.getCause();
                if (cause instanceof IllegalStateException) {
                    throw (IllegalStateException) cause;
                } else if (cause instanceof SecurityException) {
                    throw (SecurityException) cause;
                } else if (cause instanceof javax.transaction.SystemException) {
                    throw (javax.transaction.SystemException) cause;
                }

                // convert to JTA exception
                throw createJtaException(javax.transaction.SystemException.class, e);
            }
        }

        public void setRollbackOnly() throws IllegalStateException, javax.transaction.SystemException {
            if (getTransactionStatus().isCompleted()) {
                throw new IllegalStateException("SpringBeanTransactionPolicy transaction has been completed");
            }

            if (beanTransaction == null) {
                throw new IllegalStateException("Current thread is not associated with a transaction");
            }

            try {
                beanTransaction.setRollbackOnly();
            } catch (Exception e) {
                // check if exception is simply wrapping a JTA exception
                Throwable cause = e.getCause();
                if (cause instanceof IllegalStateException) {
                    throw (IllegalStateException) cause;
                } else if (cause instanceof javax.transaction.SystemException) {
                    throw (javax.transaction.SystemException) cause;
                }

                // convert to JTA exception
                throw createJtaException(javax.transaction.SystemException.class, e);
            }
        }

        public void setTransactionTimeout(int timeout) throws javax.transaction.SystemException {
            if (getTransactionStatus().isCompleted()) {
                throw new IllegalStateException("SpringBeanTransactionPolicy transaction has been completed");
            }

            if (timeout < 0) {
                throw new javax.transaction.SystemException("timeout is negative");
            }
            SpringBeanTransactionPolicy.this.timeout = timeout;
        }

        private <T extends Exception> T createJtaException(Class<T> type, Throwable cause) throws javax.transaction.SystemException {
            T exception;
            try {
                exception = type.getConstructor(String.class).newInstance("Spring PlatformTransactionManager threw an exception");
            } catch (Exception e) {
                javax.transaction.SystemException systemException = new javax.transaction.SystemException("Spring PlatformTransactionManager threw an exception");
                systemException.initCause(cause);
                throw systemException;
            }
            exception.initCause(cause);
            return exception;
        }
    }
}
