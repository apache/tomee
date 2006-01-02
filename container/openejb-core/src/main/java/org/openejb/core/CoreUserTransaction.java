package org.openejb.core;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

public class CoreUserTransaction
        implements javax.transaction.UserTransaction, java.io.Serializable {

    private transient TransactionManager _txManager;

    private transient final org.apache.log4j.Category txLogger;

    public CoreUserTransaction(TransactionManager txMngr) {
        _txManager = txMngr;
        txLogger = org.apache.log4j.Category.getInstance("Transaction");
    }

    public CoreUserTransaction() {
        this(org.openejb.OpenEJB.getTransactionManager());
    }

    private TransactionManager transactionManager() {
        if (_txManager == null) {
            _txManager = org.openejb.OpenEJB.getTransactionManager();
        }
        return _txManager;
    }

    public void begin()
            throws NotSupportedException, SystemException {
        transactionManager().begin();
        if (txLogger.isInfoEnabled()) {
            txLogger.info("Started user transaction " + transactionManager().getTransaction());
        }
    }

    public void commit()
            throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
            SecurityException, IllegalStateException, SystemException {
        if (txLogger.isInfoEnabled()) {
            txLogger.info("Committing user transaction " + transactionManager().getTransaction());
        }
        transactionManager().commit();
    }

    public void rollback()
            throws IllegalStateException, SecurityException, SystemException {
        if (txLogger.isInfoEnabled()) {
            txLogger.info("Rolling back user transaction " + transactionManager().getTransaction());
        }
        transactionManager().rollback();
    }

    public int getStatus()
            throws SystemException {
        int status = transactionManager().getStatus();
        if (txLogger.isInfoEnabled()) {
            txLogger.info("User transaction " + transactionManager().getTransaction() + " has status " + org.openejb.core.TransactionManagerWrapper.getStatus(status));
        }
        return status;
    }

    public void setRollbackOnly() throws javax.transaction.SystemException {
        if (txLogger.isInfoEnabled()) {
            txLogger.info("Marking user transaction for rollback: " + transactionManager().getTransaction());
        }
        transactionManager().setRollbackOnly();
    }

    public void setTransactionTimeout(int seconds)
            throws SystemException {
        transactionManager().setTransactionTimeout(seconds);
    }

}