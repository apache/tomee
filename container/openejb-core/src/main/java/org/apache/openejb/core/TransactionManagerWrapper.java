package org.apache.openejb.core;

import java.util.Hashtable;
import java.util.Vector;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

/**
 * @org.apache.xbean.XBean element="transactionManagerWrapper"
 */
public class TransactionManagerWrapper implements TransactionManager {
    final private TransactionManager transactionManager;
    final private Hashtable wrapperMap = new Hashtable();

    final static protected org.apache.log4j.Category logger = org.apache.log4j.Category.getInstance("Transaction");

    public TransactionManagerWrapper(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public javax.transaction.TransactionManager getTxManager() {
        return transactionManager;
    }

    public void begin() throws javax.transaction.SystemException, javax.transaction.NotSupportedException {
        int status = transactionManager.getStatus();
        if (status == Status.STATUS_NO_TRANSACTION ||
                status == Status.STATUS_ROLLEDBACK ||
                status == Status.STATUS_COMMITTED) {
            transactionManager.begin();
            createTxWrapper();
        } else {
            throw new javax.transaction.NotSupportedException("Can't start new transaction." + getStatus(status));
        }
    }

    public void commit() throws javax.transaction.SystemException, javax.transaction.RollbackException, javax.transaction.HeuristicRollbackException, javax.transaction.HeuristicMixedException {
        transactionManager.commit();
    }

    public int getStatus() throws javax.transaction.SystemException {
        return transactionManager.getStatus();
    }

    public Transaction getTransaction() throws javax.transaction.SystemException {
        return getTxWrapper(transactionManager.getTransaction());
    }

    public void resume(Transaction tx)
            throws javax.transaction.SystemException, javax.transaction.InvalidTransactionException {
        if (tx instanceof TransactionWrapper) {
            tx = ((TransactionWrapper) tx).transaction;
        }
        transactionManager.resume(tx);
    }

    public Transaction suspend() throws javax.transaction.SystemException {
        return getTxWrapper(transactionManager.suspend());
    }

    public void rollback() throws javax.transaction.SystemException {
        transactionManager.rollback();
    }

    public void setRollbackOnly() throws javax.transaction.SystemException {
        transactionManager.setRollbackOnly();
    }

    public void setTransactionTimeout(int x) throws javax.transaction.SystemException {
        transactionManager.setTransactionTimeout(x);
    }

    private Transaction getTxWrapper(Transaction tx) throws javax.transaction.SystemException {
        if (tx == null) {
            return null;
        }
        return (TransactionWrapper) wrapperMap.get(tx);
    }

    private void createTxWrapper() {
        try {
            Transaction tx = transactionManager.getTransaction();
            TransactionWrapper txW = new TransactionWrapper(tx);
            tx.registerSynchronization(txW);
            wrapperMap.put(tx, txW);
        } catch (Exception re) {

            logger.info("", re);
        }
    }

    private class TransactionWrapper
            implements Transaction, javax.transaction.Synchronization {

        private final Transaction transaction;

        private final Vector registeredSynchronizations;

        final public static int MAX_PRIORITY_LEVEL = 3;

        private TransactionWrapper(Transaction tx) {
            transaction = tx;
            registeredSynchronizations = new Vector();
        }

        public Transaction getTransaction() {

            return transaction;
        }

        public boolean equals(java.lang.Object obj) {
            if (obj != null && obj instanceof TransactionWrapper) {
                return transaction.equals(((TransactionWrapper) obj).getTransaction());
            }

            return false;
        }

        public int hashCode() {
            return transaction.hashCode();
        }

        public String toString() {
            return transaction.toString();
        }

        public void commit()
                throws javax.transaction.SystemException, javax.transaction.RollbackException, javax.transaction.HeuristicRollbackException, javax.transaction.HeuristicMixedException {
            transaction.commit();
        }

        public boolean delistResource(XAResource xaRes, int flag) throws javax.transaction.SystemException {
            return transaction.delistResource(xaRes, flag);
        }

        public boolean enlistResource(XAResource xaRes) throws javax.transaction.SystemException, javax.transaction.RollbackException {
            return transaction.enlistResource(xaRes);
        }

        public int getStatus() throws javax.transaction.SystemException {
            return transaction.getStatus();
        }

        /*
        * Automatically add the Synchronization object to the lowest priority register.
        * Synchronization objects are executed in groups according to their priority 
        * and within each group according to the order they were registered.
        */
        public void registerSynchronization(Synchronization sync)
                throws javax.transaction.SystemException, javax.transaction.RollbackException {

            registerSynchronization(sync, MAX_PRIORITY_LEVEL);
        }

        private void registerSynchronization(Synchronization sync, int priority) {
            if (!registeredSynchronizations.contains(sync)) {
                registeredSynchronizations.addElement(sync);
            }
        }

        public void rollback() throws javax.transaction.SystemException {
            transaction.rollback();
        }

        public void setRollbackOnly() throws javax.transaction.SystemException {
            transaction.setRollbackOnly();
        }

        public void beforeCompletion() {
            int count = registeredSynchronizations.size();
            for (int i = 0; i < count; ++i) {
                try {
                    Synchronization sync = (Synchronization) registeredSynchronizations.elementAt(i);
                    sync.beforeCompletion();
                } catch (RuntimeException re) {
                    logger.error("", re);
                }
            }
        }

        public void afterCompletion(int status) {
            int count = registeredSynchronizations.size();
            for (int i = 0; i < count; ++i) {
                try {
                    Synchronization sync = (Synchronization) registeredSynchronizations.elementAt(i);
                    sync.afterCompletion(status);
                } catch (RuntimeException re) {
                    logger.error("", re);
                }
            }
            wrapperMap.remove(transaction);
        }

    }// End Innerclass: TransctionWrapper

    public static String getStatus(int status) {
        StringBuffer buffer;

        buffer = new StringBuffer(100);
        switch (status) {
            case Status.STATUS_ACTIVE:
                buffer.append("STATUS_ACTIVE: ");
                buffer.append("A transaction is associated with the target object and it is in the active state.");
                break;
            case Status.STATUS_COMMITTED:
                buffer.append("STATUS_COMMITTED: ");
                buffer.append("A transaction is associated with the target object and it has been committed.");
                break;
            case Status.STATUS_COMMITTING:
                buffer.append("STATUS_COMMITTING: ");
                buffer.append("A transaction is associated with the target object and it is in the process of committing.");
                break;
            case Status.STATUS_MARKED_ROLLBACK:
                buffer.append("STATUS_MARKED_ROLLBACK: ");
                buffer.append("A transaction is associated with the target object and it has been marked for rollback, perhaps as a result of a setRollbackOnly operation.");
                break;
            case Status.STATUS_NO_TRANSACTION:
                buffer.append("STATUS_NO_TRANSACTION: ");
                buffer.append("No transaction is currently associated with the target object.");
                break;
            case Status.STATUS_PREPARED:
                buffer.append("STATUS_PREPARED: ");
                buffer.append("A transaction is associated with the target object and it has been prepared, i.e.");
                break;
            case Status.STATUS_PREPARING:
                buffer.append("STATUS_PREPARING: ");
                buffer.append("A transaction is associated with the target object and it is in the process of preparing.");
                break;
            case Status.STATUS_ROLLEDBACK:
                buffer.append("STATUS_ROLLEDBACK: ");
                buffer.append("A transaction is associated with the target object and the outcome has been determined as rollback.");
                break;
            case Status.STATUS_ROLLING_BACK:
                buffer.append("STATUS_ROLLING_BACK: ");
                buffer.append("A transaction is associated with the target object and it is in the process of rolling back.");
                break;
            default:
                buffer.append("Unknown status " + status);
                break;
        }
        return buffer.toString();
    }
}
